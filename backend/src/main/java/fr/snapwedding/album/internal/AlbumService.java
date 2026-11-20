package fr.snapwedding.album.internal;

import fr.snapwedding.album.AlbumApi;
import fr.snapwedding.album.AlbumDeleted;
import fr.snapwedding.album.AlbumView;
import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventView;
import fr.snapwedding.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * L'isolation multi-tenant des albums passe par l'événement parent :
 * avant toute action, on vérifie que l'événement appartient à l'organisateur connecté (EventApi).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AlbumService implements AlbumApi {

    private final AlbumRepository repository;
    private final EventApi events;
    private final ApplicationEventPublisher publisher;

    List<AlbumView> listForOrganizer(Long eventId) {
        events.getOwnedByCurrentOrganizer(eventId); // 404 si ce n'est pas son événement
        return listOfEvent(eventId);
    }

    List<AlbumView> listOfEvent(Long eventId) {
        return repository.findAllByEventIdOrderByCreatedAtAsc(eventId).stream().map(Album::toView).toList();
    }

    @Transactional
    AlbumView create(Long eventId, String name) {
        EventView event = events.getOwnedByCurrentOrganizer(eventId);
        return repository.save(new Album(event.id(), name)).toView();
    }

    @Transactional
    AlbumView rename(Long albumId, String name) {
        Album album = findOwned(albumId);
        album.rename(name);
        return album.toView();
    }

    @Transactional
    void delete(Long albumId) {
        Album album = findOwned(albumId);
        repository.delete(album);
        publisher.publishEvent(new AlbumDeleted(album.getId(), album.getEventId()));
    }

    private Album findOwned(Long albumId) {
        Album album = repository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album " + albumId + " not found"));
        events.getOwnedByCurrentOrganizer(album.getEventId()); // 404 si l'événement est à un autre
        return album;
    }

    @Override
    public AlbumView getForCurrentOrganizer(Long albumId) {
        return findOwned(albumId).toView();
    }

    @Override
    public AlbumView getInEvent(Long albumId, Long eventId) {
        return repository.findByIdAndEventId(albumId, eventId)
                .map(Album::toView)
                .orElseThrow(() -> new NotFoundException("Album " + albumId + " not found"));
    }

    @Transactional
    void deleteAllOfEvent(Long eventId) {
        repository.deleteAllByEventId(eventId);
    }
}
