package com.conductor.shared.auth;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Platform Web Security Configuration. Enforces stateless authentication, custom security headers,
 * CORS mappings, and executes dynamic Tenant context resolution filters.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

  private final JwtDecoder jwtDecoder;
  private final SecurityMetrics securityMetrics;

  public WebSecurityConfig(JwtDecoder jwtDecoder, SecurityMetrics securityMetrics) {
    this.jwtDecoder = jwtDecoder;
    this.securityMetrics = securityMetrics;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    TenantSecurityFilter tenantSecurityFilter =
        new TenantSecurityFilter(new HeaderAndJwtTenantContextResolver());
    RateLimitingFilter rateLimitingFilter = new RateLimitingFilter();
    ConductorAuthenticationEntryPoint authenticationEntryPoint =
        new ConductorAuthenticationEntryPoint();
    ConductorAccessDeniedHandler accessDeniedHandler =
        new ConductorAccessDeniedHandler(securityMetrics);

    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(
            headers ->
                headers
                    .frameOptions(frame -> frame.deny())
                    .xssProtection(
                        xss ->
                            xss.headerValue(
                                XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/healthz",
                        "/metrics",
                        "/actuator/health",
                        "/api/v1/auth/login",
                        "/webhooks/whatsapp",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(
                                    new KeycloakJwtAuthenticationConverter()))
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .addFilterBefore(rateLimitingFilter, BearerTokenAuthenticationFilter.class)
        .addFilterAfter(tenantSecurityFilter, BearerTokenAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of("*"));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "X-Tenant-ID", "X-Correlation-ID"));
    configuration.setExposedHeaders(List.of("X-Correlation-ID"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
