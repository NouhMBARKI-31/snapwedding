package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;
import fr.snapwedding.guest.GuestView;
import fr.snapwedding.shared.SnapWeddingProperties;
import fr.snapwedding.shared.internal.OrganizerSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste la 2e chaîne de sécurité (jeton d'invité) avec la vraie configuration,
 * les deux chaînes chargées ensemble comme dans l'application.
 */
@WebMvcTest(controllers = GuestController.class, properties = "GOOGLE_CLIENT_ID=test-client-id")
@Import({OrganizerSecurityConfig.class, GuestSecurityConfig.class, GuestTokens.class})
@EnableConfigurationProperties(SnapWeddingProperties.class)
class GuestSecurityTest {

    private static final EventView OPEN = new EventView(7L, "Mariage", LocalDate.of(2027, 6, 12),
            "Toulouse", EventStatus.OPEN, "alice", "code");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GuestService service;

    @MockitoBean
    private GuestRepository guests;

    @MockitoBean
    private EventApi events;

    private void aKnownGuestOfEventWithStatus(EventStatus status) {
        Guest guest = new Guest(7L, "Yassine", "hash");
        when(guests.findByTokenHash(anyString())).thenReturn(Optional.of(guest));
        when(events.get(7L)).thenReturn(new EventView(7L, "Mariage", OPEN.date(), "Toulouse", status, "alice", "code"));
        when(service.current()).thenReturn(new GuestView(1L, 7L, "Yassine"));
        when(service.eventOf(new GuestView(1L, 7L, "Yassine"))).thenReturn(OPEN);
    }

    @Test
    void withoutTokenTheGuestApiAnswers401() throws Exception {
        mvc.perform(get("/api/guest/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void anUnknownTokenIsRejected() throws Exception {
        when(guests.findByTokenHash(anyString())).thenReturn(Optional.empty());

        mvc.perform(get("/api/guest/me").header(GuestTokenFilter.HEADER, "made-up"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aValidTokenOfAnOpenEventIsAccepted() throws Exception {
        aKnownGuestOfEventWithStatus(EventStatus.OPEN);

        mvc.perform(get("/api/guest/me").header(GuestTokenFilter.HEADER, "valid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value("Yassine"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void theTokenBecomesUnusableWhenTheEventIsClosedOrSuspended() throws Exception {
        aKnownGuestOfEventWithStatus(EventStatus.CLOSED);
        mvc.perform(get("/api/guest/me").header(GuestTokenFilter.HEADER, "valid"))
                .andExpect(status().isUnauthorized());

        aKnownGuestOfEventWithStatus(EventStatus.SUSPENDED);
        mvc.perform(get("/api/guest/me").header(GuestTokenFilter.HEADER, "valid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aGoogleJwtDoesNotOpenTheGuestApi() throws Exception {
        // Authentifié, mais sans ROLE_GUEST : interdit
        mvc.perform(get("/api/guest/me").with(jwt().jwt(t -> t.subject("alice"))))
                .andExpect(status().isForbidden());
    }
}
