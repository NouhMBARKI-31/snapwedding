package fr.snapwedding.media.internal;

import fr.snapwedding.shared.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;
import java.util.Collection;

/** Accès au stockage S3. Les exceptions du SDK AWS sont traduites en StorageException. */
@Slf4j
@Component
@RequiredArgsConstructor
class MediaStorage {

    private final S3Client s3;
    private final S3Properties props;

    void put(String key, InputStream content, long size, String contentType) {
        try {
            s3.putObject(b -> b.bucket(props.bucket()).key(key).contentType(contentType),
                    RequestBody.fromInputStream(content, size));
        } catch (SdkException e) {
            throw new StorageException("Upload failed for " + key, e);
        }
    }

    InputStream get(String key) {
        try {
            return s3.getObject(b -> b.bucket(props.bucket()).key(key));
        } catch (SdkException e) {
            throw new StorageException("Download failed for " + key, e);
        }
    }

    /**
     * Suppression "best effort" : si S3 échoue, on le note dans les logs sans faire échouer
     * l'action de l'utilisateur. Au pire il reste un fichier orphelin (option "nettoyage").
     */
    void deleteQuietly(Collection<String> keys) {
        for (String key : keys) {
            try {
                s3.deleteObject(b -> b.bucket(props.bucket()).key(key));
            } catch (SdkException e) {
                log.warn("Orphan S3 object left behind: {}", key, e);
            }
        }
    }
}
