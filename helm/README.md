# KubeGuard Helm Chart

Helm chart for deploying KubeGuard, a Kubernetes security scanner that identifies vulnerabilities in container images and running applications.

## Prerequisites

- Kubernetes 1.34+
- Helm 4.0+
- PostgreSQL 17 (included in chart)

## Quick Start

```bash
# Install with default values
helm install kubeguard ./helm

# Install in specific namespace
helm install kubeguard ./helm --namespace kubeguard --create-namespace

# Install with custom values
helm install kubeguard ./helm -f custom-values.yaml
```

## Configuration

### Key Configuration Values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace` | Kubernetes namespace | `kubeguard` |
| `replicaCount` | Number of application replicas | `2` |
| `image.repository` | Application image repository | `kubeguard` |
| `image.tag` | Application image tag | `1.0.0` |
| `service.type` | Kubernetes service type | `ClusterIP` |
| `service.port` | Service port | `80` |
| `ingress.enabled` | Enable ingress | `true` |
| `ingress.host` | Ingress hostname | `kubeguard.yourdomain.com` |
| `hpa.enabled` | Enable horizontal pod autoscaling | `true` |
| `hpa.minReplicas` | Minimum replicas | `2` |
| `hpa.maxReplicas` | Maximum replicas | `10` |

### Database Configuration

```yaml
postgres:
  database:
    name: kubeguard
    user: kubeguard
  persistence:
    enabled: true
    size: 10Gi
```

### Security Configuration

```yaml
secrets:
  dbPassword: ""  # Base64 encoded database password

securityContext:
  runAsNonRoot: true
  runAsUser: 1001
  runAsGroup: 1001
  fsGroup: 1001
```

### Resource Limits

```yaml
resources:
  limits:
    cpu: "1000m"
    memory: "1Gi"
  requests:
    cpu: "250m"
    memory: "512Mi"
```

## Installation Examples

### Basic Installation

```bash
helm install kubeguard ./helm
```

### Custom Database Password

```bash
# Using --set
helm install kubeguard ./helm \
  --set secrets.dbPassword="$(echo -n 'your-password' | base64)"

# Using values file
cat <<EOF > my-values.yaml
secrets:
  dbPassword: "eW91ci1wYXNzd29yZA=="
EOF
helm install kubeguard ./helm -f my-values.yaml
```

### Production Deployment

```bash
helm install kubeguard ./helm \
  --set replicaCount=3 \
  --set hpa.minReplicas=3 \
  --set hpa.maxReplicas=20 \
  --set ingress.host=kubeguard.production.com \
  --set ingress.tls.enabled=true
```

## Upgrading

```bash
# Upgrade with new values
helm upgrade kubeguard ./helm -f updated-values.yaml

# Upgrade with specific parameters
helm upgrade kubeguard ./helm --set image.tag=1.1.0
```

## Uninstallation

```bash
helm uninstall kubeguard

# Remove namespace if created
kubectl delete namespace kubeguard
```

## Validation

Validate the chart before installation:

```bash
# Lint the chart
helm lint ./helm

# Dry-run installation
helm install kubeguard ./helm --dry-run --debug

# Template rendering
helm template kubeguard ./helm

# Validate against Kubernetes API
helm install kubeguard ./helm --dry-run --validate
```

## Chart Components

The chart deploys the following Kubernetes resources:

- **Deployment**: Main application deployment with configurable replicas
- **Service**: ClusterIP service for internal communication
- **Ingress**: Optional ingress for external access
- **ConfigMap**: Application configuration
- **Secret**: Sensitive data (database passwords)
- **HorizontalPodAutoscaler**: Auto-scaling based on CPU/memory
- **PodDisruptionBudget**: Ensures availability during updates
- **NetworkPolicy**: Network security policies
- **RBAC**: Service account and role bindings
- **PostgreSQL**: Database deployment with persistent storage
- **ServiceMonitor**: Prometheus monitoring integration

## Monitoring

The chart includes Prometheus integration:

```yaml
monitoring:
  prometheus:
    enabled: true
    scrape: true
    path: /actuator/prometheus
    port: 8080
```

Access metrics at: `http://<service>:8080/actuator/prometheus`

## Troubleshooting

### Check deployment status
```bash
kubectl get pods -n kubeguard
kubectl describe pod <pod-name> -n kubeguard
```

### View logs
```bash
kubectl logs -f deployment/kubeguard -n kubeguard
```

### Check configuration
```bash
kubectl get configmap kubeguard-config -n kubeguard -o yaml
```

### Database connection issues
```bash
kubectl exec -it deployment/kubeguard-postgres -n kubeguard -- psql -U kubeguard
```

## Security Best Practices

- Always set a strong database password via `secrets.dbPassword`
- Enable TLS for ingress in production
- Review and adjust network policies for your environment
- Use private image registries with pull secrets
- Regularly update image tags to patch vulnerabilities
- Enable pod security policies/standards

## License

This project is licensed under the MIT License. See the LICENSE file for details.