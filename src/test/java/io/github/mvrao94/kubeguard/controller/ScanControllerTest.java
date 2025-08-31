package io.github.mvrao94.kubeguard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mvrao94.kubeguard.dto.ScanRequest;
import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.ScanType;
import io.github.mvrao94.kubeguard.service.ScanService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for ScanController */
@WebMvcTest(ScanController.class)
@ExtendWith(MockitoExtension.class)
class ScanControllerTest {
  @Autowired private MockMvc mockMvc;

  @Mock private ScanService scanService;

  @InjectMocks private ScanController scanController;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void testScanManifests_Success() throws Exception {
    // Given
    ScanRequest request = new ScanRequest("/test/path");
    ScanReport mockReport = new ScanReport("scan-123", ScanType.MANIFEST, "/test/path");
    when(scanService.scanManifests(eq("/test/path"), any(String.class)))
        .thenReturn(CompletableFuture.completedFuture(mockReport));

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/scan/manifests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("RUNNING"))
        .andExpect(jsonPath("$.message").value("Manifest scan started successfully"))
        .andExpect(jsonPath("$.scanId").isNotEmpty());
  }

  @Test
  @WithMockUser
  void testScanManifests_InvalidPath() throws Exception {
    // Given
    ScanRequest request = new ScanRequest(""); // Empty path

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/scan/manifests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testScanCluster_Success() throws Exception {
    // Given
    String namespace = "default";
    ScanReport mockReport = new ScanReport("scan-456", ScanType.CLUSTER, namespace);
    when(scanService.scanCluster(eq(namespace), any(String.class)))
        .thenReturn(CompletableFuture.completedFuture(mockReport));

    // When & Then
    mockMvc
        .perform(get("/api/v1/scan/cluster/{namespace}", namespace))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("RUNNING"))
        .andExpect(jsonPath("$.message").value("Cluster scan started successfully"))
        .andExpect(jsonPath("$.scanId").isNotEmpty());
  }

  @Test
  @WithMockUser
  void testGetScanStatus_Found() throws Exception {
    // Given
    String scanId = "scan-123";
    ScanReport mockReport = new ScanReport(scanId, ScanType.MANIFEST, "/test/path");
    mockReport.setStatus(ScanStatus.COMPLETED);
    mockReport.setTimestamp(LocalDateTime.now());
    when(scanService.getScanReport(scanId)).thenReturn(Optional.of(mockReport));

    // When & Then
    mockMvc
        .perform(get("/api/v1/scan/status/{scanId}", scanId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scanId").value(scanId))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  @WithMockUser
  void testGetScanStatus_NotFound() throws Exception {
    // Given
    String scanId = "non-existent";
    when(scanService.getScanReport(scanId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/v1/scan/status/{scanId}", scanId)).andExpect(status().isNotFound());
  }
}
