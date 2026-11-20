package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Événement supprimé : ses invités (et donc leurs jetons) disparaissent aussi. */
@Component
@RequiredArgsConstructor
class GuestCleanupListener {

    private final GuestService service;

    @ApplicationModuleListener
    void on(EventDeleted event) {
        service.deleteAllOfEvent(event.eventId());
    }
}
