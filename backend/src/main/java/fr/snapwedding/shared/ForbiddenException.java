package fr.snapwedding.shared;

/** 403 : l'appelant est identifié mais n'a pas le droit (ex. supprimer la photo d'un autre invité). */
public final class ForbiddenException extends SnapWeddingException {
    public ForbiddenException(String message) {
        super(message, null);
    }
}
