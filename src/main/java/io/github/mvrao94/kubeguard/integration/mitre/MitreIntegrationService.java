package io.github.mvrao94.kubeguard.integration.mitre;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.mvrao94.kubeguard.integration.BaseIntegrationService;
import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import jakarta.annotation.PostConstruct;

/**
 * Service for integrating MITRE ATT&CK framework into KubeGuard rule engine.
 * The common fetch → convert → register pipeline lives in BaseIntegrationService.
 */
@Service
public class MitreIntegrationService extends BaseIntegrationService {

  @Autowired private MitreAttackClient mitreClient;
  @Autowired private MitreRuleConverter ruleConverter;
  @Autowired private RuleRegistry ruleRegistry;

  @Value("${kubeguard.mitre.auto-load:true}")
  private boolean autoLoad;

  @Override
  protected RuleRegistry getRuleRegistry() {
    return ruleRegistry;
  }

  @Override
  protected boolean isClientEnabled() {
    return mitreClient.isEnabled();
  }

  @Override
  protected String getIntegrationName() {
    return "MITRE ATT&CK";
  }

  /** Auto-load MITRE ATT&CK techniques on startup if configured. */
  @PostConstruct
  public void init() {
    if (autoLoad && mitreClient.isEnabled()) {
      logger.info("Auto-loading MITRE ATT&CK techniques...");
      loadTechniques();
    }
  }

  /** Load all Kubernetes-relevant MITRE ATT&CK techniques. */
  public int loadTechniques() {
    return loadRules(
        () -> ruleConverter.convertToRules(mitreClient.getKubernetesTechniques()),
        "all Kubernetes techniques");
  }

  /** Load MITRE ATT&CK techniques for a specific tactic. */
  public int loadTechniquesByTactic(String tactic) {
    return loadRules(
        () -> ruleConverter.convertToRules(mitreClient.getTechniquesByTactic(tactic)),
        "tactic: " + tactic);
  }

  /** Returns statistics about the MITRE integration. */
  public MitreStats getStats() {
    long mitreRuleCount = ruleRegistry.getAllRules().stream()
        .filter(rule -> rule.getMetadata().getId().startsWith("MITRE-"))
        .count();

    Map<String, Long> rulesByTactic = ruleRegistry.getAllRules().stream()
        .filter(rule -> rule.getMetadata().getId().startsWith("MITRE-"))
        .collect(Collectors.groupingBy(
            rule -> rule.getMetadata().getTags().stream()
                .filter(tag -> !tag.equals("mitre-attack") && !tag.startsWith("T"))
                .findFirst()
                .orElse("unknown"),
            Collectors.counting()));

    return new MitreStats(mitreClient.isEnabled(), autoLoad, mitreRuleCount, rulesByTactic);
  }

  /** Returns the list of supported MITRE ATT&CK tactics. */
  public List<String> getAvailableTactics() {
    return List.of(
        "Initial Access", "Execution", "Persistence", "Privilege Escalation",
        "Defense Evasion", "Credential Access", "Discovery", "Lateral Movement",
        "Collection", "Impact");
  }

  public record MitreStats(
      boolean mitreEnabled,
      boolean autoLoad,
      long mitreRuleCount,
      Map<String, Long> rulesByTactic) {}
}
