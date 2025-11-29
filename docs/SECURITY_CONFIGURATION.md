# Security Configuration Guide

## 🚨 CRITICAL: Authentication is MANDATORY

KubeGuard **REQUIRES** API key authentication. The application will **FAIL TO START** if not properly configured.

---

## 🔐 Quick Setup

### 1. Generate API Key
```bash
# Generate a secure 64-character API key
openssl rand -hex 32

# Example output:
# 7f3d9e2a1b4c8f6e5d7a9c2b4e6f8a1d3c5e7f9b2d4a6c8e1f3a5b7c9d2e4f6a8
```

### 2. Set Environment Variable
```bash
# Linux/Mac
export KUBEGUARD_API_KEY=7f3d9e2a1b4c8f6e5d7a9c2b4e6f8a1d3c5e7f9b2d4a6c8e1f3a5b7c9d2e4f6a8

# Windows PowerShell
$env:KUBEGUARD_API_KEY="7f3d9e2a1b4c8f6e5d7a9c2b4e6f8a1d3c5e7f9b2d4a6c8e1f3a5b7c9d2e4f6a8"
```

### 3. Start Application
```bash
mvn spring-boot:run

# You should see:
# ✅ API key authentication is enabled and configured
# ✅ All API requests require X-API-Key header
```

### 4. Make Authenticated Requests
```bash
curl -X POST http://localhost:8080/api/v1/scan/manifests \
  -H "X-API-Key: 7f3d9e2a1b4c8f6e5d7a9c2b4e6f8a1d3c5e7f9b2d4a6c8e1f3a5b7c9d2e4f6a8" \
  -H "Content-Type: application/json" \
  -d '{"path": "/path/to/manifests"}'
```

---

## ⚠️ What Happens Without API Key

### Application Startup
```
❌ FATAL: API key is not configured or too short!
❌ Set KUBEGUARD_API_KEY environment variable (minimum 32 characters)
❌ Generate a secure key: openssl rand -hex 32

Exception: IllegalStateException
Application FAILS TO START
```

### API Requests (if somehow bypassed)
```bash
curl http://localhost:8080/api/v1/scan/manifests

# Response: 401 Unauthorized
{
  "error": "Missing API key",
  "message": "X-API-Key header is required"
}
```

---

## 🔒 Security Features

### 1. Mandatory Authentication
- **ALL** API endpoints require `X-API-Key` header
- Minimum key length: 32 characters (64 recommended)
- Application fails fast if not configured
- No bypass or "demo mode"

### 2. Secure Endpoints
```
✅ Protected (Requires API Key):
  - /api/v1/scan/**
  - /api/v1/reports/**
  - /api/v1/integrations/**
  - /swagger-ui/**
  - /api-docs/**

✅ Public (Health Checks Only):
  - /actuator/health
  - /actuator/info
```

### 3. Security Headers
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
```

### 4. Stateless Authentication
- No sessions or cookies
- JWT-ready architecture
- Kubernetes-friendly (no sticky sessions)

---

## 🐳 Docker Configuration

### Docker Run
```bash
docker run -d \
  -e KUBEGUARD_API_KEY=$(openssl rand -hex 32) \
  -p 8080:8080 \
  mvrao94/kubeguard:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  kubeguard:
    image: mvrao94/kubeguard:latest
    environment:
      - KUBEGUARD_API_KEY=${KUBEGUARD_API_KEY}
    ports:
      - "8080:8080"
    restart: unless-stopped
```

**IMPORTANT**: Never hardcode API keys in docker-compose.yml!

---

## ☸️ Kubernetes Configuration

### Using Secrets (REQUIRED)
```yaml
# Create secret
apiVersion: v1
kind: Secret
metadata:
  name: kubeguard-api-key
  namespace: kubeguard
type: Opaque
stringData:
  api-key: "7f3d9e2a1b4c8f6e5d7a9c2b4e6f8a1d3c5e7f9b2d4a6c8e1f3a5b7c9d2e4f6a8"
---
# Use in deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubeguard
spec:
  template:
    spec:
      containers:
      - name: kubeguard
        image: mvrao94/kubeguard:latest
        env:
        - name: KUBEGUARD_API_KEY
          valueFrom:
            secretKeyRef:
              name: kubeguard-api-key
              key: api-key
```

### Generate Secret from Command Line
```bash
# Generate and create secret in one command
kubectl create secret generic kubeguard-api-key \
  --from-literal=api-key=$(openssl rand -hex 32) \
  -n kubeguard
```

---

## 🔑 API Key Management

### Rotation
```bash
# 1. Generate new key
NEW_KEY=$(openssl rand -hex 32)

