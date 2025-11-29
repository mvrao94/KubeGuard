package io.github.mvrao94.kubeguard.config;

import io.github.mvrao94.kubeguard.security.ApiKeyAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  
  private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
  
  public SecurityConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
    this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers("/actuator/info")
                    .permitAll()
                    .requestMatchers("/api-docs/**")
                    .authenticated() // Require auth for API docs
                    .requestMatchers("/swagger-ui/**")
                    .authenticated() // Require auth for Swagger UI
                    .requestMatchers("/swagger-ui.html")
                    .authenticated()
                    .requestMatchers("/webjars/**")
                    .authenticated()
                    .requestMatchers("/api/v1/**")
                    .authenticated() // ALL API endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .headers(
            headers ->
                headers
                    .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .referrerPolicy(
                        header ->
                            header.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy
                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .httpStrictTransportSecurity(
                        hstsConfig ->
                            hstsConfig.maxAgeInSeconds(31536000L).includeSubDomains(true)));

    return http.build();
  }
}
