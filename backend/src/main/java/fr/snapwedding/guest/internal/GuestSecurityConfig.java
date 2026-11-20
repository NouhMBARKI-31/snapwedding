package fr.snapwedding.guest.internal;

import fr.snapwedding.event.EventApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * 2e monde d'authentification : la chaîne des INVITÉS.
 *
 * @Order(1) : elle passe avant la chaîne organisateur, mais seulement pour /api/guest/**
 * (securityMatcher). Sur ces URL, un JWT Google ne sert à rien : seul le jeton d'invité compte.
 * Toutes les autres URL tombent dans la chaîne organisateur (module shared).
 */
@Configuration
class GuestSecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain guestSecurityFilterChain(HttpSecurity http, GuestRepository guests,
                                                 GuestTokens tokens, EventApi events) {
        http
                .securityMatcher("/api/guest/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new GuestTokenFilter(guests, tokens, events), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("GUEST"))
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}
