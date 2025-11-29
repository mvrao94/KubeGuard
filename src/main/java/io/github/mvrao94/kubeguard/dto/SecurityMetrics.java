package io.github.mvrao94.kubeguard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** DTO for security metrics */
@Schema(description = "Aggregated security metrics and statistics across all scans")
public class SecurityMetrics {

  @Schema(description = "Total number of scan reports in the system", example = "150")
  private long totalReports;

  @Schema(description = "Number of successfully completed scans", example = "142")
  private long completedReports;

  @Schema(description = "Number of failed scans", example = "5")
  private long failedReports;

  @Schema(description = "Number of currently running scans", example = "3")
  private long runningReports;

  @Schema(
      description = "Total count of critical severity findings across recent scans",
      example = "23")
  private int totalCriticalFindings;

  @Schema(description = "Total count of high severity findings across recent scans", example = "67")
  private int totalHighFindings;

  // Getters and setters
  public long getTotalReports() {
    return totalReports;
  }

  public void setTotalReports(long totalReports) {
    this.totalReports = totalReports;
  }

  public long getCompletedReports() {
    return completedReports;
  }

  public void setCompletedReports(long completedReports) {
    this.completedReports = completedReports;
  }

  public long getFailedReports() {
    return failedReports;
  }

  public void setFailedReports(long failedReports) {
    this.failedReports = failedReports;
  }

  public long getRunningReports() {
    return runningReports;
  }

  public void setRunningReports(long runningReports) {
    this.runningReports = runningReports;
  }

  public int getTotalCriticalFindings() {
    return totalCriticalFindings;
  }

  public void setTotalCriticalFindings(int totalCriticalFindings) {
    this.totalCriticalFindings = totalCriticalFindings;
  }

  public int getTotalHighFindings() {
    return totalHighFindings;
  }

  public void setTotalHighFindings(int totalHighFindings) {
    this.totalHighFindings = totalHighFindings;
  }
}
