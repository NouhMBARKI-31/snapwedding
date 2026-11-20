package fr.snapwedding.media.internal;

import fr.snapwedding.shared.InvalidRequestException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Set;

/**
 * Vérifie le type RÉEL d'un fichier avec Apache Tika.
 *
 * Le nom (photo.jpg) et le Content-Type sont envoyés par le client : il peut mentir.
 * Tika lit les premiers octets du fichier (la "signature", ou magic number : FF D8 FF pour un JPEG,
 * 89 50 4E 47 pour un PNG...). Un script renommé en .jpg est donc refusé.
 */
@Component
class FileTypeChecker {

    static final Set<String> ALLOWED = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/heic", "image/heif");

    private final Tika tika = new Tika();

    /** @return le type détecté, s'il est autorisé ; sinon InvalidRequestException (400). */
    String detectAllowedType(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            String type = tika.detect(in);
            if (!ALLOWED.contains(type)) {
                throw new InvalidRequestException("Unsupported file type: " + type);
            }
            return type;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
