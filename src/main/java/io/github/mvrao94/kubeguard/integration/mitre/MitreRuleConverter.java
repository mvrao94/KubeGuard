package io.github.mvrao94.kubeguard.integration.mitre;

import io.github.mvrao94.kubeguard.rules.*;
import java.util.*;
import org.springframework.stereotype.Component;

/**
 * Converts MITRE ATT&CK techniques to KubeGuard security rules
 */
@Component
public class MitreRuleConverter {
  
  /**
   * Convert MITRE technique to SecurityRule
   */
  public SecurityRule convertToRule(MitreTechnique technique) {
    return new MitreSecurityRule(technique);
  }
  
  /**
   * Convert multiple techniques to rules
   */
  public List<SecurityRule> convertToRules(List<MitreTechnique> techniques) {
    return techniques.stream()
        .filter(MitreTechnique::isAppliesToKubernetes)
        .map(this::convertToRule)
        .toList();
  }
  
  /**
   * Inner class implementing SecurityRule for MITRE techniques
   */
  private static class MitreSecurityRule implements SecurityRule {
    
    private final MitreTechnique technique;
    private final RuleMetadata metadata;
    
    public MitreSecurityRule(MitreTechnique technique) {
      this.technique = technique;
      this.metadata = buildMetadata(technique);
    }
    
    private RuleMetadata buildMetadata(MitreTechnique technique) {
      RuleMetadata meta = new RuleMetadata();
      
      // Basic info
      meta.setId("MITRE-" + technique.getTechniqueId());
      meta.setTitle(technique.getTechniqueId() + ": " + technique.getName());
      meta.setDescription(technique.getDescription());
      meta.setSeverity(mapSeverityByTactic(technique.getTactic()));
      meta.setCategory(mapCategory(technique.getTactic()));
      meta.setEnabled(true);
      
      // Remediation from mitigations
      if (technique.getMitigations() != null && !technique.getMitigations().isEmpty()) {
        meta.setRemediation("Mitigations: " + String.join("; ", technique.getMitigations()));
      }
      
      // MITRE ATT&CK mapping
      meta.setMitreAttack(Set.of(technique.getTechniqueId()));
      
      // References
      List<String> refs = new ArrayList<>();
      refs.add(technique.getUrl());
      refs.add("https://attack.mitre.org/matrices/enterprise/containers/");
      meta.setReferences(refs);
      
      // Tags
      List<String> tags = new ArrayList<>();
      tags.add("mitre-attack");
      tags.add(technique.getTactic().toLowerCase().replace(" ", "-"));
      tags.add(technique.getTechniqueId());
      if (technique.getKubernetesResources() != null) {
        tags.addAll(technique.getKubernetesResources().stream()
            .map(String::toLowerCase)
            .toList());
      }
      meta.setTags(tags);
      
      meta.setVersion("1.0");
      meta.setAuthor("MITRE ATT&CK");
      
      return meta;
    }
    
    private RuleSeverity mapSeverityByTactic(String tactic) {
      return switch (tactic) {
        case "Initial Access", "Execution" -> RuleSeverity.CRITICAL;
        case "Persistence", "Privilege Escalation", "Credential Access" -> RuleSeverity.HIGH;
        case "Defense Evasion", "Discovery", "Lateral Movement" -> RuleSeverity.MEDIUM;
        case "Collection", "Impact" -> RuleSeverity.HIGH;
        default -> RuleSeverity.MEDIUM;
      };
    }
    
    private RuleCategory mapCategory(String tactic) {
      return switch (tactic) {
        case "Initial Access", "Execution" -> RuleCategory.POD_SECURITY;
        case "Persistence" -> RuleCategory.CONFIGURATION;
        case "Privilege Escalation" -> RuleCategory.SECURITY_CONTEXT;
        case "Defense Evasion" -> RuleCategory.AUDIT_LOGGING;
        case "Credential Access" -> RuleCategory.SECRETS_MANAGEMENT;
        case "Discovery", "Lateral Movement" -> RuleCategory.NETWORK_SECURITY;
        case "Collection" -> RuleCategory.SECRETS_MANAGEMENT;
        case "Impact" -> RuleCategory.RESOURCE_LIMITS;
        default -> RuleCategory.SECURITY_CONTEXT;
      };
    }
    
    @Override
    public RuleMetadata getMetadata() {
      return metadata;
    }
    
    @Override
    public List<RuleViolation> evaluate(Object resource) {
      // MITRE rules are informational - they provide attack context
      // Actual detection would be implemented in specific rules
      return Collections.emptyList();
    }
    
    @Override
    public boolean appliesTo(Class<?> resourceType) {
      // MITRE rules are informational
      return false;
    }
    
    @Override
    public List<Class<?>> getSupportedResourceTypes() {
      return Collections.emptyList();
    }
  }
}
