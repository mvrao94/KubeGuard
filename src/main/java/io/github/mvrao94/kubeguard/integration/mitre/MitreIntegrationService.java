package io.github.mvrao94.kubeguard.integration.mitre;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for integrating MITRE ATT&CK framework into KubeGuard rule engine
 */
@Service
public class MitreIntegrationService {
  
  private static final Logger logger = LoggerFactory.getLogger(MitreIntegrationService.class);
  
  @Autowired
  private MitreAttackClient mitreClient;
  
  @Autowired
  private MitreRuleConverter ruleConverter;
  
  @Autowired
  private RuleRegistry ruleRegistry;
  
  @Value("${kubeguard.mitre.auto-load:true}")
  private boolean autoLoad;
  
  /**
   * Load MITRE ATT&CK techniques on startup
   */
  @PostConstruct
  public void init() {
    if (autoLoad && mitreClient.isEnabled()) {
      logger.info("Auto-loading MITRE ATT&CK techniques...");
      loadTechniques();
    }
  }
  
  /**
   * Load MITRE ATT&CK techniques and convert to rules
   */
  public int loadTechniques() {
    if (!mitreClient.isEnabled()) {
      logger.info("MITRE ATT&CK integration is disabled");
      return 0;
    }
    
    logger.info("Loading MITRE ATT&CK techniques...");
    
    try {
      // Get Kubernetes-relevant techniques
      List<MitreTechnique> techniques = mitreClient.getKubernetesTechniques();
      
      logger.info("Loaded {} MITRE ATT&CK techniques", techniques.size());
      
      // Convert to security rules
      List<SecurityRule> rules = ruleConverter.convertToRules(techniques);
      
      logger.info("Converted {} techniques to security rules", rules.size());
      
      // Register rules
      ruleRegistry.registerRules(rules);
      
      logger.info("Successfully loaded {} MITRE ATT&CK rules", rules.size());
      
      return rules.size();
      
    } catch (Exception e) {
      logger.error("Error loading MITRE ATT&CK techniques: {}", e.getMessage(), e);
      return 0;
    }
  }
  
  /**
   * Load techniques for specific tactic
   */
  public int loadTechniquesByTactic(String tactic) {
    if (!mitreClient.isEnabled()) {
      return 0;
    }
    
    logger.info("Loading MITRE ATT&CK techniques for tactic: {}", tactic);
    
    try {
      List<MitreTechnique> techniques = mitreClient.getTechniquesByTactic(tactic);
      List<SecurityRule> rules = ruleConverter.convertToRules(techniques);
      ruleRegistry.registerRules(rules);
      
      logger.info("Loaded {} rules for tactic: {}", rules.size(), tactic);
      
      return rules.size();
      
    } catch (Exception e) {
      logger.error("Error loading techniques for tactic {}: {}", tactic, e.getMessage());
      return 0;
    }
  }
  
  /**
   * Get statistics about MITRE integration
   */
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
            Collectors.counting()
        ));
    
    return new MitreStats(
        mitreClient.isEnabled(),
        autoLoad,
        mitreRuleCount,
        rulesByTactic
    );
  }
  
  /**
   * Get available tactics
   */
  public List<String> getAvailableTactics() {
    return List.of(
        "Initial Access",
        "Execution",
        "Persistence",
        "Privilege Escalation",
        "Defense Evasion",
        "Credential Access",
        "Discovery",
        "Lateral Movement",
        "Collection",
        "Impact"
    );
  }
  
  public record MitreStats(
      boolean mitreEnabled,
      boolean autoLoad,
      long mitreRuleCount,
      Map<String, Long> rulesByTactic
  ) {}
}
