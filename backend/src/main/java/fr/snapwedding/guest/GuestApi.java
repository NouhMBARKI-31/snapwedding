package fr.snapwedding.guest;

/**
 * Ce que le module guest offre aux autres modules.
 * Le module media s'en sert pour savoir qui dépose une photo et qui a le droit de la supprimer (I5).
 */
public interface GuestApi {

    /** L'invité authentifié par son jeton pour la requête en cours, sinon ForbiddenException. */
    GuestView current();
}
