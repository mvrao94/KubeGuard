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

/**
 * REST Controller for security scanning operations
 */
@RestController
@RequestMapping("/api/v1/scan")
@Tag(name = "Security Scanning", description = "APIs for performing security scans on Kubernetes resources")
public class ScanController {

    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

    @Autowired
    private ScanService scanService;

    @Operation(summary = "Scan Kubernetes manifest files",
            description = "Performs security analysis on Kubernetes YAML/YML files in the specified directory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Scan started successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/manifests")
    public ResponseEntity<ScanResponse> scanManifests(@Valid @RequestBody ScanRequest request) {
        logger.info("Received manifest scan request for path: {}", request.getPath());

        try {
            String scanId = UUID.randomUUID().toString();

            // Start async scan
            CompletableFuture<ScanReport> futureReport = scanService.scanManifests(request.getPath(), scanId);

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

    @Operation(summary = "Scan live Kubernetes cluster",
            description = "Performs security analysis on live resources in the specified namespace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Scan started successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid namespace"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/cluster/{namespace}")
    public ResponseEntity<ScanResponse> scanCluster(
            @Parameter(description = "Kubernetes namespace to scan", required = true)
            @PathVariable String namespace) {

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

    @Operation(summary = "Get scan status and results",
            description = "Retrieves the status and results of a security scan by scan ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scan report retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScanReport.class))),
            @ApiResponse(responseCode = "404", description = "Scan not found")
    })
    @GetMapping("/status/{scanId}")
    public ResponseEntity<ScanReport> getScanStatus(
            @Parameter(description = "Unique scan identifier", required = true)
            @PathVariable String scanId) {

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