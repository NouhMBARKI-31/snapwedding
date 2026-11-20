package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;

import java.time.LocalDate;

/** Ce qu'un visiteur qui a scanné le QR code voit avant de rejoindre : rien de sensible. */
public record PublicEventResponse(String name, LocalDate date, String location, EventStatus status) {

    static PublicEventResponse from(EventView e) {
        return new PublicEventResponse(e.name(), e.date(), e.location(), e.status());
    }
}
