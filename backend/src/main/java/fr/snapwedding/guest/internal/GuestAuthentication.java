package fr.snapwedding.guest.internal;

import fr.snapwedding.guest.GuestView;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * L'"utilisateur" Spring Security d'une requête invité : son principal est le GuestView,
 * son seul rôle ROLE_GUEST. Il n'a donc accès à aucune route organisateur ou admin.
 */
class GuestAuthentication extends AbstractAuthenticationToken {

    private final GuestView guest;

    GuestAuthentication(GuestView guest) {
        super(AuthorityUtils.createAuthorityList("ROLE_GUEST"));
        this.guest = guest;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return ""; // on ne garde pas le jeton en mémoire plus que nécessaire
    }

    @Override
    public GuestView getPrincipal() {
        return guest;
    }
}
