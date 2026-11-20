package fr.snapwedding.media.internal;

import java.time.Instant;

/**
 * DTO d'une photo : aucune entité JPA ne sort de l'API.
 * Pas de s3Key : le stockage est un détail interne, l'image se lit via /content.
 *
 * @param mine vrai si l'invité qui demande l'a déposée (pour afficher "supprimer"), toujours faux côté organisateur
 */
public record MediaResponse(Long id, Long albumId, String guestPseudo, String originalFilename, String contentType,
                     long size, boolean hidden, boolean mine, Instant createdAt) {

    static MediaResponse forOrganizer(Media m) {
        return new MediaResponse(m.getId(), m.getAlbumId(), m.getGuestPseudo(), m.getOriginalFilename(),
                m.getContentType(), m.getSize(), m.isHidden(), false, m.getCreatedAt());
    }

    static MediaResponse forGuest(Media m, Long guestId) {
        return new MediaResponse(m.getId(), m.getAlbumId(), m.getGuestPseudo(), m.getOriginalFilename(),
                m.getContentType(), m.getSize(), false, m.isOwnedByGuest(guestId), m.getCreatedAt());
    }
}
