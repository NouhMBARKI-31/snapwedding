package fr.snapwedding.shared.internal;

import fr.snapwedding.shared.SnapWeddingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforme un JWT Google en utilisateur Spring Security, avec ses rôles.
 *
 * Tout compte Google valide est ROLE_ORGANIZER. Il devient aussi ROLE_SUPER_ADMIN
 * si son "sub" figure dans snapwedding.super-admins : le rôle vient de NOTRE configuration,
 * jamais d'un claim que le client pourrait choisir.
 *
 * Pas de @Component : créé dans OrganizerSecurityConfig (un bean Converter serait aussi
 * enregistré par Spring MVC comme convertisseur de paramètres, ce qui n'a pas de sens ici).
 */
@RequiredArgsConstructor
class SuperAdminRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SnapWeddingProperties props;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ORGANIZER"));
        if (props.superAdmins().contains(jwt.getSubject())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        }
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
