package io.github.mvrao94.kubeguard.rules;

/**
 * Represents a security rule violation
 */
public class RuleViolation {
  
  private String ruleId;
  private String title;
  private String description;
  private RuleSeverity severity;
  private String location;
  private String remediation;
  private String resourceName;
  private String resourceType;
  
  public RuleViolation() {}
  
  public RuleViolation(String ruleId, String title, RuleSeverity severity) {
    this.ruleId = ruleId;
    this.title = title;
    this.severity = severity;
  }

  // Getters and setters
  public String getRuleId() {
    return ruleId;
  }

  public void setRuleId(String ruleId) {
    this.ruleId = ruleId;
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

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getRemediation() {
    return remediation;
  }

  public void setRemediation(String remediation) {
    this.remediation = remediation;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }
}
