package fr.snapwedding.guest.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from Guest g where g.eventId = :eventId")
    void deleteAllByEventId(Long eventId);
}
