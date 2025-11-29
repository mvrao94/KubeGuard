package io.github.mvrao94.kubeguard.integration.nvd;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for integrating NIST NVD vulnerabilities into KubeGuard rule engine
 */
@Service
public class NvdIntegrationService {
  
  private static final Logger logger = LoggerFactory.getLogger(NvdIntegrationService.class);
  
  @Autowired
  private NvdClient nvdClient;
  
  @Autowired
  private NvdRuleConverter ruleConverter;
  
  @Autowired
  private RuleRegistry ruleRegistry;
  
  @Value("${kubeguard.nvd.sync-enabled:false}")
  private boolean syncEnabled;
  
  @Value("${kubeguard.nvd.results-per-page:20}")
  private int resultsPerPage;
  
  /**
   * Manually sync vulnerabilities from NVD
   */
  public int syncVulnerabilities() {
    if (!nvdClient.isEnabled()) {
      logger.info("NVD integration is disabled");
      return 0;
    }
    
    logger.info("Starting NVD vulnerability sync...");
    
    try {
      // Fetch Kubernetes-related vulnerabilities
      List<NvdVulnerability> vulnerabilities = 
          nvdClient.searchKubernetesVulnerabilities(resultsPerPage);
      
      logger.info("Fetched {} vulnerabilities from NVD", vulnerabilities.size());
      
      // Convert to security rules
      List<SecurityRule> rules = ruleConverter.convertToRules(vulnerabilities);
      
      logger.info("Converted {} vulnerabilities to security rules", rules.size());
      
      // Register rules
      ruleRegistry.registerRules(rules);
      
      logger.info("Successfully synced {} NVD rules", rules.size());
      
      return rules.size();
      
    } catch (Exception e) {
      logger.error("Error syncing NVD vulnerabilities: {}", e.getMessage(), e);
      return 0;
    }
  }
  
  /**
   * Sync recent vulnerabilities (last 30 days)
   */
  public int syncRecentVulnerabilities(int days) {
    if (!nvdClient.isEnabled()) {
      return 0;
    }
    
    logger.info("Syncing vulnerabilities from last {} days...", days);
    
    try {
      List<NvdVulnerability> vulnerabilities = 
          nvdClient.getRecentVulnerabilities(days, resultsPerPage);
      
      List<SecurityRule> rules = ruleConverter.convertToRules(vulnerabilities);
      ruleRegistry.registerRules(rules);
      
      logger.info("Synced {} recent NVD rules", rules.size());
      
      return rules.size();
      
    } catch (Exception e) {
      logger.error("Error syncing recent vulnerabilities: {}", e.getMessage());
      return 0;
    }
  }
  
  /**
   * Scheduled sync (runs daily at 2 AM if enabled)
   */
  @Scheduled(cron = "${kubeguard.nvd.sync-cron:0 0 2 * * ?}")
  public void scheduledSync() {
    if (!syncEnabled || !nvdClient.isEnabled()) {
      return;
    }
    
    logger.info("Running scheduled NVD sync...");
    syncRecentVulnerabilities(7); // Last 7 days
  }
  
  /**
   * Get sync statistics
   */
  public NvdSyncStats getStats() {
    long nvdRuleCount = ruleRegistry.getAllRules().stream()
        .filter(rule -> rule.getMetadata().getId().startsWith("NVD-"))
        .count();
    
    return new NvdSyncStats(
        nvdClient.isEnabled(),
        syncEnabled,
        nvdRuleCount
    );
  }
  
  public record NvdSyncStats(
      boolean nvdEnabled,
      boolean syncEnabled,
      long nvdRuleCount
  ) {}
}
