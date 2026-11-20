package fr.snapwedding.media.internal;

import fr.snapwedding.media.MediaUploaded;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Option "miniatures asynchrones".
 *
 * @ApplicationModuleListener = @Async + @Transactional(REQUIRES_NEW) + @TransactionalEventListener :
 * - APRÈS le commit du dépôt : la photo existe bien en base ;
 * - dans un AUTRE thread : l'invité reçoit sa réponse sans attendre le calcul de la miniature ;
 * - avec le registre de publication JPA : si le serveur s'arrête en plein calcul, l'événement est
 *   rejoué au redémarrage (republish-outstanding-events-on-restart dans application.yaml).
 */
@Component
@RequiredArgsConstructor
class ThumbnailListener {

    private final MediaService service;

    @ApplicationModuleListener
    void on(MediaUploaded event) {
        service.createThumbnail(event.mediaId());
    }
}
