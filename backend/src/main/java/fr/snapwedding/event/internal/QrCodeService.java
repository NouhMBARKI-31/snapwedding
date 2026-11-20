package fr.snapwedding.event.internal;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import fr.snapwedding.shared.SnapWeddingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Génère le QR code (PNG) d'un événement avec ZXing.
 * Il contient l'URL de la page invité du front : <front-url>/e/<code public>.
 */
@Service
@RequiredArgsConstructor
class QrCodeService {

    private static final int SIZE_PX = 512;

    private final SnapWeddingProperties props;

    String guestUrl(String publicCode) {
        return props.frontUrl() + "/e/" + publicCode;
    }

    byte[] png(String publicCode) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(guestUrl(publicCode), BarcodeFormat.QR_CODE,
                    SIZE_PX, SIZE_PX, Map.of(EncodeHintType.MARGIN, 2));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException e) {
            throw new IllegalStateException("QR code generation failed", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
