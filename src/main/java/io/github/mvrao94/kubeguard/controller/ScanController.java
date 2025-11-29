package io.github.mvrao94.kubeguard.controller;

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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    logger.info("Received manifest scan request for path: {}", request.getPath());

    try {
      String scanId = UUID.randomUUID().toString();

      // Start async scan
      CompletableFuture<ScanReport> futureReport =
          scanService.scanManifests(request.getPath(), scanId);

      ScanResponse response = new ScanResponse();
      response.setScanId(scanId);
      response.setMessage("Manifest scan started successfully");
      response.setStatus("RUNNING");

      return ResponseEntity.accepted().body(response);

    } catch (IllegalArgumentException e) {
      logger.error("Invalid request for manifest scan: {}", e.getMessage());
      ScanResponse errorResponse = new ScanResponse();
      errorResponse.setMessage("Invalid request: " + e.getMessage());
      errorResponse.setStatus("FAILED");
      return ResponseEntity.badRequest().body(errorResponse);

    } catch (Exception e) {
      logger.error("Unexpected error during manifest scan request: ", e);
      ScanResponse errorResponse = new ScanResponse();
      errorResponse.setMessage("Internal server error");
      errorResponse.setStatus("FAILED");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    logger.info("Received cluster scan request for namespace: {}", namespace);

    try {
      String scanId = UUID.randomUUID().toString();

      // Start async scan
      CompletableFuture<ScanReport> futureReport = scanService.scanCluster(namespace, scanId);

      ScanResponse response = new ScanResponse();
      response.setScanId(scanId);
      response.setMessage("Cluster scan started successfully");
      response.setStatus("RUNNING");

      return ResponseEntity.accepted().body(response);

    } catch (Exception e) {
      logger.error("Unexpected error during cluster scan request: ", e);
      ScanResponse errorResponse = new ScanResponse();
      errorResponse.setMessage("Internal server error");
      errorResponse.setStatus("FAILED");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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

    logger.debug("Retrieving scan status for scanId: {}", scanId);

    Optional<ScanReport> report = scanService.getScanReport(scanId);

    if (report.isPresent()) {
      return ResponseEntity.ok(report.get());
    } else {
      logger.warn("Scan report not found for scanId: {}", scanId);
      return ResponseEntity.notFound().build();
    }
  }
}
