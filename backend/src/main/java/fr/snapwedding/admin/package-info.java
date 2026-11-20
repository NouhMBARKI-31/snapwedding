/**
 * Module "admin" : l'espace du super-admin, le seul acteur qui traverse les tenants.
 * Il n'a pas de base de données à lui : il lit les événements (EventApi) et les statistiques
 * des photos (MediaApi).
 */
@org.springframework.modulith.ApplicationModule(displayName = "Admin")
package fr.snapwedding.admin;
