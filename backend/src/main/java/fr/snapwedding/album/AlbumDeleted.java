package fr.snapwedding.album;

/** Publié à la suppression d'un album : le module media supprime ses photos (base + S3). */
public record AlbumDeleted(Long albumId, Long eventId) {
}
