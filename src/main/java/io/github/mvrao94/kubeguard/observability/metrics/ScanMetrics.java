package io.github.mvrao94.kubeguard.observability.metrics;

import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.ScanType;
import io.github.mvrao94.kubeguard.model.Severity;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for scan operations
 */
@Component
public class ScanMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> scanCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Counter> findingCounters = new ConcurrentHashMap<>();

    public ScanMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record a scan execution
     */
    public void recordScan(ScanType scanType, ScanStatus status) {
        String key = scanType.name() + "_" + status.name();
        scanCounters.computeIfAbsent(key, k ->
                Counter.builder("kubeguard.scans.total")
                        .description("Total number of scans")
                        .tag("type", scanType.name())
                        .tag("status", status.name())
                        .register(meterRegistry)
        ).increment();
    }

    /**
     * Record scan duration
     */
    public void recordScanDuration(ScanType scanType, Duration duration) {
        Timer.builder("kubeguard.scan.duration")
                .description("Duration of scan operations")
                .tag("type", scanType.name())
                .register(meterRegistry)
                .record(duration);
    }

    /**
     * Record a security finding
     */
    public void recordFinding(Severity severity) {
        findingCounters.computeIfAbsent(severity.name(), k ->
                Counter.builder("kubeguard.findings.total")
                        .description("Total number of security findings")
                        .tag("severity", severity.name())
                        .register(meterRegistry)
        ).increment();
    }

    /**
     * Record scan error
     */
    public void recordScanError(ScanType scanType, String errorType) {
        Counter.builder("kubeguard.scan.errors")
                .description("Number of scan errors")
                .tag("type", scanType.name())
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Get timer for manual recording
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop timer and record
     */
    public void stopTimer(Timer.Sample sample, ScanType scanType) {
        sample.stop(Timer.builder("kubeguard.scan.duration")
                .tag("type", scanType.name())
                .register(meterRegistry));
    }
}
