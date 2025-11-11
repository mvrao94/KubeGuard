# KubeGuard Security Rules Reference

Complete reference of all 27+ security rules implemented in KubeGuard.

## Container Security Rules (CON)

### CON001 - Privileged Container Detected
- **Severity**: CRITICAL
- **Description**: Container is running in privileged mode, which grants access to all host devices
- **Remediation**: Remove `privileged: true` from container security context. Use specific capabilities instead.

### CON002 - Container Running as Root
- **Severity**: HIGH
- **Description**: Container is configured to run as root user (UID 0)
- **Remediation**: Set `runAsUser` to a non-zero value or set `runAsNonRoot: true`

### CON003 - Missing Resource Limits
- **Severity**: MEDIUM
- **Description**: Container does not have resource limits defined
- **Remediation**: Define CPU and memory limits for the container

### CON004 - Missing Readiness Probe
- **Severity**: LOW
- **Description**: Container does not have a readiness probe configured
- **Remediation**: Add a readiness probe to ensure traffic is only sent to ready containers

### CON005 - Missing Liveness Probe
- **Severity**: LOW
- **Description**: Container does not have a liveness probe configured
- **Remediation**: Add a liveness probe to enable automatic restart of unhealthy containers

### CON006 - Using Latest Tag
- **Severity**: MEDIUM
- **Description**: Container image uses 'latest' tag or no tag specified
- **Remediation**: Use specific version tags for container images to ensure reproducible deployments

### CON007 - Root Filesystem Not Read-Only
- **Severity**: MEDIUM
- **Description**: Container does not have read-only root filesystem enabled
- **Remediation**: Set `readOnlyRootFilesystem: true` in container security context

### CON008 - RunAsNonRoot Not Enforced
- **Severity**: HIGH
- **Description**: Container does not enforce running as non-root user
- **Remediation**: Set `runAsNonRoot: true` in container security context

### CON009 - Privilege Escalation Allowed
- **Severity**: HIGH
- **Description**: Container allows privilege escalation, which could be exploited by attackers
- **Remediation**: Set `allowPrivilegeEscalation: false` in container security context

### CON010 - Additional Capabilities Added
- **Severity**: MEDIUM
- **Description**: Container has additional Linux capabilities added
- **Remediation**: Remove unnecessary capabilities. Only add capabilities that are absolutely required

### CON011 - Capabilities Not Dropped
- **Severity**: MEDIUM
- **Description**: Container does not drop all capabilities by default
- **Remediation**: Drop all capabilities by default: `capabilities.drop: [ALL]`

### CON012 - Missing Startup Probe
- **Severity**: LOW
- **Description**: Container does not have a startup probe configured
- **Remediation**: Add a startup probe for applications with slow startup times

## Pod Security Rules (POD)

### POD001 - Missing Pod Security Context
- **Severity**: MEDIUM
- **Description**: Pod does not have security context defined
- **Remediation**: Define a security context for the pod with appropriate security settings

### POD002 - Pod Running as Root
- **Severity**: HIGH
- **Description**: Pod is configured to run as root user
- **Remediation**: Set `runAsUser` to a non-zero value in pod security context

### POD003 - Missing FSGroup
- **Severity**: LOW
- **Description**: Pod does not have fsGroup specified in security context
- **Remediation**: Set `fsGroup` in pod security context for proper volume permissions

### POD004 - Host Network Enabled
- **Severity**: HIGH
- **Description**: Pod is using the host network namespace
- **Remediation**: Avoid using `hostNetwork` unless absolutely necessary

### POD005 - Host PID Namespace Enabled
- **Severity**: HIGH
- **Description**: Pod is using the host PID namespace
- **Remediation**: Avoid using `hostPID` unless absolutely necessary

### POD006 - Host IPC Namespace Enabled
- **Severity**: HIGH
- **Description**: Pod is using the host IPC namespace
- **Remediation**: Avoid using `hostIPC` unless absolutely necessary

### POD007 - HostPath Volume Detected
- **Severity**: CRITICAL
- **Description**: Pod uses hostPath volume which provides direct access to host filesystem
- **Remediation**: Avoid using hostPath volumes. Use PersistentVolumes or other volume types instead

## Service Security Rules (SVC)

