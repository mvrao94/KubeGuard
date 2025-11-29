package io.github.mvrao94.kubeguard.controller;

import io.github.mvrao94.kubeguard.dto.SecurityMetrics;
import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.repository.SecurityFindingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(
    name = "Reports & Analytics",
    description =
        "APIs for retrieving historical scan reports, security findings, and aggregated analytics. "
            + "Use these endpoints to analyze trends, identify top security issues, and generate compliance reports.")
public class ReportController {

  @Autowired private ScanReportRepository scanReportRepository;

  @Autowired private SecurityFindingRepository findingRepository;

  @Operation(
      summary = "Get all scan reports",
      description =
          "Retrieves a paginated list of all security scan reports in the system. "
              + "Supports sorting by various fields and filtering by date range. "
              + "Use this endpoint to browse historical scans and track security posture over time.",
      tags = {"Reports & Analytics"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of scan reports retrieved successfully",
            content = @Content(mediaType = "application/json"))
      })
  @GetMapping
  public ResponseEntity<Page<ScanReport>> getAllReports(
      @Parameter(
              description = "Page number (0-based index). First page is 0.",
              example = "0",
              schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"))
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(
              description = "Number of reports per page",
              example = "10",
              schema =
                  @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10"))
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(
              description =
                  "Field name to sort by. Common values: timestamp, scanId, status, criticalIssues",
              example = "timestamp",
              schema = @Schema(type = "string", defaultValue = "timestamp"))
          @RequestParam(defaultValue = "timestamp")
          String sortBy,
      @Parameter(
              description = "Sort direction: 'asc' for ascending, 'desc' for descending",
              example = "desc",
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {"asc", "desc"},
                      defaultValue = "desc"))
          @RequestParam(defaultValue = "desc")
          String sortDir) {

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
    Page<ScanReport> reports = scanReportRepository.findAll(pageable);

    return ResponseEntity.ok(reports);
  }

  @Operation(
      summary = "Get reports with high priority findings",
      description =
          "Retrieves scan reports that contain critical or high severity security findings. "
              + "Use this endpoint to quickly identify scans that require immediate attention. "
              + "Reports are filtered to only include those with at least one CRITICAL or HIGH severity finding.",
      tags = {"Reports & Analytics"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of high-priority scan reports retrieved successfully",
            content = @Content(mediaType = "application/json"))
      })
  @GetMapping("/high-priority")
  public ResponseEntity<List<ScanReport>> getHighPriorityReports() {
    List<ScanReport> reports = scanReportRepository.findReportsWithHighPriorityFindings();
    return ResponseEntity.ok(reports);
  }

  @Operation(
      summary = "Get findings for a scan report",
      description =
          "Retrieves a paginated list of detailed security findings for a specific scan report. "
              + "Each finding includes information about the affected resource, severity, rule violated, "
              + "and remediation guidance. Use this to drill down into specific security issues.",
      tags = {"Reports & Analytics"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of security findings retrieved successfully",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(
            responseCode = "404",
            description = "Scan report not found with the provided ID")
      })
  @GetMapping("/{scanReportId}/findings")
  public ResponseEntity<Page<SecurityFinding>> getFindings(
      @Parameter(
              description = "Database ID of the scan report (not the scanId UUID)",
              required = true,
              example = "42",
              schema = @Schema(type = "integer", format = "int64"))
          @PathVariable
          Long scanReportId,
      @Parameter(
              description = "Page number (0-based index)",
              example = "0",
              schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"))
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(
              description = "Number of findings per page",
              example = "20",
              schema =
                  @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "20"))
          @RequestParam(defaultValue = "20")
          int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<SecurityFinding> findings = findingRepository.findByScanReportId(scanReportId, pageable);

    return ResponseEntity.ok(findings);
  }

  @Operation(
      summary = "Get top failing security rules",
      description =
          "Retrieves the most frequently violated security rules across all scans. "
              + "Returns a map of rule IDs to their violation counts, limited to the top 10 rules. "
              + "Use this to identify systemic security issues and prioritize remediation efforts.",
      tags = {"Reports & Analytics"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Map of rule IDs to violation counts retrieved successfully. "
                    + "Keys are rule IDs (e.g., 'KSV001'), values are violation counts.",
            content = @Content(mediaType = "application/json"))
      })
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
      description =
          "Retrieves aggregated security metrics and statistics across all scans in the system. "
              + "Includes total scan counts by status, and aggregated finding counts by severity. "
              + "Use this endpoint for dashboard displays and executive reporting.",
      tags = {"Reports & Analytics"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Security metrics summary retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SecurityMetrics.class)))
      })
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
