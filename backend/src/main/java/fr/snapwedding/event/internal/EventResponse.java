package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;

import java.time.LocalDate;

/** Réponse organisateur. Pas d'organizerId : c'est une donnée interne (le tenant). */
public record EventResponse(
        Long id,
        String name,
        LocalDate date,
        String location,
        EventStatus status,
        String publicCode,
        String guestUrl
) {
    static EventResponse from(EventView e, String guestUrl) {
        return new EventResponse(e.id(), e.name(), e.date(), e.location(), e.status(), e.publicCode(), guestUrl);
    }
}