### SVC001 - LoadBalancer Service Detected
- **Severity**: MEDIUM
- **Description**: LoadBalancer services expose applications to the internet
- **Remediation**: Consider using ClusterIP or NodePort if external access is not required

## Network Security Rules (NET)

### NET001 - Missing Network Policies
- **Severity**: MEDIUM
- **Description**: No NetworkPolicy resources found in the namespace
- **Remediation**: Define NetworkPolicy resources to restrict traffic between pods

### NET002 - Ingress Without TLS
- **Severity**: HIGH
- **Description**: Ingress resource does not define TLS configuration
- **Remediation**: Add TLS configuration to your Ingress to secure traffic with HTTPS

## RBAC Rules (RBAC)

### RBAC001 - Overly Permissive Role
- **Severity**: HIGH
- **Description**: Role/ClusterRole grants wildcard ('*') permissions
- **Remediation**: Restrict verbs and resources to the minimum required. Avoid using '*'

### RBAC002 - Use of Default Service Account
- **Severity**: MEDIUM
- **Description**: Resource is using the default service account
- **Remediation**: Create and use a dedicated service account with least privilege

## Secret Management Rules (SEC)

### SEC001 - Hardcoded Secret in Environment Variable
- **Severity**: CRITICAL
- **Description**: Environment variable appears to contain a hardcoded secret
- **Remediation**: Use Kubernetes Secrets with `valueFrom.secretKeyRef` instead of hardcoded values

### SEC002 - Service Account Token Auto-Mount Enabled
- **Severity**: LOW
- **Description**: Pod automatically mounts service account token
- **Remediation**: Set `automountServiceAccountToken: false` if the pod doesn't need Kubernetes API access

## Resource Management Rules (RES)

### RES001 - Missing Resource Requests
- **Severity**: MEDIUM
- **Description**: Container does not have resource requests defined
- **Remediation**: Define CPU and memory requests for proper scheduling and resource allocation

---

## Rule Categories by Severity

### CRITICAL (3 rules)
- CON001: Privileged Container
- POD007: HostPath Volume
- SEC001: Hardcoded Secret

### HIGH (10 rules)
- CON002: Running as Root
- CON008: RunAsNonRoot Not Enforced
- CON009: Privilege Escalation
- POD002: Pod Running as Root
- POD004: Host Network
- POD005: Host PID
- POD006: Host IPC
- NET002: Ingress Without TLS
- RBAC001: Overly Permissive Role

### MEDIUM (10 rules)
- CON003: Missing Resource Limits
- CON006: Using Latest Tag
- CON007: Root Filesystem Not Read-Only
- CON010: Additional Capabilities
- CON011: Capabilities Not Dropped
- POD001: Missing Pod Security Context
- SVC001: LoadBalancer Service
- NET001: Missing Network Policies
- RBAC002: Default Service Account
- RES001: Missing Resource Requests

### LOW (4 rules)
- CON004: Missing Readiness Probe
- CON005: Missing Liveness Probe
- CON012: Missing Startup Probe
- POD003: Missing FSGroup
- SEC002: Service Account Token Auto-Mount

## Compliance Mapping

### CIS Kubernetes Benchmark
- 5.2.1: Minimize privileged containers (CON001)
- 5.2.2: Minimize containers running as root (CON002, CON008, POD002)
- 5.2.3: Minimize capabilities (CON010, CON011)
- 5.2.4: Minimize hostPath volumes (POD007)
- 5.2.5: Minimize host networking (POD004)
- 5.2.6: Minimize host PID/IPC (POD005, POD006)
- 5.2.7: Minimize privilege escalation (CON009)

### Pod Security Standards
- **Restricted**: All CON and POD rules
- **Baseline**: CON001, POD004-007
- **Privileged**: No restrictions

## Quick Reference

```yaml
# Secure Container Configuration
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  allowPrivilegeEscalation: false
  readOnlyRootFilesystem: true
  capabilities:
    drop:
      - ALL
    add:
      - NET_BIND_SERVICE  # Only if needed

# Secure Pod Configuration
spec:
  securityContext:
    runAsUser: 1000
    fsGroup: 2000
  automountServiceAccountToken: false
  hostNetwork: false
  hostPID: false
  hostIPC: false
```
