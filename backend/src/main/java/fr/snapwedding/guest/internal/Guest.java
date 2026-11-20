package fr.snapwedding.guest.internal;

import fr.snapwedding.guest.GuestView;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Un invité : un pseudo rattaché à UN événement. Pas de compte, pas de mot de passe.
 *
 * On ne stocke PAS le jeton, seulement son empreinte SHA-256 : si la base fuite,
 * personne ne peut se faire passer pour un invité (comme un mot de passe haché).
 */
@Entity
@Table(name = "guests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false, length = 40)
    private String pseudo;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Guest(Long eventId, String pseudo, String tokenHash) {
        this.eventId = eventId;
        this.pseudo = pseudo;
        this.tokenHash = tokenHash;
        this.createdAt = Instant.now();
    }

    public GuestView toView() {
        return new GuestView(id, eventId, pseudo);
    }
}
