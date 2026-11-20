package fr.snapwedding.shared;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Identité de l'organisateur connecté = son tenant.
 *
 * Le tenant est le claim "sub" du JWT Google : un identifiant stable et unique par compte Google.
 * On ne le lit JAMAIS dans la requête (paramètre, header...) : un client pourrait le falsifier.
 */
public final class CurrentOrganizer {

    private CurrentOrganizer() {
    }

    public static String id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getToken().getSubject();
        }
        throw new ForbiddenException("Organizer authentication required");
    }
}
