package io.github.mvrao94.kubeguard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * API Key authentication filter - MANDATORY for all API requests
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
  
  private static final String API_KEY_HEADER = "X-API-Key";
  
  @Value("${kubeguard.security.api-key:}")
  private String configuredApiKey;
  
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    
    // Allow health check and actuator endpoints
    String path = request.getRequestURI();
    if (path.startsWith("/actuator/health") || path.startsWith("/actuator/info")) {
      filterChain.doFilter(request, response);
      return;
    }
    
    String apiKey = request.getHeader(API_KEY_HEADER);
    
    if (apiKey == null || apiKey.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Missing API key\",\"message\":\"X-API-Key header is required\"}");
      return;
    }
    
    if (!apiKey.equals(configuredApiKey)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Invalid API key\",\"message\":\"The provided API key is invalid\"}");
      return;
    }
    
    // Set authentication
    UsernamePasswordAuthenticationToken authentication = 
        new UsernamePasswordAuthenticationToken("api-user", null, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    filterChain.doFilter(request, response);
  }
}
