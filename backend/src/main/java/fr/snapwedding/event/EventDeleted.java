package fr.snapwedding.event;

/**
 * Publié quand un organisateur supprime un événement.
 *
 * Le module event ne connaît pas les albums, les photos ni les invités : il annonce
 * la suppression, et chaque module concerné fait son propre ménage (@ApplicationModuleListener).
 * C'est l'approche "événements applicatifs" : event ne dépend d'aucun autre module.
 */
public record EventDeleted(Long eventId) {
}
