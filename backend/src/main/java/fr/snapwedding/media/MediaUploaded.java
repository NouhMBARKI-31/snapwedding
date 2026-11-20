package fr.snapwedding.media;

/**
 * Publié après chaque dépôt de photo. Personne ne l'écoute encore dans le socle :
 * c'est le point d'accroche de l'option "miniatures en asynchrone" (et de statistiques, notifications...).
 */
public record MediaUploaded(Long mediaId, Long eventId, Long albumId, long size) {
}
