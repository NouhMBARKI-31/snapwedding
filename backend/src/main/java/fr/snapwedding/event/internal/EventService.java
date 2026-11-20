package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventDeleted;
import fr.snapwedding.event.EventView;
import fr.snapwedding.shared.CurrentOrganizer;
import fr.snapwedding.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Logique des événements. Implémente EventApi : c'est la seule porte d'entrée des autres modules.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class EventService implements EventApi {

    private final EventRepository repository;
    private final PublicCodeGenerator codeGenerator;
    private final ApplicationEventPublisher events;

    // ---------- Organisateur (toujours filtré par son tenant) ----------

    Page<EventView> findMine(Pageable pageable) {
        return repository.findAllByOrganizerId(CurrentOrganizer.id(), pageable).map(Event::toView);
    }

    @Transactional
    EventView create(String name, LocalDate date, String location) {
        Event event = new Event(CurrentOrganizer.id(), name, date, location, codeGenerator.generate());
        return repository.save(event).toView();
    }

    @Transactional
    EventView update(Long id, String name, LocalDate date, String location) {
        Event event = findOwned(id);
        event.update(name, date, location);
        return event.toView(); // dirty checking : Hibernate enregistre la modification au commit
    }

    @Transactional
    EventView close(Long id) {
        Event event = findOwned(id);
        event.close();
        return event.toView();
    }

    /**
     * Supprime l'événement et annonce EventDeleted. Les albums, photos et invités sont supprimés
     * par leurs propres modules, APRÈS le commit (Spring Modulith enregistre l'événement en base
     * et le rejoue en cas de crash).
     */
    @Transactional
    void delete(Long id) {
        Event event = findOwned(id);
        repository.delete(event);
        events.publishEvent(new EventDeleted(event.getId()));
    }

    private Event findOwned(Long id) {
        return repository.findByIdAndOrganizerId(id, CurrentOrganizer.id())
                // 404 et pas 403 : on ne révèle pas qu'un événement existe chez un autre organisateur
                .orElseThrow(() -> new NotFoundException("Event " + id + " not found"));
    }

    // ---------- EventApi (utilisée par les autres modules) ----------

    @Override
    public EventView getOwnedByCurrentOrganizer(Long eventId) {
        return findOwned(eventId).toView();
    }

    @Override
    public EventView get(Long eventId) {
        return repository.findById(eventId)
                .map(Event::toView)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
    }

    @Override
    public Optional<EventView> findByPublicCode(String publicCode) {
        return repository.findByPublicCode(publicCode).map(Event::toView);
    }

    /**
     * Seule méthode qui traverse les tenants. Double protection : l'URL /api/admin/** exige déjà
     * ROLE_SUPER_ADMIN, et @PreAuthorize le revérifie ici, au cas où un autre code l'appellerait.
     */
    @Override
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Page<EventView> findAllForAdmin(Pageable pageable) {
        return repository.findAll(pageable).map(Event::toView);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void suspend(Long eventId) {
        repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"))
                .suspend();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public void reactivate(Long eventId) {
        repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"))
                .reactivate();
    }
}
