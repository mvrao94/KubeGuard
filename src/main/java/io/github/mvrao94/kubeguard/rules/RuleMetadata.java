package io.github.mvrao94.kubeguard.rules;

import java.util.List;
import java.util.Set;

/**
 * Metadata for security rules including compliance framework mappings
 */
public class RuleMetadata {
  
  private String id;
  private String title;
  private String description;
  private RuleSeverity severity;
  private RuleCategory category;
  private String remediation;
  private boolean enabled;
  
  // Compliance framework mappings
  private Set<String> cisBenchmarks;      // CIS Kubernetes Benchmark IDs
  private Set<String> nsaCisaGuidelines;  // NSA/CISA Kubernetes Hardening Guide
  private Set<String> mitreAttack;        // MITRE ATT&CK techniques
  private Set<String> owaspK8s;           // OWASP Kubernetes Top 10
  private Set<String> pciDss;             // PCI DSS requirements
  private Set<String> hipaa;              // HIPAA requirements
  private Set<String> nistCsf;            // NIST Cybersecurity Framework
  
  // Rule metadata
  private List<String> references;
  private List<String> tags;
  private String version;
  private String author;
  
  public RuleMetadata() {}
  
  public RuleMetadata(String id, String title, RuleSeverity severity, RuleCategory category) {
    this.id = id;
    this.title = title;
    this.severity = severity;
    this.category = category;
    this.enabled = true;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public RuleSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(RuleSeverity severity) {
    this.severity = severity;
  }

  public RuleCategory getCategory() {
    return category;
  }

  public void setCategory(RuleCategory category) {
    this.category = category;
  }

  public String getRemediation() {
    return remediation;
  }

  public void setRemediation(String remediation) {
    this.remediation = remediation;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Set<String> getCisBenchmarks() {
    return cisBenchmarks;
  }

  public void setCisBenchmarks(Set<String> cisBenchmarks) {
    this.cisBenchmarks = cisBenchmarks;
  }

  public Set<String> getNsaCisaGuidelines() {
    return nsaCisaGuidelines;
  }

  public void setNsaCisaGuidelines(Set<String> nsaCisaGuidelines) {
    this.nsaCisaGuidelines = nsaCisaGuidelines;
  }

  public Set<String> getMitreAttack() {
    return mitreAttack;
  }

  public void setMitreAttack(Set<String> mitreAttack) {
    this.mitreAttack = mitreAttack;
  }

  public Set<String> getOwaspK8s() {
    return owaspK8s;
  }

  public void setOwaspK8s(Set<String> owaspK8s) {
    this.owaspK8s = owaspK8s;
  }

  public Set<String> getPciDss() {
    return pciDss;
  }

  public void setPciDss(Set<String> pciDss) {
    this.pciDss = pciDss;
  }

  public Set<String> getHipaa() {
    return hipaa;
  }

  public void setHipaa(Set<String> hipaa) {
    this.hipaa = hipaa;
  }

  public Set<String> getNistCsf() {
    return nistCsf;
  }

  public void setNistCsf(Set<String> nistCsf) {
    this.nistCsf = nistCsf;
  }

  public List<String> getReferences() {
    return references;
  }

  public void setReferences(List<String> references) {
    this.references = references;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
