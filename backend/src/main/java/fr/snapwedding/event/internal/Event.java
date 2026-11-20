package fr.snapwedding.event.internal;

import fr.snapwedding.event.EventStatus;
import fr.snapwedding.event.EventView;
import fr.snapwedding.shared.InvalidRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entité JPA d'un événement. Les règles métier (fermer, suspendre...) sont des méthodes de
 * l'entité : elles sont testables sans base de données (EventTest).
 */
@Entity
@Table(name = "events", indexes = @Index(name = "idx_events_organizer", columnList = "organizerId"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // pour JPA uniquement
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    /** Tenant : claim "sub" du JWT Google de l'organisateur. */
    @Column(nullable = false, updatable = false)
    private String organizerId;

    /** Code mis dans le QR code : 128 bits aléatoires, jamais l'id numérique. */
    @Column(nullable = false, unique = true, length = 32)
    private String publicCode;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Event(String organizerId, String name, LocalDate date, String location, String publicCode) {
        this.organizerId = organizerId;
        this.publicCode = publicCode;
        this.status = EventStatus.OPEN;
        this.createdAt = Instant.now();
        update(name, date, location);
    }

    public void update(String name, LocalDate date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }

    /** Fermeture par l'organisateur : plus de dépôt. Un événement suspendu ne peut pas être "fermé" à la place. */
    public void close() {
        if (status == EventStatus.SUSPENDED) {
            throw new InvalidRequestException("Event is suspended");
        }
        this.status = EventStatus.CLOSED;
    }

    /** Suspension par le super-admin : l'emporte sur tout autre statut. */
    public void suspend() {
        this.status = EventStatus.SUSPENDED;
    }

    /** Levée de la suspension : l'événement redevient ouvert. */
    public void reactivate() {
        if (status != EventStatus.SUSPENDED) {
            throw new InvalidRequestException("Event is not suspended");
        }
        this.status = EventStatus.OPEN;
    }

    public EventView toView() {
        return new EventView(id, name, date, location, status, organizerId, publicCode);
    }
}
