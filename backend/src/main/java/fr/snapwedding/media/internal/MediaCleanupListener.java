package fr.snapwedding.media.internal;

import fr.snapwedding.album.AlbumDeleted;
import fr.snapwedding.event.EventDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Réagit aux suppressions des autres modules (événements applicatifs) :
 * supprime les lignes en base, puis les fichiers S3 après le commit.
 */
@Component
@RequiredArgsConstructor
class MediaCleanupListener {

    private final MediaService service;

    @ApplicationModuleListener
    void on(EventDeleted event) {
        service.deleteAllOfEvent(event.eventId());
    }

    @ApplicationModuleListener
    void on(AlbumDeleted event) {
        service.deleteAllOfAlbum(event.albumId());
    }
}
