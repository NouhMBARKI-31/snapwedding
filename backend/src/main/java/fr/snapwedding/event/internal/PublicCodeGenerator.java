package fr.snapwedding.event.internal;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Code public de l'événement, mis dans le QR code.
 *
 * 16 octets = 128 bits tirés par SecureRandom (générateur cryptographique, imprévisible),
 * encodés en Base64 URL-safe sans "=" : 22 caractères. /events/42 se devine, ce code non :
 * il y a 2^128 valeurs possibles.
 */
@Component
class PublicCodeGenerator {

    private static final int BYTES = 16;
    private final SecureRandom random = new SecureRandom();

    String generate() {
        byte[] bytes = new byte[BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
