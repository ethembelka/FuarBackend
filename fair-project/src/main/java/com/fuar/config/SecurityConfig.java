package com.fuar.config;

import com.fuar.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(corsCustomizer -> corsCustomizer.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                    "/api/v1/auth/register",  // Explicitly whitelist the register endpoint
                    "/api/v1/auth/authenticate",  // Explicitly whitelist the authenticate endpoint
                    "/api/v1/auth/**",  // Other auth endpoints
                    "/api/v1/events/speaker/**",  // Konuşmacı etkinlikleri endpoint'i
                    "/ws/**",
                    "/api/v1/ws/**",  // Also allow /api/v1/ws endpoints for WebSocket
                    "/info/**",  // SockJS needs this for info frames
                    "/topic/**",
                    "/queue/**",
                    "/app/**",
                    "/v2/api-docs",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/configuration/ui",
                    "/configuration/security",
                    "/swagger-ui/**",
                    "/webjars/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, 
                    "/api/v1/speakers/all",
                    "/api/v1/speakers/*/profile",
                    "/api/v1/speakers/*/events",
                    "/api/v1/events",
                    "/api/v1/events/all",
                    "/api/v1/events/*",
                    "/api/v1/events/*/details",
                    "/api/v1/events/upcoming",
                    "/api/v1/events/available",
                    "/api/v1/events/search",
                    "/api/v1/events/between",
                    "/api/v1/users/**",
                    "/api/v1/education/user/*",
                    "/api/v1/work-experience/user/*",
                    "/api/v1/publications/user/*",
                    "/uploads/**"  // Allow access to uploaded files
                ).permitAll()
                // Protected endpoints that require authentication
                .requestMatchers(
                    "/api/v1/speakers/**",
                    "/api/v1/events/**",
                    "/api/v1/conversations/**"
                ).authenticated()
                .anyRequest().authenticated();
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> 
                    SecurityContextHolder.clearContext()
                ));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));  // Allow all origins for WebSocket connections
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type",
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Headers",
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Content-Type",
            "Authorization",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Headers"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
