package fr.snapwedding.admin;

import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventView;
import fr.snapwedding.media.MediaApi;
import fr.snapwedding.media.MediaStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Espace super-admin. Protégé deux fois : la règle d'URL /api/admin/** (OrganizerSecurityConfig)
 * et @PreAuthorize ici. Le filtre multi-tenant des organisateurs n'est pas affaibli :
 * on passe par des méthodes dédiées (findAllForAdmin), réservées à ce rôle.
 */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Admin", description = "Super-admin de la plateforme")
@SecurityRequirement(name = "googleJwt")
class AdminController {

    private final EventApi events;
    private final MediaApi media;

    @GetMapping
    @Operation(summary = "Tous les événements avec leurs statistiques (S1)")
    Page<AdminEventResponse> findAll(@PageableDefault(size = 20, sort = "date") Pageable pageable) {
        Page<EventView> page = events.findAllForAdmin(pageable);
        // UNE requête groupée pour toutes les statistiques de la page : pas de N+1
        Map<Long, MediaStats> stats = media.statsByEvent(page.map(EventView::id).getContent());

        return page.map(e -> {
            MediaStats s = stats.getOrDefault(e.id(), MediaStats.EMPTY);
            return new AdminEventResponse(e.id(), e.name(), e.date(), e.organizerId(), e.status(),
                    s.mediaCount(), s.totalBytes());
        });
    }

    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Suspendre un événement (S2)")
    void suspend(@PathVariable Long id) {
        events.suspend(id);
    }

    @PostMapping("/{id}/reactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Lever la suspension")
    void reactivate(@PathVariable Long id) {
        events.reactivate(id);
    }
}
