package fr.snapwedding.album;

/** Ce que le module album offre aux autres modules (utilisé par media). */
public interface AlbumApi {

    /** L'album si son événement appartient à l'organisateur connecté, sinon 404. */
    AlbumView getForCurrentOrganizer(Long albumId);

    /**
     * L'album s'il fait partie de CET événement, sinon 404.
     * Pour un invité, eventId vient toujours de son jeton, jamais de l'URL (I6).
     */
    AlbumView getInEvent(Long albumId, Long eventId);
}
