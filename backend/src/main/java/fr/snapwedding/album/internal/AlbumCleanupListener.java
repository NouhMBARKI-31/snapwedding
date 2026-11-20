package fr.snapwedding.album.internal;

import fr.snapwedding.event.EventDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Événement supprimé : on supprime ses albums. (Les photos sont gérées par le module media.) */
@Component
@RequiredArgsConstructor
class AlbumCleanupListener {

    private final AlbumService service;

    @ApplicationModuleListener
    void on(EventDeleted event) {
        service.deleteAllOfEvent(event.eventId());
    }
}
