package io.github.mvrao94.kubeguard.integration.nvd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.github.mvrao94.kubeguard.integration.BaseIntegrationService;
import io.github.mvrao94.kubeguard.rules.RuleRegistry;

/**
 * Service for integrating NIST NVD vulnerabilities into KubeGuard rule engine.
 * The common fetch → convert → register pipeline lives in BaseIntegrationService.
 */
@Service
public class NvdIntegrationService extends BaseIntegrationService {

  @Autowired private NvdClient nvdClient;
  @Autowired private NvdRuleConverter ruleConverter;
  @Autowired private RuleRegistry ruleRegistry;

  @Value("${kubeguard.nvd.sync-enabled:false}")
  private boolean syncEnabled;

  @Value("${kubeguard.nvd.results-per-page:20}")
  private int resultsPerPage;

  @Override
  protected RuleRegistry getRuleRegistry() {
    return ruleRegistry;
  }

  @Override
  protected boolean isClientEnabled() {
    return nvdClient.isEnabled();
  }

  @Override
  protected String getIntegrationName() {
    return "NVD";
  }

  /** Manually sync all Kubernetes-related vulnerabilities from NVD. */
  public int syncVulnerabilities() {
    return loadRules(
        () -> ruleConverter.convertToRules(nvdClient.searchKubernetesVulnerabilities(resultsPerPage)),
        "all Kubernetes CVEs");
  }

  /** Sync vulnerabilities published in the last {@code days} days. */
  public int syncRecentVulnerabilities(int days) {
    return loadRules(
        () -> ruleConverter.convertToRules(nvdClient.getRecentVulnerabilities(days, resultsPerPage)),
        "last " + days + " days");
  }

  /** Scheduled sync — runs daily at 2 AM if enabled. */
  @Scheduled(cron = "${kubeguard.nvd.sync-cron:0 0 2 * * ?}")
  public void scheduledSync() {
    if (!syncEnabled || !nvdClient.isEnabled()) {
      return;
    }
    logger.info("Running scheduled NVD sync...");
    syncRecentVulnerabilities(7);
  }

  /** Returns statistics about the NVD integration. */
  public NvdSyncStats getStats() {
    long nvdRuleCount = ruleRegistry.getAllRules().stream()
        .filter(rule -> rule.getMetadata().getId().startsWith("NVD-"))
        .count();
    return new NvdSyncStats(nvdClient.isEnabled(), syncEnabled, nvdRuleCount);
  }

  public record NvdSyncStats(boolean nvdEnabled, boolean syncEnabled, long nvdRuleCount) {}
}
