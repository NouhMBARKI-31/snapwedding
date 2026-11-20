package fr.snapwedding.media.internal;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Une photo. En base, seulement ses MÉTADONNÉES : le fichier lui-même est dans S3 (s3Key).
 *
 * guestPseudo est recopié ici au dépôt : la galerie affiche le pseudo sans jointure vers
 * la table des invités (un autre module) et sans requête par photo (pas de N+1).
 */
@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_media_album", columnList = "albumId, createdAt"),
        @Index(name = "idx_media_event", columnList = "eventId")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long albumId;

    @Column(nullable = false)
    private Long guestId;

    @Column(nullable = false, length = 40)
    private String guestPseudo;

    @Column(nullable = false, unique = true)
    private String s3Key;

    @Column(nullable = false)
    private String originalFilename;

    /** Type RÉEL détecté par Tika, pas celui annoncé par le client. */
    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long size;

    /** Masquée par l'organisateur (modération) : invisible pour les invités. */
    private boolean hidden;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Media(Long eventId, Long albumId, Long guestId, String guestPseudo, String s3Key,
                 String originalFilename, String contentType, long size) {
        this.eventId = eventId;
        this.albumId = albumId;
        this.guestId = guestId;
        this.guestPseudo = guestPseudo;
        this.s3Key = s3Key;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.createdAt = Instant.now();
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /** I5 : un invité ne peut supprimer que ses propres photos. */
    public boolean isOwnedByGuest(Long guestId) {
        return this.guestId.equals(guestId);
    }

    /** Un invité ne voit une photo que si elle est de son événement et non masquée. */
    public boolean isVisibleToGuestOf(Long eventId) {
        return this.eventId.equals(eventId) && !hidden;
    }
}
