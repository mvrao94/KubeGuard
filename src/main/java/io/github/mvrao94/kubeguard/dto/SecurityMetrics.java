package io.github.mvrao94.kubeguard.dto;

/** DTO for security metrics */
public class SecurityMetrics {
  private long totalReports;
  private long completedReports;
  private long failedReports;
  private long runningReports;
  private int totalCriticalFindings;
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
