package fr.snapwedding.media.internal;

import fr.snapwedding.album.AlbumApi;
import fr.snapwedding.album.AlbumView;
import fr.snapwedding.event.EventApi;
import fr.snapwedding.guest.GuestApi;
import fr.snapwedding.guest.GuestView;
import fr.snapwedding.media.MediaApi;
import fr.snapwedding.media.MediaStats;
import fr.snapwedding.media.MediaUploaded;
import fr.snapwedding.shared.ForbiddenException;
import fr.snapwedding.shared.InvalidRequestException;
import fr.snapwedding.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class MediaService implements MediaApi {

    private static final int MAX_FILES_PER_UPLOAD = 20;

    private final MediaRepository repository;
    private final MediaStorage storage;
    private final FileTypeChecker typeChecker;
    private final ThumbnailGenerator thumbnails;
    private final AlbumApi albums;
    private final EventApi events;
    private final GuestApi guests;
    private final ApplicationEventPublisher publisher;
    private final TransactionTemplate transaction;

    // ---------------- Invité ----------------

    /**
     * I3 : un invité dépose une ou plusieurs photos dans un album de SON événement.
     *
     * Ordre choisi : d'abord S3, puis la base.
     * - S3 échoue          : rien n'est en base, l'invité reçoit une erreur. Cohérent.
     * - S3 OK, base échoue : on supprime le fichier qu'on vient d'envoyer (compensation).
     * L'inverse (base puis S3) laisserait des lignes en base pointant vers un fichier absent.
     */
    List<Media> upload(Long albumId, List<MultipartFile> files) {
        GuestView guest = guests.current();
        AlbumView album = albums.getInEvent(albumId, guest.eventId()); // 404 si autre événement (I6)
        if (!events.get(guest.eventId()).isOpen()) {
            throw new InvalidRequestException("Event is not open"); // O7 : plus de dépôt
        }
        if (files == null || files.isEmpty() || files.size() > MAX_FILES_PER_UPLOAD) {
            throw new InvalidRequestException("Send between 1 and " + MAX_FILES_PER_UPLOAD + " files");
        }

        List<Media> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            saved.add(uploadOne(album, guest, file));
        }
        return saved;
    }

    private Media uploadOne(AlbumView album, GuestView guest, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("Empty file");
        }
        String contentType = typeChecker.detectAllowedType(file);
        String key = "events/%d/albums/%d/%s".formatted(album.eventId(), album.id(), UUID.randomUUID());

        try (InputStream in = file.getInputStream()) {
            storage.put(key, in, file.getSize(), contentType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            return transaction.execute(status -> {
                Media media = repository.save(new Media(album.eventId(), album.id(), guest.id(), guest.pseudo(),
                        key, safeName(file.getOriginalFilename()), contentType, file.getSize()));
                publisher.publishEvent(new MediaUploaded(media.getId(), media.getEventId(), media.getAlbumId(),
                        media.getSize()));
                return media;
            });
        } catch (RuntimeException e) {
            storage.deleteQuietly(List.of(key)); // compensation : pas de fichier orphelin
            throw e;
        }
    }

    @Transactional(readOnly = true)
    Page<Media> listForGuest(Long albumId, Pageable pageable) {
        GuestView guest = guests.current();
        albums.getInEvent(albumId, guest.eventId());
        return repository.findAllByAlbumIdAndHiddenFalse(albumId, pageable);
    }

    @Transactional(readOnly = true)
    Media getForGuest(Long mediaId) {
        GuestView guest = guests.current();
        return repository.findById(mediaId)
                .filter(m -> m.isVisibleToGuestOf(guest.eventId()))
                .orElseThrow(() -> new NotFoundException("Media " + mediaId + " not found"));
    }

    /** I5 : un invité supprime SA photo, et seulement la sienne. */
    @Transactional
    void deleteByGuest(Long mediaId) {
        GuestView guest = guests.current();
        Media media = repository.findById(mediaId)
                .filter(m -> m.getEventId().equals(guest.eventId()))
                .orElseThrow(() -> new NotFoundException("Media " + mediaId + " not found"));
        if (!media.isOwnedByGuest(guest.id())) {
            throw new ForbiddenException("You can only delete your own photos");
        }
        delete(media);
    }

    // ---------------- Organisateur ----------------

    @Transactional(readOnly = true)
    Page<Media> listForOrganizer(Long albumId, Pageable pageable) {
        albums.getForCurrentOrganizer(albumId); // 404 si ce n'est pas son album
        return repository.findAllByAlbumId(albumId, pageable);
    }

    @Transactional(readOnly = true)
    Media getForOrganizer(Long mediaId) {
        Media media = repository.findById(mediaId)
                .orElseThrow(() -> new NotFoundException("Media " + mediaId + " not found"));
        events.getOwnedByCurrentOrganizer(media.getEventId()); // 404 si autre organisateur
        return media;
    }

    /** O6 : modération, masquer ou réafficher. */
    @Transactional
    Media setHidden(Long mediaId, boolean hidden) {
        Media media = getForOrganizer(mediaId);
        media.setHidden(hidden);
        return media;
    }

    /** O6 : modération, supprimer. */
    @Transactional
    void deleteByOrganizer(Long mediaId) {
        delete(getForOrganizer(mediaId));
    }

    // ---------------- Commun ----------------

    InputStream content(Media media) {
        return storage.get(media.getS3Key());
    }

    InputStream thumbnail(Media media) {
        return storage.get(media.getThumbnailKey());
    }

    /**
     * Option "miniatures asynchrones" : appelé par ThumbnailListener, après le commit du dépôt,
     * dans un autre thread. L'invité n'attend donc jamais ce calcul.
     */
    @Transactional
    void createThumbnail(Long mediaId) {
        Media media = repository.findById(mediaId).orElse(null);
        if (media == null || media.hasThumbnail()) {
            return; // supprimée entre-temps, ou déjà faite (événement rejoué au redémarrage)
        }
        byte[] original;
        try (InputStream in = storage.get(media.getS3Key())) {
            original = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        byte[] jpeg = thumbnails.toJpeg(original).orElse(null);
        if (jpeg == null) {
            return; // format illisible par ImageIO (HEIC, WebP) : la galerie affichera l'original
        }
        String key = media.getS3Key() + "-thumb.jpg";
        storage.put(key, new ByteArrayInputStream(jpeg), jpeg.length, "image/jpeg");
        media.attachThumbnail(key); // sauvegardé au commit (entité suivie par JPA)
    }

    /**
     * Supprime la ligne, puis le fichier S3 APRÈS le commit : si la base échoue, le fichier
     * est toujours là (rien de perdu) ; si S3 échoue ensuite, il reste un orphelin, signalé dans les logs.
     */
    private void delete(Media media) {
        repository.delete(media);
        deleteFilesAfterCommit(media.storageKeys());
    }

    void deleteFilesAfterCommit(List<String> keys) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    storage.deleteQuietly(keys);
                }
            });
        } else {
            storage.deleteQuietly(keys);
        }
    }

    @Transactional
    void deleteAllOfEvent(Long eventId) {
        List<String> keys = new ArrayList<>(repository.findKeysByEventId(eventId));
        keys.addAll(repository.findThumbnailKeysByEventId(eventId));
        repository.deleteAllByEventId(eventId);
        deleteFilesAfterCommit(keys);
    }

    @Transactional
    void deleteAllOfAlbum(Long albumId) {
        List<String> keys = new ArrayList<>(repository.findKeysByAlbumId(albumId));
        keys.addAll(repository.findThumbnailKeysByAlbumId(albumId));
        repository.deleteAllByAlbumId(albumId);
        deleteFilesAfterCommit(keys);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, MediaStats> statsByEvent(Collection<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return repository.statsByEvent(eventIds).stream().collect(Collectors.toMap(
                MediaRepository.EventStatsRow::getEventId,
                row -> new MediaStats(row.getMediaCount(), row.getTotalBytes())));
    }

    private static String safeName(String name) {
        if (name == null || name.isBlank()) {
            return "photo";
        }
        String base = name.replaceAll("[\\\\/]", "_"); // pas de chemin dans le nom
        return base.length() > 200 ? base.substring(0, 200) : base;
    }
}
