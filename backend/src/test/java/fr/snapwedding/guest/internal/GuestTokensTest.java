package fr.snapwedding.guest.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuestTokensTest {

    private final GuestTokens tokens = new GuestTokens();

    @Test
    void tokensAreRandomAnd256Bits() {
        String a = tokens.generate();
        String b = tokens.generate();
        assertThat(a).hasSize(43).isNotEqualTo(b); // 32 octets en Base64 URL sans "=" = 43 caractères
    }

    @Test
    void theHashIsStableAndDoesNotContainTheToken() {
        String token = tokens.generate();
        assertThat(tokens.hash(token))
                .isEqualTo(tokens.hash(token))
                .hasSize(64) // SHA-256 en hexadécimal
                .doesNotContain(token);
    }
}
