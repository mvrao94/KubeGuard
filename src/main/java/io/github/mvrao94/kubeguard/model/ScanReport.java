package io.github.mvrao94.kubeguard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scan_reports")
@Schema(description = "Complete security scan report with findings and statistics")
public class ScanReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(
      description = "Unique database identifier for the scan report",
      example = "1",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  @Schema(
      description = "Unique scan identifier (UUID)",
      example = "123e4567-e89b-12d3-a456-426614174000",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String scanId;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(nullable = false)
  @Schema(
      description = "Type of scan performed",
      example = "MANIFEST",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private ScanType scanType;

  @Column(nullable = false)
  @Schema(
      description =
          "Target of the scan (namespace for cluster scans, file path for manifest scans)",
      example = "default",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String target;

  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Schema(
      description = "Timestamp when the scan was initiated",
      example = "2025-11-30T14:30:00",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Schema(
      description = "Current status of the scan",
      example = "COMPLETED",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private ScanStatus status;

  @Column
  @Schema(
      description = "Error message if the scan failed",
      example = "Connection timeout to Kubernetes cluster")
  private String errorMessage;

  @Column
  @Schema(description = "Total number of Kubernetes resources scanned", example = "25")
  private Integer totalResources;

  @Column
  @Schema(description = "Number of critical severity issues found", example = "3")
  private Integer criticalIssues;

  @Column
  @Schema(description = "Number of high severity issues found", example = "8")
  private Integer highIssues;

  @Column
  @Schema(description = "Number of medium severity issues found", example = "15")
  private Integer mediumIssues;

  @Column
  @Schema(description = "Number of low severity issues found", example = "22")
  private Integer lowIssues;

  @OneToMany(mappedBy = "scanReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference
  @Schema(description = "List of security findings discovered during the scan")
  private List<SecurityFinding> findings = new ArrayList<>();

  // Constructors
  public ScanReport() {}

  public ScanReport(String scanId, ScanType scanType, String target) {
    this.scanId = scanId;
    this.scanType = scanType;
    this.target = target;
    this.timestamp = LocalDateTime.now();
    this.status = ScanStatus.RUNNING;
    this.totalResources = 0;
    this.criticalIssues = 0;
    this.highIssues = 0;
    this.mediumIssues = 0;
    this.lowIssues = 0;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getScanId() {
    return scanId;
  }

  public void setScanId(String scanId) {
    this.scanId = scanId;
  }

  public ScanType getScanType() {
    return scanType;
  }

  public void setScanType(ScanType scanType) {
    this.scanType = scanType;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public ScanStatus getStatus() {
    return status;
  }

  public void setStatus(ScanStatus status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getTotalResources() {
    return totalResources;
  }

  public void setTotalResources(Integer totalResources) {
    this.totalResources = totalResources;
  }

  public Integer getCriticalIssues() {
    return criticalIssues;
  }

  public void setCriticalIssues(Integer criticalIssues) {
    this.criticalIssues = criticalIssues;
  }

  public Integer getHighIssues() {
    return highIssues;
  }

  public void setHighIssues(Integer highIssues) {
    this.highIssues = highIssues;
  }

  public Integer getMediumIssues() {
    return mediumIssues;
  }

  public void setMediumIssues(Integer mediumIssues) {
    this.mediumIssues = mediumIssues;
  }

  public Integer getLowIssues() {
    return lowIssues;
  }

  public void setLowIssues(Integer lowIssues) {
    this.lowIssues = lowIssues;
  }

  public List<SecurityFinding> getFindings() {
    return findings;
  }

  public void setFindings(List<SecurityFinding> findings) {
    this.findings = findings;
  }

  public void addFinding(SecurityFinding finding) {
    findings.add(finding);
    finding.setScanReport(this);
  }
}
