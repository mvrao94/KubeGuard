package io.github.mvrao94.kubeguard.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "security_findings")
@Schema(
    description = "Individual security finding or vulnerability detected in a Kubernetes resource")
public class SecurityFinding {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(
      description = "Unique database identifier for the finding",
      example = "42",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scan_report_id", nullable = false)
  @JsonBackReference
  @Schema(description = "Reference to the parent scan report", hidden = true)
  private ScanReport scanReport;

  @NotBlank
  @Column(nullable = false)
  @Schema(
      description = "Name of the Kubernetes resource where the issue was found",
      example = "nginx-deployment",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String resourceName;

  @NotBlank
  @Column(nullable = false)
  @Schema(
      description = "Type of Kubernetes resource",
      example = "Deployment",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String resourceType;

  @Column
  @Schema(description = "Kubernetes namespace of the resource", example = "production")
  private String namespace;

  @NotBlank
  @Column(nullable = false)
  @Schema(
      description = "Unique identifier of the security rule that was violated",
      example = "KSV001",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String ruleId;

  @NotBlank
  @Column(nullable = false)
  @Schema(
      description = "Short title describing the security issue",
      example = "Container running as root",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Column(columnDefinition = "TEXT")
  @Schema(
      description = "Detailed description of the security issue and its implications",
      example =
          "Container is running with root privileges, which poses a security risk if the container is compromised")
  private String description;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(nullable = false)
  @Schema(
      description = "Severity level of the security finding",
      example = "HIGH",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Severity severity;

  @Column
  @Schema(description = "Category of the security issue", example = "Security Context")
  private String category;

  @Column(columnDefinition = "TEXT")
  @Schema(
      description = "Recommended steps to remediate the security issue",
      example = "Set securityContext.runAsNonRoot to true and specify a non-root user ID")
  private String remediation;

  @Column
  @Schema(
      description = "Location in the manifest where the issue was found",
      example = "spec.template.spec.containers[0].securityContext")
  private String location;

  // Constructors
  public SecurityFinding() {}

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ScanReport getScanReport() {
    return scanReport;
  }

  public void setScanReport(ScanReport scanReport) {
    this.scanReport = scanReport;
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

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

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

  public Severity getSeverity() {
    return severity;
  }

  public void setSeverity(Severity severity) {
    this.severity = severity;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getRemediation() {
    return remediation;
  }

  public void setRemediation(String remediation) {
    this.remediation = remediation;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
}
