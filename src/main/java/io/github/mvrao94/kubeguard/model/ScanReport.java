package io.github.mvrao94.kubeguard.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scan_reports")
public class ScanReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String scanId;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Column(nullable = false)
  private ScanType scanType;

  @Column(nullable = false)
  private String target; // namespace or file path

  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ScanStatus status;

  @Column private String errorMessage;

  @Column private Integer totalResources;

  @Column private Integer criticalIssues;

  @Column private Integer highIssues;

  @Column private Integer mediumIssues;

  @Column private Integer lowIssues;

  @OneToMany(mappedBy = "scanReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference
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
