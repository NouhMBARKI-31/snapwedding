package fr.snapwedding.event.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface EventRepository extends JpaRepository<Event, Long> {

    /** Filtrage multi-tenant : toujours par organizerId. */
    Page<Event> findAllByOrganizerId(String organizerId, Pageable pageable);

    Optional<Event> findByIdAndOrganizerId(Long id, String organizerId);

    Optional<Event> findByPublicCode(String publicCode);
}
