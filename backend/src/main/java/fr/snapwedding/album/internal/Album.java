package fr.snapwedding.album.internal;

import fr.snapwedding.album.AlbumView;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Un album. Il référence son événement par son id (eventId), pas par une relation JPA @ManyToOne :
 * les entités ne traversent pas les modules, chaque module reste maître de ses tables.
 */
@Entity
@Table(name = "albums", indexes = @Index(name = "idx_albums_event", columnList = "eventId"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Album(Long eventId, String name) {
        this.eventId = eventId;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public void rename(String name) {
        this.name = name;
    }

    public AlbumView toView() {
        return new AlbumView(id, eventId, name);
    }
}
