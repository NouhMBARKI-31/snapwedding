package fr.snapwedding.media.internal;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/** Option "miniatures" : tests unitaires purs, sans Spring ni S3. */
class ThumbnailGeneratorTest {

    private final ThumbnailGenerator generator = new ThumbnailGenerator();

    @Test
    void aBigPhotoBecomesASmallJpegWithTheSameProportions() throws IOException {
        byte[] png = png(2000, 1000);

        byte[] thumbnail = generator.toJpeg(png).orElseThrow();

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(thumbnail));
        assertThat(result.getWidth()).isEqualTo(400);
        assertThat(result.getHeight()).isEqualTo(200);
    }

    @Test
    void anUnreadableFileGivesNoThumbnail() {
        assertThat(generator.toJpeg("ceci n'est pas une image".getBytes())).isEmpty();
    }

    @Test
    void aPhotoTakenSidewaysIsTurnedUpright() {
        BufferedImage landscape = new BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB);
        landscape.setRGB(0, 0, 0xFF0000); // un point rouge en haut à gauche

        BufferedImage upright = ThumbnailGenerator.rotate(landscape, 6); // EXIF 6 : tourner de 90°

        assertThat(upright.getWidth()).isEqualTo(2);
        assertThat(upright.getHeight()).isEqualTo(4);
        assertThat(upright.getRGB(1, 0) & 0xFFFFFF).isEqualTo(0xFF0000); // il est passé en haut à droite
    }

    @Test
    void theExifOrientationIsReadFromTheJpegHeader() throws IOException {
        byte[] tiff = {'M', 'M', 0, 0x2A, 0, 0, 0, 8, 0, 1, 0x01, 0x12, 0, 3, 0, 0, 0, 1, 0, 6, 0, 0, 0, 0, 0, 0};
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        jpeg.write(new byte[]{(byte) 0xFF, (byte) 0xD8});
        int length = 2 + 6 + tiff.length;
        jpeg.write(new byte[]{(byte) 0xFF, (byte) 0xE1, (byte) (length >> 8), (byte) length, 'E', 'x', 'i', 'f', 0, 0});
        jpeg.write(tiff);
        jpeg.write(new byte[]{(byte) 0xFF, (byte) 0xDA});

        assertThat(ExifOrientation.of(jpeg.toByteArray())).isEqualTo(6);
        assertThat(ExifOrientation.of(png(10, 10))).isEqualTo(ExifOrientation.NORMAL);
    }

    private static byte[] png(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", out);
        return out.toByteArray();
    }
}
