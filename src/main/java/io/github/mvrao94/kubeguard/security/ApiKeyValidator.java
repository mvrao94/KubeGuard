package io.github.mvrao94.kubeguard.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validates that API key is configured on startup
 * FAILS FAST if security is not properly configured
 */
@Component
public class ApiKeyValidator {
  
  private static final Logger logger = LoggerFactory.getLogger(ApiKeyValidator.class);
  
  @Value("${kubeguard.security.api-key:}")
  private String apiKey;
  
  @Value("${kubeguard.security.require-api-key:true}")
  private boolean requireApiKey;
  
  @PostConstruct
  public void validateApiKey() {
    if (!requireApiKey) {
      logger.warn("⚠️  API KEY AUTHENTICATION IS DISABLED - THIS IS INSECURE!");
      logger.warn("⚠️  Set kubeguard.security.require-api-key=true in production");
      return;
    }
    
    if (apiKey == null || apiKey.isEmpty() || apiKey.length() < 32) {
      logger.error("❌ FATAL: API key is not configured or too short!");
      logger.error("❌ Set KUBEGUARD_API_KEY environment variable (minimum 32 characters)");
      logger.error("❌ Generate a secure key: openssl rand -hex 32");
      throw new IllegalStateException(
          "API key must be configured via KUBEGUARD_API_KEY environment variable (minimum 32 characters). " +
          "Generate one with: openssl rand -hex 32"
      );
    }
    
    logger.info("✅ API key authentication is enabled and configured");
    logger.info("✅ All API requests require X-API-Key header");
  }
}
