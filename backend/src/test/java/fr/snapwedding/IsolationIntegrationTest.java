package fr.snapwedding;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import software.amazon.awssdk.services.s3.S3Client;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'INTÉGRATION : toute l'application contre un vrai PostgreSQL (Testcontainers).
 * Il prouve l'isolation :
 *  - un organisateur ne voit pas l'événement d'un autre ;
 *  - un invité ne voit rien d'un autre événement que le sien.
 * S3 est mocké : ce test porte sur l'isolation, pas sur le stockage.
 */
@SpringBootTest(properties = "GOOGLE_CLIENT_ID=test-client-id")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class IsolationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @MockitoBean
    private S3Client s3Client;

    @Autowired
    private MockMvc mvc;

    private static RequestPostProcessor organizer(String sub) {
        return jwt().jwt(token -> token.subject(sub));
    }

    private String createEvent(String sub, String name) throws Exception {
        return mvc.perform(post("/api/events").with(organizer(sub))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "date": "2027-06-12", "location": "Toulouse"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private long createAlbum(String sub, long eventId) throws Exception {
        String body = mvc.perform(post("/api/events/" + eventId + "/albums").with(organizer(sub))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Soirée"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private String join(String publicCode) throws Exception {
        String body = mvc.perform(post("/api/public/events/" + publicCode + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pseudo": "Yassine"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    @Test
    void anOrganizerNeverSeesAnotherOrganizersEvent() throws Exception {
        // Des identifiants uniques : la base est partagée entre les tests de cette classe,
        // donc "bob" a peut-être déjà ses propres événements créés par l'autre test.
        String alice = "alice-" + UUID.randomUUID();
        String bob = "bob-" + UUID.randomUUID();

        String body = createEvent(alice, "Mariage d'Alice");
        long eventId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(get("/api/events").with(organizer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));

        mvc.perform(get("/api/events/" + eventId).with(organizer(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/events/" + eventId + "/albums").with(organizer(bob)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/events/" + eventId + "/qrcode").with(organizer(bob)))
                .andExpect(status().isNotFound());
    }

    @Test
    void aGuestOnlySeesTheEventOfHisToken() throws Exception {
        String alice = createEvent("alice", "Mariage");
        long aliceEventId = ((Number) JsonPath.read(alice, "$.id")).longValue();
        String aliceCode = JsonPath.read(alice, "$.publicCode");
        long aliceAlbum = createAlbum("alice", aliceEventId);

        String bob = createEvent("bob", "Anniversaire");
        long bobAlbum = createAlbum("bob", ((Number) JsonPath.read(bob, "$.id")).longValue());

        String token = join(aliceCode);

        // Son événement : OK
        mvc.perform(get("/api/guest/albums").header("X-Guest-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(aliceAlbum));
        mvc.perform(get("/api/guest/albums/" + aliceAlbum + "/media").header("X-Guest-Token", token))
                .andExpect(status().isOk());

        // L'album d'un autre événement : 404, comme s'il n'existait pas
        mvc.perform(get("/api/guest/albums/" + bobAlbum + "/media").header("X-Guest-Token", token))
                .andExpect(status().isNotFound());

        // Sans jeton, ou avec un code inventé : rien
        mvc.perform(get("/api/guest/albums")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/public/events/invented-code")).andExpect(status().isNotFound());

        // Le jeton invité n'ouvre pas les routes organisateur
        mvc.perform(get("/api/events").header("X-Guest-Token", token)).andExpect(status().isUnauthorized());
    }
}