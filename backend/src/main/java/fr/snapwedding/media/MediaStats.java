package fr.snapwedding.media;

/** Nombre de photos et espace de stockage utilisé par un événement. */
public record MediaStats(long mediaCount, long totalBytes) {

    public static final MediaStats EMPTY = new MediaStats(0, 0);
}
