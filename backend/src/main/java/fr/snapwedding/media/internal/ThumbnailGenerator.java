package fr.snapwedding.media.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Option "miniatures asynchrones" : fabrique une petite version JPEG d'une photo (400 px de côté maximum).
 *
 * Seulement le JDK (ImageIO), aucune bibliothèque en plus. ImageIO lit JPEG, PNG et GIF ;
 * il ne sait pas lire HEIC ni WebP : dans ce cas on renvoie vide et la galerie affiche l'original.
 */
@Slf4j
@Component
class ThumbnailGenerator {

    static final int MAX_SIDE = 400;

    /** @return le JPEG de la miniature, ou vide si le format n'est pas lisible. */
    Optional<byte[]> toJpeg(byte[] original) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
            if (source == null) {
                return Optional.empty();
            }
            BufferedImage thumbnail = rotate(scale(source), ExifOrientation.of(original));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, "jpg", out);
            return Optional.of(out.toByteArray());
        } catch (IOException | RuntimeException e) {
            log.warn("Thumbnail not generated", e);
            return Optional.empty();
        }
    }

    /** Réduit sans déformer : le plus grand côté passe à MAX_SIDE (jamais d'agrandissement). */
    private static BufferedImage scale(BufferedImage source) {
        double ratio = Math.min(1.0, (double) MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(source.getHeight() * ratio));

        // TYPE_INT_RGB : le JPEG n'a pas de transparence, on pose donc l'image sur un fond blanc
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.drawImage(source, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return result;
    }

    /**
     * Les téléphones enregistrent souvent la photo "couchée" et notent dans l'EXIF comment la tourner.
     * ImageIO ignore cette information : on applique nous-mêmes la rotation (cas 3, 6 et 8).
     */
    static BufferedImage rotate(BufferedImage source, int orientation) {
        int quarterTurns = switch (orientation) {
            case 6 -> 1; // 90° dans le sens des aiguilles d'une montre
            case 3 -> 2; // 180°
            case 8 -> 3; // 270°
            default -> 0;
        };
        if (quarterTurns == 0) {
            return source;
        }
        int w = source.getWidth();
        int h = source.getHeight();
        boolean swap = quarterTurns % 2 == 1;
        BufferedImage result = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_RGB);
        AffineTransform transform = new AffineTransform();
        switch (quarterTurns) {
            case 1 -> { transform.translate(h, 0); transform.rotate(Math.PI / 2); }
            case 2 -> { transform.translate(w, h); transform.rotate(Math.PI); }
            default -> { transform.translate(0, w); transform.rotate(-Math.PI / 2); }
        }
        Graphics2D g = result.createGraphics();
        try {
            g.drawImage(source, transform, null);
        } finally {
            g.dispose();
        }
        return result;
    }
}
