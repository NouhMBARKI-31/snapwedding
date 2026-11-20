package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;
import fr.snapwedding.shared.NotFoundException;
import fr.snapwedding.shared.SnapWeddingProperties;
import fr.snapwedding.shared.internal.OrganizerSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest : la couche web seule (controller + sécurité + erreurs), le service est un mock.
 * On importe NOTRE configuration de sécurité, sinon on testerait celle par défaut de Spring.
 */
@WebMvcTest(controllers = EventController.class, properties = "GOOGLE_CLIENT_ID=test-client-id")
@Import(OrganizerSecurityConfig.class)
@EnableConfigurationProperties(SnapWeddingProperties.class)
class EventControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService service;

    @MockitoBean
    private QrCodeService qrCodes;

    private static RequestPostProcessor organizer(String sub) {
        return jwt().jwt(token -> token.subject(sub));
    }

    @Test
    void requestsWithoutGoogleTokenAreRejected() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void anOrganizerGets404OnAnotherOrganizersEvent() throws Exception {
        when(service.getOwnedByCurrentOrganizer(42L)).thenThrow(new NotFoundException("Event 42 not found"));

        mvc.perform(get("/api/events/42").with(organizer("bob")))
                .andExpect(status().isNotFound());
    }

    @Test
    void theTenantIdNeverLeaksInTheResponse() throws Exception {
        when(service.getOwnedByCurrentOrganizer(1L)).thenReturn(new EventView(1L, "Mariage",
                LocalDate.of(2027, 6, 12), "Toulouse", EventStatus.OPEN, "alice", "abc"));
        when(qrCodes.guestUrl("abc")).thenReturn("http://localhost:5173/e/abc");

        mvc.perform(get("/api/events/1").with(organizer("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mariage"))
                .andExpect(jsonPath("$.guestUrl").value("http://localhost:5173/e/abc"))
                .andExpect(jsonPath("$.organizerId").doesNotExist());
    }
}
