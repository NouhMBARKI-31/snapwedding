package fr.snapwedding.event;

/**
 * OPEN      : les invités rejoignent et déposent des photos.
 * CLOSED    : fermé par l'organisateur, plus aucun dépôt, l'album reste consultable par l'organisateur.
 * SUSPENDED : bloqué par le super-admin (contenu signalé, abus...).
 */
public enum EventStatus {
    OPEN, CLOSED, SUSPENDED
}
