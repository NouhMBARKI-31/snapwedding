package fr.snapwedding.media.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

/**
 * Toutes les listes sont paginées et lisent UNE seule table : 1 requête pour la page
 * + 1 requête de comptage, quel que soit le nombre de photos (pas de N+1).
 */
interface MediaRepository extends JpaRepository<Media, Long> {

    /** Organisateur : toutes les photos de l'album, masquées comprises (O5, O6). */
    Page<Media> findAllByAlbumId(Long albumId, Pageable pageable);

    /** Invité : seulement les photos visibles (I4). */
    Page<Media> findAllByAlbumIdAndHiddenFalse(Long albumId, Pageable pageable);

    /** Projection pour les statistiques du super-admin. */
    interface EventStatsRow {
        Long getEventId();

        Long getMediaCount();

        Long getTotalBytes();
    }

    @Query("""
            select m.eventId as eventId, count(m) as mediaCount, coalesce(sum(m.size), 0L) as totalBytes
            from Media m
            where m.eventId in :eventIds
            group by m.eventId
            """)
    List<EventStatsRow> statsByEvent(Collection<Long> eventIds);

    @Query("select m.s3Key from Media m where m.eventId = :eventId")
    List<String> findKeysByEventId(Long eventId);

    @Query("select m.s3Key from Media m where m.albumId = :albumId")
    List<String> findKeysByAlbumId(Long albumId);

    @Query("select m.thumbnailKey from Media m where m.eventId = :eventId and m.thumbnailKey is not null")
    List<String> findThumbnailKeysByEventId(Long eventId);

    @Query("select m.thumbnailKey from Media m where m.albumId = :albumId and m.thumbnailKey is not null")
    List<String> findThumbnailKeysByAlbumId(Long albumId);

    @Modifying
    @Query("delete from Media m where m.eventId = :eventId")
    void deleteAllByEventId(Long eventId);

    @Modifying
    @Query("delete from Media m where m.albumId = :albumId")
    void deleteAllByAlbumId(Long albumId);
}
