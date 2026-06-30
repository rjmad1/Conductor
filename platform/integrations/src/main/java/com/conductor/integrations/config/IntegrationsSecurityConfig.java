package com.conductor.integrations.config;

import com.conductor.shared.auth.ConductorAccessDeniedHandler;
import com.conductor.shared.auth.ConductorAuthenticationEntryPoint;
import com.conductor.shared.auth.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SuppressWarnings("null")
public class IntegrationsSecurityConfig {

  private final ConductorAuthenticationEntryPoint authenticationEntryPoint;
  private final ConductorAccessDeniedHandler accessDeniedHandler;

  public IntegrationsSecurityConfig(
      ConductorAuthenticationEntryPoint authenticationEntryPoint,
      ConductorAccessDeniedHandler accessDeniedHandler) {
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.accessDeniedHandler = accessDeniedHandler;
  }

  @Bean
  @Primary
  public SecurityFilterChain integrationsSecurityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/v1/integrations/webhooks/ingress/**")
                    .permitAll()
                    .requestMatchers(
                        "/healthz",
                        "/metrics",
                        "/actuator/health",
                        "/api/v1/integrations/oauth/callback",
                        "/api/v1/integrations/webhooks/whatsapp/**")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(
                    jwt ->
                        jwt.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler));

    return http.build();
  }
}
