package fr.snapwedding.guest;

/** L'invité connecté, tel que les autres modules le voient. Il appartient à UN seul événement. */
public record GuestView(Long id, Long eventId, String pseudo) {
}
