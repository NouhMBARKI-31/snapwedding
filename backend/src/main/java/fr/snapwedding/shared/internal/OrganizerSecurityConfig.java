package fr.snapwedding.shared.internal;

import fr.snapwedding.shared.SnapWeddingProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Chaîne de sécurité "organisateur / super-admin" : JWT Google (Resource Server).
 *
 * Elle passe EN SECOND (@Order(2)) : la chaîne des invités (module guest, @Order(1))
 * prend d'abord toutes les URL /api/guest/**. Tout le reste arrive ici.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class OrganizerSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain organizerSecurityFilterChain(HttpSecurity http, SnapWeddingProperties props) {
        SuperAdminRoleConverter roleConverter = new SuperAdminRoleConverter(props);
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // API stateless avec Bearer token : pas de cookie, pas de CSRF
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        // Pages publiques : infos d'un événement par son code et "rejoindre" (invité sans compte)
                        .requestMatchers("/api/public/**").permitAll()
                        // Seul le super-admin traverse les tenants
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(roleConverter)));

        return http.build();
    }

    /** Utilisée par les deux chaînes (organisateur et invité) via .cors(Customizer.withDefaults()). */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(SnapWeddingProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(props.frontUrl()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
