# KubeGuard Observability - Quick Start Guide

## 🚀 Quick Start

### 1. Start with Docker Compose (Recommended)

The fastest way to get KubeGuard running with full observability:

```bash
# Start all services (KubeGuard + PostgreSQL + Prometheus + Grafana)
docker-compose up -d

# Check all services are running
docker-compose ps

# View logs
docker-compose logs -f kubeguard
```

**Access Points:**
- **KubeGuard API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (login: admin/admin)

### 2. Verify Metrics Collection

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health | jq

# View all available metrics
curl http://localhost:8080/actuator/metrics | jq

# Get Prometheus-formatted metrics
curl http://localhost:8080/actuator/prometheus

# Check specific metric
curl http://localhost:8080/actuator/metrics/kubeguard.scans.total | jq
```

### 3. Access Grafana Dashboard

1. Open http://localhost:3000
2. Login with `admin` / `admin`
3. Navigate to **Dashboards** → **KubeGuard Monitoring Dashboard**
4. View real-time metrics and charts

## 📊 Available Endpoints

### Health Checks

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Overall application health |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |

### Metrics

| Endpoint | Description |
|----------|-------------|
| `/actuator/metrics` | List of available metrics |
| `/actuator/prometheus` | Prometheus-formatted metrics |
| `/actuator/info` | Application information |

### Management

| Endpoint | Description |
|----------|-------------|
| `/actuator/env` | Environment properties |
| `/actuator/loggers` | Logger configuration |

## 📈 Key Metrics

### Application Metrics

- `kubeguard_scans_total` - Total number of scans
- `kubeguard_scans_active` - Currently running scans
- `kubeguard_scans_completed` - Completed scans
- `kubeguard_scans_failed` - Failed scans
- `kubeguard_scan_duration_seconds` - Scan duration histogram
- `kubeguard_findings_total` - Security findings by severity
- `kubeguard_findings_count` - Total findings count

### System Metrics

- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_gc_pause_seconds` - Garbage collection metrics
- `http_server_requests_seconds` - HTTP request metrics
- `process_cpu_usage` - CPU usage

## 🔍 Example Queries

### Prometheus Queries

```promql
# Scan rate per second
rate(kubeguard_scans_total[5m])

# 95th percentile scan duration
histogram_quantile(0.95, rate(kubeguard_scan_duration_seconds_bucket[5m]))

# Failed scans in last hour
increase(kubeguard_scans_total{status="FAILED"}[1h])

# Critical findings
kubeguard_findings_total{severity="CRITICAL"}

# JVM memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100
```

### cURL Examples

```bash
# Get scan metrics
curl -s http://localhost:8080/actuator/metrics/kubeguard.scans.total | jq

# Get JVM memory
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq

# Get HTTP request metrics
curl -s http://localhost:8080/actuator/metrics/http.server.requests | jq

# Check application info
curl -s http://localhost:8080/actuator/info | jq
```

## 🎯 Kubernetes Deployment

### Deploy with Health Probes

The deployment already includes health probes:

```bash
# Apply Kubernetes resources
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Deploy monitoring resources (requires Prometheus Operator)
kubectl apply -f k8s/servicemonitor.yaml
kubectl apply -f k8s/prometheusrule.yaml

# Check pod health
kubectl get pods -n kubeguard
kubectl describe pod <pod-name> -n kubeguard

# Check health endpoints
kubectl port-forward -n kubeguard svc/kubeguard 8080:8080
curl http://localhost:8080/actuator/health
```

### Verify Prometheus Integration

```bash
# Port-forward to Prometheus (if using Prometheus Operator)
kubectl port-forward -n monitoring svc/prometheus-operated 9090:9090

# Check targets in Prometheus UI
open http://localhost:9090/targets

# Query KubeGuard metrics
curl 'http://localhost:9090/api/v1/query?query=kubeguard_scans_total'
```

## 🚨 Alerts

Pre-configured alerts include:

1. **High Failure Rate** - Scan failures exceed threshold
2. **Service Down** - KubeGuard is unreachable
3. **High Memory Usage** - JVM memory above 90%
4. **Slow Scan Duration** - Performance degradation
5. **High Critical Findings** - Excessive security issues
6. **Pod Crash Looping** - Container restart issues

View alerts in Prometheus: http://localhost:9090/alerts

## 🛠️ Troubleshooting

### Metrics Not Showing

```bash
# Check if actuator is enabled
curl http://localhost:8080/actuator

# Verify Prometheus endpoint
curl http://localhost:8080/actuator/prometheus | head -20

# Check application logs
docker-compose logs kubeguard
```

### Grafana Dashboard Empty

```bash
# Test Prometheus datasource
curl http://localhost:9090/api/v1/query?query=up

# Check if KubeGuard is being scraped
curl http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.labels.job=="kubeguard")'

# Restart Grafana
docker-compose restart grafana
```

### Health Check Failures

```bash
# Check detailed health
curl http://localhost:8080/actuator/health | jq

# Check database connectivity
docker-compose exec postgres pg_isready -U kubeguard

# View application logs
docker-compose logs -f kubeguard
```

## 📚 Next Steps

1. **Customize Dashboards**: Create custom Grafana dashboards for your needs
2. **Set Up Alerting**: Configure Alertmanager for notifications
3. **Add Custom Metrics**: Instrument your code with additional metrics
4. **Long-term Storage**: Set up Thanos or Cortex for metric retention
5. **Log Aggregation**: Integrate with ELK or Loki for log analysis

## 📖 Documentation

- [Full Observability Guide](docs/OBSERVABILITY.md)
- [Monitoring Setup](monitoring/README.md)
- [Prometheus Configuration](monitoring/prometheus.yml)
- [Alert Rules](monitoring/alerts.yml)

## 🤝 Support

For issues or questions:
- GitHub Issues: https://github.com/mvrao94/KubeGuard/issues
- Documentation: https://github.com/mvrao94/KubeGuard/blob/main/docs/OBSERVABILITY.md
