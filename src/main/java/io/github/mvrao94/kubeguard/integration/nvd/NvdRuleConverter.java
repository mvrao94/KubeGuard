package io.github.mvrao94.kubeguard.integration.nvd;

import io.github.mvrao94.kubeguard.rules.*;
import java.util.*;
import org.springframework.stereotype.Component;

/**
 * Converts NVD vulnerabilities to KubeGuard security rules
 */
@Component
public class NvdRuleConverter {
  
  /**
   * Convert NVD vulnerability to SecurityRule
   */
  public SecurityRule convertToRule(NvdVulnerability vuln) {
    return new NvdSecurityRule(vuln);
  }
  
  /**
   * Convert multiple vulnerabilities to rules
   */
  public List<SecurityRule> convertToRules(List<NvdVulnerability> vulnerabilities) {
    return vulnerabilities.stream()
        .filter(NvdVulnerability::isKubernetesRelated)
        .map(this::convertToRule)
        .toList();
  }
  
  /**
   * Inner class implementing SecurityRule for NVD vulnerabilities
   */
  private static class NvdSecurityRule implements SecurityRule {
    
    private final NvdVulnerability vulnerability;
    private final RuleMetadata metadata;
    
    public NvdSecurityRule(NvdVulnerability vuln) {
      this.vulnerability = vuln;
      this.metadata = buildMetadata(vuln);
    }
    
    private RuleMetadata buildMetadata(NvdVulnerability vuln) {
      RuleMetadata meta = new RuleMetadata();
      
      // Basic info
      meta.setId("NVD-" + vuln.getCveId());
      meta.setTitle(vuln.getCveId() + ": " + truncate(vuln.getDescription(), 100));
      meta.setDescription(vuln.getDescription());
      meta.setSeverity(mapSeverity(vuln.getBaseSeverity()));
      meta.setCategory(RuleCategory.VULNERABILITY);
      meta.setEnabled(true);
      
      // Remediation
      meta.setRemediation("Review CVE details and apply patches. Check references: " + 
          String.join(", ", vuln.getReferences() != null ? vuln.getReferences() : List.of()));
      
      // References
      List<String> refs = new ArrayList<>();
      refs.add("https://nvd.nist.gov/vuln/detail/" + vuln.getCveId());
      if (vuln.getReferences() != null) {
        refs.addAll(vuln.getReferences());
      }
      meta.setReferences(refs);
      
      // Tags
      List<String> tags = new ArrayList<>();
      tags.add("nvd");
      tags.add("cve");
      tags.add(vuln.getCveId());
      if (vuln.getCweIds() != null) {
        tags.addAll(vuln.getCweIds());
      }
      meta.setTags(tags);
      
      // NIST CSF mapping
      if (vuln.getCweIds() != null && !vuln.getCweIds().isEmpty()) {
        meta.setNistCsf(new HashSet<>(vuln.getCweIds()));
      }
      
      meta.setVersion("1.0");
      meta.setAuthor("NIST NVD");
      
      return meta;
    }
    
    private RuleSeverity mapSeverity(String nvdSeverity) {
      if (nvdSeverity == null) return RuleSeverity.MEDIUM;
      
      return switch (nvdSeverity.toUpperCase()) {
        case "CRITICAL" -> RuleSeverity.CRITICAL;
        case "HIGH" -> RuleSeverity.HIGH;
        case "MEDIUM" -> RuleSeverity.MEDIUM;
        case "LOW" -> RuleSeverity.LOW;
        default -> RuleSeverity.INFO;
      };
    }
    
    private String truncate(String text, int maxLength) {
      if (text == null) return "";
      return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
    
    @Override
    public RuleMetadata getMetadata() {
      return metadata;
    }
    
    @Override
    public List<RuleViolation> evaluate(Object resource) {
      // NVD rules are informational - they don't evaluate resources directly
      // They provide vulnerability context for other rules
      return Collections.emptyList();
    }
    
    @Override
    public boolean appliesTo(Class<?> resourceType) {
      // NVD rules are informational
      return false;
    }
    
    @Override
    public List<Class<?>> getSupportedResourceTypes() {
      return Collections.emptyList();
    }
  }
}
