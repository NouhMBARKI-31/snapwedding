# SnapWedding

[![CI](https://github.com/NouhMBARKI-31/snapwedding/actions/workflows/ci.yml/badge.svg)](https://github.com/NouhMBARKI-31/snapwedding/actions/workflows/ci.yml)

Album photo d'événement par QR code. L'organisateur crée son événement et ses albums, puis il imprime le QR code. Les invités déposent ensuite leurs photos depuis leur téléphone, sans créer de compte.

| | |
|---|---|
| **Auteur** | Nouh MBARKI |
| **Format** | Seul : socle complet + 2 options |
| **Stack** | Java 25, Spring Boot 4, Spring Modulith 2, PostgreSQL 18, RustFS (S3), React + Vite + shadcn/ui |

## Lancer le projet (moins de 5 minutes)

**Prérequis**
- Docker Desktop
- Java 25 (IntelliJ peut le télécharger)
- Node.js 20 ou plus
- un Client ID Google OAuth de type « Application Web », avec les origines JavaScript autorisées `http://localhost` et `http://localhost:5173`

```bash
# 1. Base de données + stockage S3
docker compose up -d

# 2. Back : http://localhost:8080 (Swagger : http://localhost:8080/swagger-ui.html)
cd backend
cp .env.example .env          # puis renseigner GOOGLE_CLIENT_ID
./mvnw spring-boot:run        # Windows : mvnw.cmd spring-boot:run

# 3. Front : http://localhost:5173
cd ../frontend
cp .env.example .env          # renseigner VITE_GOOGLE_CLIENT_ID (le même Client ID)
npm install
npm run dev
```

**Tests :** `./mvnw test`. Il y a 30 tests. Docker doit tourner, car le test d'intégration utilise Testcontainers.

**Devenir super-admin**
1. Connectez-vous une fois avec Google et créez un événement.
2. Récupérez votre identifiant Google (le claim `sub`) :
   ```bash
   docker exec -it snapwedding-postgres psql -U snap -d snapwedding -c "select distinct organizer_id from events;"
   ```
3. Mettez-le dans `SUPER_ADMINS` dans `backend/.env` (plusieurs identifiants séparés par des virgules), puis redémarrez le back.

**Aucun secret dans le dépôt :** les fichiers `.env` sont ignorés par git, et seuls les modèles `.env.example` sont commités.

## Parcours de démo

1. **Organisateur :** connexion Google, puis création d'un événement et d'un album, puis affichage du QR code.
2. **Invité :** ouverture du lien du QR code dans une fenêtre privée (en vue mobile), saisie d'un pseudo, puis dépôt de photos.
3. **Organisateur :** il voit les photos avec le pseudo de chaque invité. Il peut masquer une photo, et elle disparaît alors chez les invités.
4. **Organisateur :** il ferme l'événement. Plus aucun dépôt n'est possible, et les jetons des invités ne marchent plus.
5. **Super-admin :** il voit tous les événements avec leurs statistiques, et il peut suspendre un événement.

La collection **Bruno** (`bruno/`) rejoue tout ce parcours côté API :
1. Ouvrez le dossier dans Bruno, puis choisissez l'environnement `local`.
2. Collez un jeton Google dans `googleToken` : c'est la valeur `snapwedding_token` dans le stockage local du navigateur (F12 → Application → Local Storage).
3. Lancez la collection.

## Architecture : Spring Modulith

| Module | Rôle | Dépend de |
|---|---|---|
| `shared` | Sécurité organisateur (JWT Google), erreurs, propriétés | — |
| `event` | Événements, code public, QR code, fermeture, suspension | shared |
| `guest` | Invités sans compte, jeton d'invité, 2ᵉ chaîne de sécurité | event |
| `album` | Albums d'un événement | event, guest |
| `media` | Photos (S3), vérification Tika, galerie, modération, miniatures | album, event, guest |
| `admin` | Super-admin : tous les événements, statistiques, suspension | event, media |

- **Vitrine et cuisine.** Chaque module expose une API minimale dans son package racine : `EventApi`, `GuestApi`, `AlbumApi`, `MediaApi`, et des records en lecture seule. Tout le reste est dans `internal`, inaccessible aux autres modules.
- **Deux façons de communiquer :**
  - une **interface**, quand un module a besoin d'une réponse tout de suite. Par exemple, `media` demande à `event` si l'événement est ouvert ;
  - un **événement applicatif**, quand un module annonce un fait sans savoir qui réagit. `EventDeleted` déclenche le ménage dans album, media et guest ; `AlbumDeleted` déclenche celui de media ; `MediaUploaded` déclenche la miniature.
- **`ModularityTest`** vérifie la structure avec `ApplicationModules.verify()`. Il échoue s'il y a un cycle, ou si un module utilise la partie `internal` d'un autre. Il génère aussi la documentation, recopiée dans [`docs/modulith`](docs/modulith).
- **Multi-tenant :** le tenant est le claim `sub` du JWT Google. Une ressource d'un autre organisateur répond **404**, et non 403 : on ne révèle même pas qu'elle existe. `IsolationIntegrationTest` le prouve avec une vraie base PostgreSQL.

## Choix du jeton d'invité : jeton opaque haché

Après avoir saisi son pseudo, l'invité reçoit un jeton aléatoire de 256 bits, généré par `SecureRandom`. Il le renvoie ensuite dans le header `X-Guest-Token`. En base, on ne garde que son empreinte SHA-256.

- **Révocable immédiatement :** à chaque requête, on retrouve l'invité et on vérifie le statut de son événement. Dès que l'événement est fermé ou suspendu, le jeton ne marche plus. Un JWT signé, lui, resterait valable jusqu'à son expiration.
- **Rattaché à un seul événement :** l'événement vient de la ligne en base, jamais de l'URL.
- **Fuite de la base sans conséquence :** comme un mot de passe, le jeton n'est stocké que haché.
- **Coût négligeable :** une lecture en base par requête invité, sur une colonne indexée unique.

## Options réalisées (2)

### 1. Intégration continue (GitHub Actions)

Le fichier [`.github/workflows/ci.yml`](.github/workflows/ci.yml) s'exécute à chaque push et à chaque pull request :
- **back :** `./mvnw verify` lance les tests unitaires, `ModularityTest` et le test d'intégration Testcontainers sur un vrai PostgreSQL ;
- **front :** vérification des types TypeScript, puis build Vite.

Le badge en haut de ce README montre l'état du dernier passage.

### 2. Miniatures asynchrones

Quand un invité dépose une photo, `media` publie `MediaUploaded`. Le `ThumbnailListener` l'écoute avec `@ApplicationModuleListener`, ce qui apporte trois garanties :
- **après le commit**, donc la photo existe bien en base ;
- **dans un autre thread**, donc l'invité reçoit sa réponse sans attendre ;
- **avec le registre de publication JPA**, donc si le serveur s'arrête pendant le calcul, l'événement est rejoué au redémarrage.

La miniature mesure 400 px de côté maximum. Elle est calculée avec le JDK seul (ImageIO), en tenant compte de l'orientation EXIF des photos de téléphone, puis stockée à côté de l'original dans S3.

Les galeries chargent `/thumbnail`, et un clic ouvre l'original. Tant que la miniature n'est pas prête, ou pour les formats qu'ImageIO ne lit pas (HEIC, WebP), la route renvoie l'original.
