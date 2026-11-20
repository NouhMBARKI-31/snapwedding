package fr.snapwedding.shared;

/** 400 : requête refusée par une règle métier (ex. dépôt sur un événement fermé). */
public final class InvalidRequestException extends SnapWeddingException {
    public InvalidRequestException(String message) {
        super(message, null);
    }
}
