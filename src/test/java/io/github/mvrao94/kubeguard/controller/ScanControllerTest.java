package io.github.mvrao94.kubeguard.controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.github.mvrao94.kubeguard.dto.ScanRequest;
import io.github.mvrao94.kubeguard.dto.ScanResponse;
import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanType;
import io.github.mvrao94.kubeguard.service.ScanService;

@ExtendWith(MockitoExtension.class)
class ScanControllerTest {

  @Mock private ScanService scanService;

  @InjectMocks private ScanController controller;

  private ScanReport mockReport;

  @BeforeEach
  void setUp() {
    mockReport = new ScanReport("test-id", ScanType.MANIFEST, "/test/path");
    mockReport.setId(1L);
  }

  // --- scanManifests ---

  @Test
  void scanManifests_success_returns202WithRunningStatus() {
    when(scanService.scanManifests(anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockReport));

    ScanRequest request = new ScanRequest("/some/path");
    ResponseEntity<ScanResponse> response = controller.scanManifests(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo("RUNNING");
    assertThat(response.getBody().getMessage()).isEqualTo("Manifest scan started successfully");
    assertThat(response.getBody().getScanId()).isNotBlank();
  }

  @Test
  void scanManifests_illegalArgument_returns400() {
    when(scanService.scanManifests(anyString(), anyString()))
        .thenThrow(new IllegalArgumentException("path traversal not allowed"));

    ScanRequest request = new ScanRequest("../etc/passwd");
    ResponseEntity<ScanResponse> response = controller.scanManifests(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo("FAILED");
    assertThat(response.getBody().getMessage()).contains("path traversal not allowed");
  }

  @Test
  void scanManifests_unexpectedException_returns500() {
    when(scanService.scanManifests(anyString(), anyString()))
        .thenThrow(new RuntimeException("unexpected"));

    ScanRequest request = new ScanRequest("/some/path");
    ResponseEntity<ScanResponse> response = controller.scanManifests(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo("FAILED");
    assertThat(response.getBody().getMessage()).isEqualTo("Internal server error");
  }

  // --- scanCluster ---

  @Test
  void scanCluster_success_returns202WithRunningStatus() {
    when(scanService.scanCluster(anyString(), anyString()))
        .thenReturn(CompletableFuture.completedFuture(mockReport));

    ResponseEntity<ScanResponse> response = controller.scanCluster("default");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo("RUNNING");
    assertThat(response.getBody().getMessage()).isEqualTo("Cluster scan started successfully");
    assertThat(response.getBody().getScanId()).isNotBlank();
  }

  @Test
  void scanCluster_unexpectedException_returns500() {
    when(scanService.scanCluster(anyString(), anyString()))
        .thenThrow(new RuntimeException("cluster unreachable"));

    ResponseEntity<ScanResponse> response = controller.scanCluster("production");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo("FAILED");
  }

  // --- getScanStatus ---

  @Test
  void getScanStatus_whenFound_returns200WithReport() {
    when(scanService.getScanReport("test-id")).thenReturn(Optional.of(mockReport));

    ResponseEntity<ScanReport> response = controller.getScanStatus("test-id");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(mockReport);
  }

  @Test
  void getScanStatus_whenNotFound_returns404() {
    when(scanService.getScanReport("missing-id")).thenReturn(Optional.empty());

    ResponseEntity<ScanReport> response = controller.getScanStatus("missing-id");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
