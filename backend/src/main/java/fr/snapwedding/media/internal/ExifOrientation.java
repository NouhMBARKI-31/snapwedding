package fr.snapwedding.media.internal;

/**
 * Lit le tag EXIF "Orientation" (0x0112) d'un JPEG, sans bibliothèque.
 *
 * Un JPEG est une suite de blocs : FF D8, puis des blocs "FF xx + longueur". Le bloc APP1 (FF E1)
 * qui commence par "Exif" contient un mini-fichier TIFF : un en-tête (II = petit-boutiste,
 * MM = gros-boutiste), puis une table d'entrées de 12 octets. On y cherche le tag 0x0112.
 */
final class ExifOrientation {

    static final int NORMAL = 1;

    private ExifOrientation() {
    }

    static int of(byte[] jpeg) {
        try {
            if (u8(jpeg, 0) != 0xFF || u8(jpeg, 1) != 0xD8) {
                return NORMAL; // pas un JPEG
            }
            int pos = 2;
            while (pos + 4 <= jpeg.length && u8(jpeg, pos) == 0xFF) {
                int marker = u8(jpeg, pos + 1);
                if (marker == 0xDA || marker == 0xD9) {
                    return NORMAL; // début de l'image : plus de métadonnées après
                }
                int length = u16(jpeg, pos + 2, false);
                if (marker == 0xE1 && isExifHeader(jpeg, pos + 4)) {
                    return readOrientation(jpeg, pos + 10);
                }
                pos += 2 + length;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // fichier tronqué ou EXIF abîmé : on ne tourne pas
        }
        return NORMAL;
    }

    private static boolean isExifHeader(byte[] b, int pos) {
        return u8(b, pos) == 'E' && u8(b, pos + 1) == 'x' && u8(b, pos + 2) == 'i' && u8(b, pos + 3) == 'f'
                && u8(b, pos + 4) == 0 && u8(b, pos + 5) == 0;
    }

    private static int readOrientation(byte[] b, int tiff) {
        boolean little = u8(b, tiff) == 'I';
        int ifd = tiff + u32(b, tiff + 4, little);
        int entries = u16(b, ifd, little);
        for (int i = 0; i < entries; i++) {
            int entry = ifd + 2 + i * 12;
            if (u16(b, entry, little) == 0x0112) {
                int value = u16(b, entry + 8, little);
                return value >= 1 && value <= 8 ? value : NORMAL;
            }
        }
        return NORMAL;
    }

    private static int u8(byte[] b, int pos) {
        return b[pos] & 0xFF;
    }

    private static int u16(byte[] b, int pos, boolean little) {
        return little ? u8(b, pos) | u8(b, pos + 1) << 8 : u8(b, pos) << 8 | u8(b, pos + 1);
    }

    private static int u32(byte[] b, int pos, boolean little) {
        return little
                ? u16(b, pos, true) | u16(b, pos + 2, true) << 16
                : u16(b, pos, false) << 16 | u16(b, pos + 2, false);
    }
}
