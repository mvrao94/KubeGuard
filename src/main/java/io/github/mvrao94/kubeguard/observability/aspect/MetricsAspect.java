package io.github.mvrao94.kubeguard.observability.aspect;

import io.github.mvrao94.kubeguard.dto.ScanResponse;
import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.ScanType;
import io.github.mvrao94.kubeguard.observability.metrics.ScanMetrics;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for automatic metrics collection on scan operations
 */
@Aspect
@Component
public class MetricsAspect {

    private final ScanMetrics scanMetrics;

    public MetricsAspect(ScanMetrics scanMetrics) {
        this.scanMetrics = scanMetrics;
    }

    @Around("execution(* io.github.mvrao94.kubeguard.service.ScanService.scan*(..))")
    public Object trackScanMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = scanMetrics.startTimer();
        
        try {
            Object result = joinPoint.proceed();
            
            // Extract scan type and record metrics
            if (result instanceof ScanResponse response) {
                ScanType scanType = determineScanType(joinPoint.getSignature().getName());
                ScanStatus status = ScanStatus.valueOf(response.getStatus());
                scanMetrics.recordScan(scanType, status);
                scanMetrics.stopTimer(sample, scanType);
            }
            
            return result;
        } catch (Exception e) {
            ScanType scanType = determineScanType(joinPoint.getSignature().getName());
            scanMetrics.recordScanError(scanType, e.getClass().getSimpleName());
            throw e;
        }
    }

    private ScanType determineScanType(String methodName) {
        if (methodName.contains("Manifest")) {
            return ScanType.MANIFEST;
        } else if (methodName.contains("Cluster")) {
            return ScanType.CLUSTER;
        }
        return ScanType.MANIFEST; // default
    }
}
