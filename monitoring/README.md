# KubeGuard Monitoring Setup

This directory contains monitoring configurations for KubeGuard using Prometheus and Grafana.

## Contents

- `prometheus.yml` - Prometheus scrape configuration
- `alerts.yml` - Prometheus alert rules
- `grafana/datasources/` - Grafana datasource configurations
- `grafana/dashboards/` - Grafana dashboard definitions

## Quick Start

### Using Docker Compose

The easiest way to get started with monitoring:

```bash
# Start all services including Prometheus and Grafana
docker-compose -f ../scripts/docker-compose.yml up -d

# Check services are running
docker-compose -f ../scripts/docker-compose.yml ps

# View logs
docker-compose -f ../scripts/docker-compose.yml logs -f prometheus
docker-compose -f ../scripts/docker-compose.yml logs -f grafana
```

Access:
- **KubeGuard**: http://localhost:8080
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

### Standalone Prometheus

If you want to run Prometheus separately:

```bash
# Start Prometheus with the configuration
prometheus --config.file=monitoring/prometheus.yml

# Access Prometheus UI
open http://localhost:9090
```

### Standalone Grafana

```bash
# Start Grafana
docker run -d \
  -p 3000:3000 \
  -v $(pwd)/monitoring/grafana/datasources:/etc/grafana/provisioning/datasources \
  -v $(pwd)/monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards \
  grafana/grafana:latest

# Access Grafana
open http://localhost:3000
```

## Kubernetes Deployment

### Prerequisites

- Prometheus Operator installed in your cluster
- kubectl configured to access your cluster

### Deploy Monitoring Resources

```bash
# Apply ServiceMonitor for metrics collection
kubectl apply -f k8s/servicemonitor.yaml

# Apply PrometheusRule for alerts
kubectl apply -f k8s/prometheusrule.yaml

# Verify ServiceMonitor
kubectl get servicemonitor -n kubeguard

# Verify PrometheusRule
kubectl get prometheusrule -n kubeguard
```

### Verify Metrics Collection

```bash
# Port-forward to Prometheus
kubectl port-forward -n monitoring svc/prometheus-operated 9090:9090

# Check targets in Prometheus UI
open http://localhost:9090/targets

# Query metrics
curl http://localhost:9090/api/v1/query?query=kubeguard_scans_total
```

## Available Metrics

### Application Metrics

| Metric | Type | Description |
|--------|------|-------------|
| `kubeguard_scans_total` | Counter | Total scans by type and status |
| `kubeguard_scans_active` | Gauge | Currently active scans |
| `kubeguard_scans_completed` | Gauge | Completed scans |
| `kubeguard_scans_failed` | Gauge | Failed scans |
| `kubeguard_scan_duration_seconds` | Histogram | Scan duration distribution |
| `kubeguard_findings_total` | Counter | Security findings by severity |
| `kubeguard_findings_count` | Gauge | Total findings count |
| `kubeguard_scan_errors` | Counter | Scan errors by type |

### JVM Metrics

- `jvm_memory_used_bytes` - Memory usage
- `jvm_memory_max_bytes` - Maximum memory
- `jvm_gc_pause_seconds` - GC pause time
- `jvm_threads_live` - Live threads

### HTTP Metrics

- `http_server_requests_seconds` - Request duration
- `http_server_requests_seconds_count` - Request count

## Grafana Dashboards

### KubeGuard Main Dashboard

The main dashboard (`kubeguard-dashboard.json`) includes:

1. **Overview Panels**
   - Active Scans
   - Completed Scans
   - Failed Scans
   - Total Findings

2. **Performance Panels**
   - Scan Rate over time
   - Scan Duration (p50, p95)
   - HTTP Request Duration

3. **Security Panels**
   - Findings by Severity
   - Critical Findings Trend

4. **System Panels**
   - JVM Memory Usage
   - CPU Usage
   - Thread Count

### Importing Custom Dashboards

1. Access Grafana at http://localhost:3000
2. Navigate to Dashboards → Import
3. Upload JSON file or paste dashboard ID
4. Select Prometheus datasource
5. Click Import

## Alert Rules

### Configured Alerts

1. **HighFailureRate**
   - Condition: Scan failure rate > 0.1/sec for 5 minutes
   - Severity: Warning

2. **ServiceDown**
   - Condition: Service unreachable for 1 minute
   - Severity: Critical

3. **HighMemoryUsage**
   - Condition: JVM heap > 90% for 5 minutes
   - Severity: Warning

4. **SlowScanDuration**
   - Condition: p95 scan duration > 60s for 10 minutes
   - Severity: Warning

5. **HighCriticalFindings**
   - Condition: > 10 critical findings in 1 hour
   - Severity: Warning

6. **DatabaseConnectionFailed**
   - Condition: Database health check fails for 2 minutes
   - Severity: Critical

### Testing Alerts

```bash
# Check alert rules in Prometheus
curl http://localhost:9090/api/v1/rules

# Check active alerts
curl http://localhost:9090/api/v1/alerts
```

## Customization

### Adding New Metrics

1. Create a new metric in your service:
```java
@Component
public class CustomMetrics {
    private final Counter myCounter;
    
    public CustomMetrics(MeterRegistry registry) {
        this.myCounter = Counter.builder("kubeguard.custom.metric")
            .description("My custom metric")
            .register(registry);
    }
}
```

2. Metrics are automatically exposed at `/actuator/prometheus`

### Adding New Dashboards

1. Create dashboard in Grafana UI
2. Export as JSON
3. Save to `monitoring/grafana/dashboards/`
4. Restart Grafana or wait for auto-reload

### Modifying Alert Rules

1. Edit `monitoring/alerts.yml` or `k8s/prometheusrule.yaml`
2. Reload Prometheus configuration:
```bash
# Docker
docker-compose -f ../scripts/docker-compose.yml restart prometheus

# Kubernetes
kubectl apply -f k8s/prometheusrule.yaml
```

## Troubleshooting

### Metrics Not Appearing

1. Check actuator endpoint:
```bash
curl http://localhost:8080/actuator/prometheus
```

2. Verify Prometheus scrape config:
```bash
curl http://localhost:9090/api/v1/targets
```

3. Check Prometheus logs:
```bash
docker-compose -f ../scripts/docker-compose.yml logs prometheus
```

### Grafana Connection Issues

1. Verify datasource configuration:
   - Go to Configuration → Data Sources
   - Test connection to Prometheus

2. Check network connectivity:
```bash
docker-compose -f ../scripts/docker-compose.yml exec grafana ping prometheus
```

### Missing Dashboard Panels

1. Verify metrics exist in Prometheus:
```bash
curl 'http://localhost:9090/api/v1/query?query=kubeguard_scans_total'
```

2. Check dashboard queries in Grafana
3. Verify time range selection

## Best Practices

1. **Metric Naming**: Follow Prometheus naming conventions
   - Use base unit (seconds, bytes)
   - Use descriptive names
   - Add appropriate labels

2. **Alert Tuning**: 
   - Start with conservative thresholds
   - Adjust based on baseline metrics
   - Avoid alert fatigue

3. **Dashboard Organization**:
   - Group related metrics
   - Use consistent time ranges
   - Add helpful descriptions

4. **Data Retention**:
   - Configure appropriate retention periods
   - Archive old data if needed
   - Monitor storage usage

## Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
