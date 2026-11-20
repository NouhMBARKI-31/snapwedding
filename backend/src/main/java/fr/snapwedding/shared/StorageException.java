package fr.snapwedding.shared;

/** 500 : erreur du stockage S3 (RustFS). Le détail technique part dans les logs, pas au client. */
public final class StorageException extends SnapWeddingException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
