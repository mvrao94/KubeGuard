# KubeGuard Observability Guide

## Overview

KubeGuard includes comprehensive observability features with Prometheus metrics, custom health checks, and Grafana dashboards for monitoring application performance and security scan operations.

## Features

### 1. Health Checks

KubeGuard exposes multiple health endpoints:

- **Liveness Probe**: `/actuator/health/liveness`
- **Readiness Probe**: `/actuator/health/readiness`
- **Overall Health**: `/actuator/health`

#### Custom Health Indicators

- **Database Health**: Monitors database connectivity
- **Kubernetes Health**: Checks Kubernetes API connectivity
- **Scan Service Health**: Validates scan service availability

### 2. Prometheus Metrics

#### Application Metrics

- `kubeguard_scans_total` - Total number of scans by type and status
- `kubeguard_scans_active` - Number of currently active scans
- `kubeguard_scans_completed` - Number of completed scans
- `kubeguard_scans_failed` - Number of failed scans
- `kubeguard_scan_duration_seconds` - Scan duration histogram
- `kubeguard_findings_total` - Total security findings by severity
- `kubeguard_findings_count` - Current count of all findings
- `kubeguard_scan_errors` - Number of scan errors by type

#### JVM Metrics

- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_memory_max_bytes` - JVM maximum memory
- `jvm_gc_*` - Garbage collection metrics
- `jvm_threads_*` - Thread pool metrics

#### System Metrics

- `process_cpu_usage` - CPU usage
- `system_cpu_usage` - System CPU usage
- `process_uptime_seconds` - Application uptime

#### HTTP Metrics

- `http_server_requests_seconds` - HTTP request duration
- `http_server_requests_seconds_count` - HTTP request count
- `http_server_requests_seconds_sum` - Total HTTP request time

## Endpoints

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Overall application health |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/info` | Application information |
| `/actuator/metrics` | Available metrics list |
| `/actuator/metrics/{metric}` | Specific metric details |
| `/actuator/prometheus` | Prometheus-formatted metrics |
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logger configuration |

## Setup

### Local Development

1. **Start the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Access metrics**:
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

3. **Check health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Docker Compose

The docker-compose setup includes Prometheus and Grafana:

```bash
docker-compose up -d
```

Access:
- **Application**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### Kubernetes

Update your deployment to include health probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
```

## Grafana Dashboard

### Import Dashboard

1. Access Grafana at http://localhost:3000
2. Login with admin/admin
3. The KubeGuard dashboard is auto-provisioned
4. Navigate to Dashboards → KubeGuard Monitoring Dashboard

### Dashboard Panels

- **Active Scans**: Current number of running scans
- **Completed Scans**: Total completed scans
- **Failed Scans**: Total failed scans
- **Total Findings**: Aggregate security findings
- **Scan Rate**: Scans per second over time
- **Scan Duration**: p50 and p95 scan duration
- **Findings by Severity**: Distribution of findings
- **JVM Memory Usage**: Heap and non-heap memory

## Prometheus Configuration

### Scrape Configuration

```yaml
scrape_configs:
  - job_name: 'kubeguard'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['kubeguard:8080']
```

### Alert Rules

Alert rules are defined in `monitoring/alerts.yml`:

- **HighFailureRate**: Triggers when scan failure rate exceeds threshold
- **ServiceDown**: Alerts when KubeGuard is unreachable
- **HighMemoryUsage**: Warns when JVM memory usage is high
- **SlowScanDuration**: Detects performance degradation
- **HighCriticalFindings**: Alerts on excessive critical findings
- **DatabaseConnectionFailed**: Database connectivity issues

## Custom Metrics

### Recording Scan Metrics

The `ScanMetrics` component automatically tracks:

```java
@Autowired
private ScanMetrics scanMetrics;

// Record scan execution
scanMetrics.recordScan(ScanType.MANIFEST, ScanStatus.COMPLETED);

// Record scan duration
scanMetrics.recordScanDuration(ScanType.CLUSTER, Duration.ofSeconds(30));

// Record security finding
scanMetrics.recordFinding(Severity.CRITICAL);

// Record scan error
scanMetrics.recordScanError(ScanType.MANIFEST, "ValidationException");
```

### Automatic Tracking

The `MetricsAspect` automatically tracks metrics for all scan operations in `ScanService`.

## Monitoring Best Practices

### 1. Set Up Alerts

Configure alerting in Prometheus or Grafana for:
- High failure rates
- Slow scan performance
- Memory issues
- Service availability

### 2. Monitor Key Metrics

Focus on:
- Scan success rate
- Scan duration (p95, p99)
- Critical findings rate
- JVM memory usage
- HTTP error rates

### 3. Dashboard Organization

Create dashboards for:
- **Operations**: Service health, uptime, errors
- **Performance**: Response times, throughput
- **Security**: Findings by severity, scan coverage
- **Infrastructure**: JVM, CPU, memory

### 4. Log Correlation

Correlate metrics with logs:
- Use trace IDs for request tracking
- Link metrics to log events
- Set up log aggregation (ELK, Loki)

## Troubleshooting

### Metrics Not Appearing

1. Check actuator endpoints are enabled:
   ```yaml
   management:
     endpoints:
       web:
         exposure:
           include: prometheus
   ```

2. Verify Prometheus can reach the application:
   ```bash
   curl http://localhost:8080/actuator/prometheus
   ```

3. Check Prometheus targets: http://localhost:9090/targets

### Health Check Failures

1. Check individual health indicators:
   ```bash
   curl http://localhost:8080/actuator/health | jq
   ```

2. Review application logs for errors

3. Verify dependencies (database, Kubernetes API)

### High Memory Usage

1. Check JVM metrics:
   ```bash
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   ```

2. Analyze heap dumps if needed

3. Adjust JVM settings in Dockerfile or deployment

## Production Considerations

### Security

- Restrict actuator endpoints in production
- Use authentication for sensitive endpoints
- Configure `show-details: when_authorized` for health checks

### Performance

- Set appropriate scrape intervals (15-30s)
- Use metric cardinality limits
- Archive old metrics data

### Retention

- Configure Prometheus retention period
- Set up long-term storage (Thanos, Cortex)
- Archive Grafana dashboards

## References

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
