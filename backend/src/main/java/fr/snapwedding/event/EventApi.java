package fr.snapwedding.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Ce que le module event offre aux autres modules (inversion de dépendance : ils dépendent
 * de cette interface, jamais de EventService ni du repository, qui restent internes).
 */
public interface EventApi {

    /** L'événement s'il appartient à l'organisateur connecté, sinon NotFoundException (404). */
    EventView getOwnedByCurrentOrganizer(Long eventId);

    /**
     * L'événement, sans contrôle de tenant. Réservé aux contrôles faits par le back lui-même
     * (validation du jeton invité, dépôt d'une photo) : jamais exposé tel quel dans une réponse.
     */
    EventView get(Long eventId);

    /** Recherche par le code public du QR code (invités). */
    Optional<EventView> findByPublicCode(String publicCode);

    // --- Super-admin uniquement : appelées par le module admin, protégé par ROLE_SUPER_ADMIN ---

    /** Tous les événements de la plateforme, tous tenants confondus. */
    Page<EventView> findAllForAdmin(Pageable pageable);

    void suspend(Long eventId);

    void reactivate(Long eventId);
}
