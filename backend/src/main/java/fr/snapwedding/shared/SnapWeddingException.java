package fr.snapwedding.shared;

/**
 * Erreurs métier de l'application. "sealed" : le compilateur connaît toutes les sous-classes,
 * donc le switch de ApiExceptionHandler est exhaustif sans "default".
 */
public sealed abstract class SnapWeddingException extends RuntimeException
        permits NotFoundException, ForbiddenException, InvalidRequestException, StorageException {

    protected SnapWeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}
