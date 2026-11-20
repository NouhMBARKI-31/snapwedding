package fr.snapwedding.media;

/**
 * Publié après chaque dépôt de photo. Écouté par ThumbnailListener (option "miniatures asynchrones") ;
 * d'autres modules pourraient s'y abonner plus tard (statistiques, notifications...) sans toucher à media.
 */
public record MediaUploaded(Long mediaId, Long eventId, Long albumId, long size) {
}
