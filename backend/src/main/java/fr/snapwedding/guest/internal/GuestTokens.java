package fr.snapwedding.guest.internal;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Jeton d'invité OPAQUE : une suite aléatoire sans signification, qu'on retrouve en base.
 *
 * - 32 octets (256 bits) tirés par SecureRandom : impossible à deviner.
 * - Stocké haché (SHA-256). Pas besoin de sel ni d'algorithme lent (bcrypt) : contrairement à
 *   un mot de passe choisi par un humain, le jeton est déjà totalement aléatoire.
 */
@Component
class GuestTokens {

    private final SecureRandom random = new SecureRandom();

    String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e); // n'arrive pas : SHA-256 est imposé par Java
        }
    }
}
