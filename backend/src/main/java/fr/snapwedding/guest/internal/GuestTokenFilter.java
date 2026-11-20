package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventApi;
import fr.snapwedding.event.EventView;
import fr.snapwedding.shared.NotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authentifie l'invité à partir du header X-Guest-Token, pour les seules routes /api/guest/**.
 *
 * Le jeton est refusé (401) s'il est inconnu, ou si son événement n'est plus OUVERT
 * (fermé par l'organisateur ou suspendu par le super-admin). La vérification du statut
 * est faite à CHAQUE requête : la fermeture prend effet immédiatement.
 *
 * Pas de @Component : sinon Spring Boot l'enregistrerait aussi sur TOUTES les URL.
 * Il est créé dans GuestSecurityConfig et ne vit que dans la chaîne des invités.
 */
@RequiredArgsConstructor
class GuestTokenFilter extends OncePerRequestFilter {

    static final String HEADER = "X-Guest-Token";

    private final GuestRepository guests;
    private final GuestTokens tokens;
    private final EventApi events;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader(HEADER);
        if (token == null || token.isBlank()) {
            chain.doFilter(request, response); // pas de jeton : la règle "authenticated" renverra 401
            return;
        }

        Optional<Guest> guest = guests.findByTokenHash(tokens.hash(token));
        if (guest.isEmpty()) {
            reject(response, "Invalid guest token");
            return;
        }

        EventView event;
        try {
            event = events.get(guest.get().getEventId());
        } catch (NotFoundException e) {
            reject(response, "Event not found"); // événement supprimé, invités pas encore nettoyés
            return;
        }
        if (!event.isOpen()) {
            reject(response, "Event is " + event.status().name().toLowerCase());
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new GuestAuthentication(guest.get().toView()));
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
