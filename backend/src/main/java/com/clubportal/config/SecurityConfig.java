package com.clubportal.config;

import com.clubportal.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final RequestMatcher PUBLIC_DEBUG_MATCHER = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/debug/ping", HttpMethod.GET.name()),
            new AntPathRequestMatcher("/api/debug/chat-version", HttpMethod.GET.name())
    );

    private final JwtAuthenticationFilter jwtFilter;
    private final AppSecurityProperties appSecurityProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          AppSecurityProperties appSecurityProperties) {
        this.jwtFilter = jwtFilter;
        this.appSecurityProperties = appSecurityProperties;
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
    public SecurityFilterChain publicDebugSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(PUBLIC_DEBUG_MATCHER)
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        Http403ForbiddenEntryPoint forbiddenEntryPoint = new Http403ForbiddenEntryPoint();
        AccessDeniedHandlerImpl accessDeniedHandler = new AccessDeniedHandlerImpl();

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
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
                .requestMatchers("/api/my/clubs/*/chat/**").hasAnyRole("CLUB", "ADMIN")
                .requestMatchers("/api/clubs/*/chat/**").authenticated()
                .requestMatchers("/api/debug/**").denyAll()
                .requestMatchers(HttpMethod.GET, "/api/clubs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.PUT, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.PATCH, "/api/clubs/**").hasRole("CLUB")
                .requestMatchers(HttpMethod.DELETE, "/api/clubs/**").hasRole("CLUB")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    logDebugBlockIfNeeded("SecurityConfig", request, authException == null ? "authentication" : authException.getClass().getSimpleName());
                    forbiddenEntryPoint.commence(request, response, authException);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    logDebugBlockIfNeeded("SecurityConfig", request, accessDeniedException == null ? "access denied" : accessDeniedException.getClass().getSimpleName());
                    accessDeniedHandler.handle(request, response, accessDeniedException);
                })
            )
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void logDebugBlockIfNeeded(String source, HttpServletRequest request, String reason) {
        if (request == null) {
            return;
        }
        String path = request.getRequestURI();
        if (!path.startsWith("/api/debug/")) {
            return;
        }
        log.warn("[CLUB_CHAT_DEBUG] debug endpoint blocked at {}: path={}, reason={}",
                source,
                path,
                reason);
    }
}
