package fr.snapwedding.shared.internal;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger : http://localhost:8080/swagger-ui.html
 * Deux schémas d'authentification, comme l'API : le JWT Google et le jeton d'invité.
 */
@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI snapWeddingOpenApi() {
        return new OpenAPI()
                .info(new Info().title("SnapWedding API").version("1.0")
                        .description("Album photo d'événement par QR code"))
                .components(new Components()
                        .addSecuritySchemes("googleJwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .description("ID token Google de l'organisateur ou du super-admin"))
                        .addSecuritySchemes("guestToken", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-Guest-Token")
                                .description("Jeton d'invité reçu en rejoignant l'événement")));
    }
}
