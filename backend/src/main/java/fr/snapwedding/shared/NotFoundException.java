package fr.snapwedding.shared;

/**
 * 404. Utilisée aussi quand la ressource existe mais appartient à un autre tenant :
 * on ne révèle pas qu'elle existe.
 */
public final class NotFoundException extends SnapWeddingException {
    public NotFoundException(String message) {
        super(message, null);
    }
}
