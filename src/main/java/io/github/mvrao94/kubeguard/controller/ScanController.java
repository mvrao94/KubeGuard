package io.github.mvrao94.kubeguard.controller;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.mvrao94.kubeguard.dto.ScanRequest;
import io.github.mvrao94.kubeguard.dto.ScanResponse;
import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** REST Controller for security scanning operations */
@RestController
@RequestMapping("/api/v1/scan")
@Tag(
    name = "Security Scanning",
    description =
        "APIs for initiating and monitoring security scans on Kubernetes manifests and live clusters. "
            + "Scans analyze resources against security best practices and compliance rules.")
public class ScanController {

  private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

  @Autowired private ScanService scanService;

  // --- Shared helpers ---

  private ScanResponse errorResponse(String message) {
    ScanResponse r = new ScanResponse();
    r.setMessage(message);
    r.setStatus("FAILED");
    return r;
  }

  private ScanResponse startedResponse(String scanId, String message) {
    ScanResponse r = new ScanResponse();
    r.setScanId(scanId);
    r.setMessage(message);
    r.setStatus("RUNNING");
    return r;
  }

  private String sanitizeLog(String input) {
    return input == null ? null : input.replaceAll("[\r\n]", "_");
  }

  @Operation(
      summary = "Scan Kubernetes manifest files",
      description =
          "Initiates an asynchronous security scan of Kubernetes YAML/YML manifest files in the specified directory. "
              + "The scan analyzes resources for security misconfigurations, compliance violations, and best practice deviations. "
              + "Returns immediately with a scan ID that can be used to check status and retrieve results.",
      tags = {"Security Scanning"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "202",
            description =
                "Scan request accepted and processing started. Use the returned scanId to check status.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - path is missing, empty, or exceeds maximum length",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error occurred while initiating the scan",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class)))
      })
  @PostMapping("/manifests")
  public ResponseEntity<ScanResponse> scanManifests(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Scan request containing the path to Kubernetes manifest files",
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = ScanRequest.class)))
          @Valid
          @RequestBody
          ScanRequest request) {
    logger.info("Received manifest scan request for path: {}", sanitizeLog(request.getPath()));

    try {
      String scanId = UUID.randomUUID().toString();
      scanService.scanManifests(request.getPath(), scanId);
      return ResponseEntity.accepted().body(startedResponse(scanId, "Manifest scan started successfully"));

    } catch (IllegalArgumentException e) {
      logger.error("Invalid request for manifest scan: {}", e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse("Invalid request: " + e.getMessage()));

    } catch (Exception e) {
      logger.error("Unexpected error during manifest scan request: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Internal server error"));
    }
  }

  @Operation(
      summary = "Scan live Kubernetes cluster",
      description =
          "Initiates an asynchronous security scan of live resources in a Kubernetes cluster namespace. "
              + "Connects to the configured Kubernetes cluster and analyzes all resources in the specified namespace "
              + "for security issues, misconfigurations, and compliance violations. "
              + "Returns immediately with a scan ID that can be used to check status and retrieve results.",
      tags = {"Security Scanning"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "202",
            description =
                "Cluster scan request accepted and processing started. Use the returned scanId to check status.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid namespace provided or namespace does not exist",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class))),
        @ApiResponse(
            responseCode = "500",
            description =
                "Internal server error - could be due to cluster connectivity issues or authentication failures",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanResponse.class)))
      })
  @GetMapping("/cluster/{namespace}")
  public ResponseEntity<ScanResponse> scanCluster(
      @Parameter(
              description =
                  "Kubernetes namespace to scan. Use 'default' for the default namespace or specify a custom namespace name.",
              required = true,
              example = "production",
              schema = @Schema(type = "string", minLength = 1, maxLength = 253))
          @PathVariable
          String namespace) {

    logger.info("Received cluster scan request for namespace: {}", sanitizeLog(namespace));

    try {
      String scanId = UUID.randomUUID().toString();
      scanService.scanCluster(namespace, scanId);
      return ResponseEntity.accepted().body(startedResponse(scanId, "Cluster scan started successfully"));

    } catch (Exception e) {
      logger.error("Unexpected error during cluster scan request: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse("Internal server error"));
    }
  }

  @Operation(
      summary = "Get scan status and results",
      description =
          "Retrieves the complete status and results of a security scan using its unique scan ID. "
              + "Returns the scan report including status (RUNNING, COMPLETED, FAILED), statistics on findings by severity, "
              + "and detailed security findings if the scan is complete. "
              + "Poll this endpoint to monitor scan progress for asynchronous scans.",
      tags = {"Security Scanning"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Scan report retrieved successfully. Check the 'status' field to determine if scan is complete.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScanReport.class))),
        @ApiResponse(
            responseCode = "404",
            description =
                "Scan not found - the provided scanId does not exist or may have been deleted")
      })
  @GetMapping("/status/{scanId}")
  public ResponseEntity<ScanReport> getScanStatus(
      @Parameter(
              description =
                  "Unique scan identifier (UUID) returned from the scan initiation endpoint",
              required = true,
              example = "123e4567-e89b-12d3-a456-426614174000",
              schema = @Schema(type = "string", format = "uuid"))
          @PathVariable
          String scanId) {

    logger.debug("Retrieving scan status for scanId: {}", sanitizeLog(scanId));

    Optional<ScanReport> report = scanService.getScanReport(scanId);

    if (report.isPresent()) {
      return ResponseEntity.ok(report.get());
    } else {
      logger.warn("Scan report not found for scanId: {}", sanitizeLog(scanId));
      return ResponseEntity.notFound().build();
    }
  }
}