# 2. Update secret
kubectl create secret generic kubeguard-api-key \
  --from-literal=api-key=$NEW_KEY \
  -n kubeguard \
  --dry-run=client -o yaml | kubectl apply -f -

# 3. Restart pods
kubectl rollout restart deployment/kubeguard -n kubeguard
```

### Multiple Keys (Future Enhancement)
```yaml
# Support for multiple API keys
kubeguard:
  security:
    api-keys:
      - name: "ci-cd-pipeline"
        key: "${CI_CD_API_KEY}"
      - name: "monitoring"
        key: "${MONITORING_API_KEY}"
```

---

## 🛡️ Advanced Security

### 1. RBAC Integration (Kubernetes)
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: kubeguard
  namespace: kubeguard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: kubeguard-scanner
rules:
- apiGroups: [""]
  resources: ["pods", "services", "configmaps", "secrets"]
  verbs: ["get", "list"]
- apiGroups: ["apps"]
  resources: ["deployments", "statefulsets", "daemonsets"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: kubeguard-scanner
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: kubeguard-scanner
subjects:
- kind: ServiceAccount
  name: kubeguard
  namespace: kubeguard
```

### 2. Network Policies
```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: kubeguard-network-policy
  namespace: kubeguard
spec:
  podSelector:
    matchLabels:
      app: kubeguard
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 443  # For NVD API
```

### 3. Pod Security Standards
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: kubeguard
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1000
    fsGroup: 1000
    seccompProfile:
      type: RuntimeDefault
  containers:
  - name: kubeguard
    securityContext:
      allowPrivilegeEscalation: false
      readOnlyRootFilesystem: true
      capabilities:
        drop:
        - ALL
```

---

## 🚫 What NOT to Do

### ❌ NEVER Disable Authentication
```yaml
# DON'T DO THIS!
kubeguard:
  security:
    require-api-key: false  # NEVER in production!
```

### ❌ NEVER Hardcode API Keys
```yaml
# DON'T DO THIS!
environment:
  - KUBEGUARD_API_KEY=my-secret-key  # Use secrets!
```

### ❌ NEVER Commit API Keys
```bash
# DON'T DO THIS!
git add .env
git commit -m "Added API key"  # Keys in git history!
```

### ❌ NEVER Use Weak Keys
```bash
# DON'T DO THIS!
export KUBEGUARD_API_KEY=password123  # Too short, too weak!
```

---

## ✅ Security Checklist

### Before Deployment
- [ ] API key generated (minimum 32 characters)
- [ ] API key stored in secrets management
- [ ] Environment variable configured
- [ ] Application starts successfully
- [ ] Authentication tested with curl
- [ ] Unauthorized requests return 401
- [ ] Invalid keys return 403

### Production Deployment
- [ ] Kubernetes Secret created
- [ ] RBAC configured
- [ ] Network policies applied
- [ ] Pod security standards enforced
- [ ] TLS/HTTPS enabled
- [ ] Monitoring configured
- [ ] API key rotation scheduled

### Ongoing
- [ ] Rotate API keys quarterly
- [ ] Monitor authentication failures
- [ ] Review access logs
- [ ] Update security policies
- [ ] Audit API key usage

---

## 📊 Monitoring Authentication

### Metrics
```
# Prometheus metrics
kubeguard_auth_requests_total{status="success"}
kubeguard_auth_requests_total{status="missing_key"}
kubeguard_auth_requests_total{status="invalid_key"}
```

### Logs
```
# Successful authentication
INFO: API request authenticated successfully

# Failed authentication
WARN: Authentication failed: Missing API key
WARN: Authentication failed: Invalid API key
```

---

## 🆘 Troubleshooting

### Application Won't Start
```
Error: IllegalStateException: API key must be configured

Solution:
1. Generate key: openssl rand -hex 32
2. Set environment: export KUBEGUARD_API_KEY=<key>
3. Restart application
```

### 401 Unauthorized
```
Error: {"error":"Missing API key"}

Solution:
Add X-API-Key header to all requests:
curl -H "X-API-Key: your-key-here" ...
```

### 403 Forbidden
```
Error: {"error":"Invalid API key"}

Solution:
1. Verify API key matches configured value
2. Check for typos or extra spaces
3. Regenerate if necessary
```

---

## 📞 Support

For security issues:
- **Email**: venkateswararaom07@gmail.com
- **Security Policy**: [SECURITY.md](../SECURITY.md)
- **GitHub Issues**: https://github.com/mvrao94/KubeGuard/issues

---

**Security Version**: 1.0.0  
**Last Updated**: 2025-11-30  
**Status**: ✅ Production-Ready and Secure
