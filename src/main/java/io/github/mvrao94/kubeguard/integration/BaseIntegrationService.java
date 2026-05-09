package io.github.mvrao94.kubeguard.integration;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;

/**
 * Base class for external security framework integration services.
 * Provides the common fetch → convert → register pipeline shared by NVD and MITRE integrations.
 */
public abstract class BaseIntegrationService {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected abstract RuleRegistry getRuleRegistry();

  protected abstract boolean isClientEnabled();

  protected abstract String getIntegrationName();

  /**
   * Executes the common fetch → convert → register pipeline.
   * Subclasses supply the fetch-and-convert step via a supplier lambda.
   *
   * @param rulesSupplier fetches items from the external source and converts them to SecurityRules
   * @param context       a short description used in log messages (e.g. "all techniques", "last 7 days")
   * @return number of rules registered, or 0 on error / disabled
   */
  protected int loadRules(RulesSupplier rulesSupplier, String context) {
    if (!isClientEnabled()) {
      logger.info("{} integration is disabled", getIntegrationName());
      return 0;
    }

    logger.info("Loading {} rules ({})", getIntegrationName(), context);

    try {
      List<SecurityRule> rules = rulesSupplier.get();
      getRuleRegistry().registerRules(rules);
      logger.info("Successfully loaded {} {} rules ({})", rules.size(), getIntegrationName(), context);
      return rules.size();
    } catch (Exception e) {
      logger.error("Error loading {} rules ({}): {}", getIntegrationName(), context, e.getMessage(), e);
      return 0;
    }
  }

  @FunctionalInterface
  public interface RulesSupplier {
    List<SecurityRule> get() throws Exception;
  }
}
