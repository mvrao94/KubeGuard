package service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanType;
import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.service.ScanService;
import io.github.mvrao94.kubeguard.service.rules.SecurityRulesEngine;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private ScanReportRepository scanReportRepository;

    @Mock
    private SecurityRulesEngine rulesEngine;

    @InjectMocks
    private ScanService scanService;

    private ScanReport mockScanReport;

    @BeforeEach
    void setUp() {
        mockScanReport = new ScanReport("test-scan-id", ScanType.MANIFEST, "/test/path");
        mockScanReport.setId(1L);
    }

    @Test
    void testGetScanReport_WhenReportExists() {
        // Given
        String scanId = "test-scan-id";
        when(scanReportRepository.findByScanId(scanId)).thenReturn(Optional.of(mockScanReport));

        // When
        Optional<ScanReport> result = scanService.getScanReport(scanId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getScanId()).isEqualTo(scanId);
        verify(scanReportRepository).findByScanId(scanId);
    }

    @Test
    void testGetScanReport_WhenReportDoesNotExist() {
        // Given
        String scanId = "non-existent-scan-id";
        when(scanReportRepository.findByScanId(scanId)).thenReturn(Optional.empty());

        // When
        Optional<ScanReport> result = scanService.getScanReport(scanId);

        // Then
        assertThat(result).isEmpty();
        verify(scanReportRepository).findByScanId(scanId);
    }

    @Test
    void testScanManifests_WithValidDirectory() throws Exception {
        // Given
        String directoryPath = "src/test/resources/manifests";
        String scanId = "test-scan-id";
        when(scanReportRepository.save(any(ScanReport.class))).thenReturn(mockScanReport);

        // When
        CompletableFuture<ScanReport> result = scanService.scanManifests(directoryPath, scanId);

        // Then
        assertThat(result).isNotNull();
        verify(scanReportRepository, atLeastOnce()).save(any(ScanReport.class));
    }
}