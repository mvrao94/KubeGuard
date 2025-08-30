package io.github.mvrao94.kubeguard.controller;

import io.github.mvrao94.kubeguard.dto.SecurityMetrics;
import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.repository.SecurityFindingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for reporting and analytics */
@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports & Analytics", description = "APIs for retrieving scan reports and analytics")
public class ReportController {

  @Autowired private ScanReportRepository scanReportRepository;

  @Autowired private SecurityFindingRepository findingRepository;

  @Operation(
      summary = "Get all scan reports",
      description = "Retrieves paginated list of all scan reports")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Reports retrieved successfully")})
  @GetMapping
  public ResponseEntity<Page<ScanReport>> getAllReports(
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "Sort field") @RequestParam(defaultValue = "timestamp")
          String sortBy,
      @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc")
          String sortDir) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Page<ScanReport> reports = scanReportRepository.findAll(pageable);

    return ResponseEntity.ok(reports);
  }

  @Operation(
      summary = "Get reports with high priority findings",
      description = "Retrieves scan reports that contain critical or high severity findings")
  @GetMapping("/high-priority")
  public ResponseEntity<List<ScanReport>> getHighPriorityReports() {
    List<ScanReport> reports = scanReportRepository.findReportsWithHighPriorityFindings();
    return ResponseEntity.ok(reports);
  }

  @Operation(
      summary = "Get findings for a scan report",
      description = "Retrieves paginated security findings for a specific scan report")
  @GetMapping("/{scanReportId}/findings")
  public ResponseEntity<Page<SecurityFinding>> getFindings(
      @Parameter(description = "Scan report ID", required = true) @PathVariable Long scanReportId,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<SecurityFinding> findings = findingRepository.findByScanReportId(scanReportId, pageable);

    return ResponseEntity.ok(findings);
  }

  @Operation(
      summary = "Get top failing security rules",
      description = "Retrieves the most frequently failing security rules across all scans")
  @GetMapping("/analytics/top-failing-rules")
  public ResponseEntity<Map<String, Long>> getTopFailingRules() {
    List<Object[]> results = findingRepository.getTopFailingRules();

    Map<String, Long> topRules =
        results.stream()
            .limit(10) // Top 10 rules
            .collect(
                Collectors.toMap(
                    result -> (String) result[0], // rule ID
                    result -> (Long) result[1] // count
                    ));

    return ResponseEntity.ok(topRules);
  }

  @Operation(
      summary = "Get security metrics summary",
      description = "Retrieves overall security metrics and statistics")
  @GetMapping("/analytics/summary")
  public ResponseEntity<SecurityMetrics> getSecurityMetrics() {
    long totalReports = scanReportRepository.count();
    long completedReports = scanReportRepository.countByStatus(ScanStatus.COMPLETED);
    long failedReports = scanReportRepository.countByStatus(ScanStatus.FAILED);
    long runningReports = scanReportRepository.countByStatus(ScanStatus.RUNNING);

    List<ScanReport> recentReports =
        scanReportRepository
            .findAll(PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "timestamp")))
            .getContent();

    int totalCriticalFindings =
        recentReports.stream()
            .mapToInt(report -> report.getCriticalIssues() != null ? report.getCriticalIssues() : 0)
            .sum();

    int totalHighFindings =
        recentReports.stream()
            .mapToInt(report -> report.getHighIssues() != null ? report.getHighIssues() : 0)
            .sum();

    SecurityMetrics metrics = new SecurityMetrics();
    metrics.setTotalReports(totalReports);
    metrics.setCompletedReports(completedReports);
    metrics.setFailedReports(failedReports);
    metrics.setRunningReports(runningReports);
    metrics.setTotalCriticalFindings(totalCriticalFindings);
    metrics.setTotalHighFindings(totalHighFindings);

    return ResponseEntity.ok(metrics);
  }
}
