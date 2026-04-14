package com.clubportal.config;

import com.clubportal.security.JwtAuthenticationFilter;
import com.clubportal.service.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final AppSecurityProperties appSecurityProperties;
    private final SecurityEventService securityEventService;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          AppSecurityProperties appSecurityProperties,
                          SecurityEventService securityEventService) {
        this.jwtFilter = jwtFilter;
        this.appSecurityProperties = appSecurityProperties;
        this.securityEventService = securityEventService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> allowedOrigins = appSecurityProperties.getCors().getAllowedOriginPatterns().stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Http403ForbiddenEntryPoint forbiddenEntryPoint = new Http403ForbiddenEntryPoint();
        AccessDeniedHandlerImpl accessDeniedHandler = new AccessDeniedHandlerImpl();

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(buildContentSecurityPolicy()))
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .frameOptions(frame -> frame.sameOrigin())
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/login", "/api/register").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/config").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payments/webhook/stripe").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/public/users/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clubs/*/community-questions").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/clubs/*/community-questions/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/clubs/*/community-questions/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/clubs/*/community-questions/*/answers").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/clubs/*/community-questions/*/answers/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/clubs/*/community-questions/*/answers/*").authenticated()
                .requestMatchers("/api/my/clubs/*/chat/**").hasAnyRole("CLUB", "ADMIN")
                .requestMatchers("/api/clubs/*/chat/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/clubs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.PUT, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.PATCH, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.DELETE, "/api/clubs/**").hasRole("CLUB")
                .anyRequest().authenticated()
            )
                .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    if (Boolean.TRUE.equals(request.getAttribute(JwtAuthenticationFilter.AUTH_FAILURE_ATTRIBUTE))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    securityEventService.recordForEmail(request, "AUTHENTICATION_REQUIRED", "WARN", resolvePrincipalEmail(request), java.util.Map.of(
                            "reason", authException == null ? "authentication_required" : authException.getClass().getSimpleName()
                    ));
                    forbiddenEntryPoint.commence(request, response, authException);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    securityEventService.recordForEmail(request, "ACCESS_DENIED", "HIGH", resolvePrincipalEmail(request), java.util.Map.of(
                            "reason", accessDeniedException == null ? "access_denied" : accessDeniedException.getClass().getSimpleName()
                    ));
                    accessDeniedHandler.handle(request, response, accessDeniedException);
                })
            )
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static String buildContentSecurityPolicy() {
        return String.join("; ",
                "default-src 'self'",
                "base-uri 'self'",
                "object-src 'none'",
                "frame-ancestors 'self'",
                "img-src 'self' data: blob: https://maps.gstatic.com https://maps.googleapis.com https://*.googleusercontent.com https://www.google.com https://www.gstatic.com",
                "font-src 'self' data: https://fonts.gstatic.com",
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
                "script-src 'self' 'unsafe-inline' https://accounts.google.com https://maps.googleapis.com",
                "connect-src 'self' https://accounts.google.com https://maps.googleapis.com https://api.stripe.com",
                "frame-src 'self' https://accounts.google.com https://js.stripe.com",
                "form-action 'self' https://accounts.google.com"
        );
    }

    private static String resolvePrincipalEmail(HttpServletRequest request) {
        if (request == null || request.getUserPrincipal() == null) {
            return "";
        }
        String name = request.getUserPrincipal().getName();
        return name == null ? "" : name.trim().toLowerCase();
    }
}
