package fr.snapwedding.guest.internal;

/**
 * Réponse de "rejoindre" (avec le jeton) et de /api/guest/me (token = null).
 * Le front garde le jeton et le renvoie dans le header X-Guest-Token.
 */
public record GuestSessionResponse(String token, Long guestId, String pseudo, Long eventId, String eventName) {
}
