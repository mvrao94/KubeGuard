package io.github.mvrao94.kubeguard.observability.metrics;

import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.repository.SecurityFindingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Collector for custom application metrics
 */
@Component
public class CustomMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final ScanReportRepository scanReportRepository;
    private final SecurityFindingRepository securityFindingRepository;

    public CustomMetricsCollector(
            MeterRegistry meterRegistry,
            ScanReportRepository scanReportRepository,
            SecurityFindingRepository securityFindingRepository) {
        this.meterRegistry = meterRegistry;
        this.scanReportRepository = scanReportRepository;
        this.securityFindingRepository = securityFindingRepository;
    }

    @PostConstruct
    public void registerMetrics() {
        // Total scans gauge
        Gauge.builder("kubeguard.scans.active", scanReportRepository,
                        repo -> repo.countByStatus(ScanStatus.RUNNING))
                .description("Number of active scans")
                .register(meterRegistry);

        Gauge.builder("kubeguard.scans.completed", scanReportRepository,
                        repo -> repo.countByStatus(ScanStatus.COMPLETED))
                .description("Number of completed scans")
                .register(meterRegistry);

        Gauge.builder("kubeguard.scans.failed", scanReportRepository,
                        repo -> repo.countByStatus(ScanStatus.FAILED))
                .description("Number of failed scans")
                .register(meterRegistry);

        // Total findings gauge
        Gauge.builder("kubeguard.findings.count", securityFindingRepository,
                        SecurityFindingRepository::count)
                .description("Total number of security findings")
                .register(meterRegistry);
    }
}
