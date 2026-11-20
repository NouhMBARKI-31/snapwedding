package fr.snapwedding.album.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface AlbumRepository extends JpaRepository<Album, Long> {

    List<Album> findAllByEventIdOrderByCreatedAtAsc(Long eventId);

    Optional<Album> findByIdAndEventId(Long id, Long eventId);

    @Modifying
    @Query("delete from Album a where a.eventId = :eventId")
    void deleteAllByEventId(Long eventId);
}
