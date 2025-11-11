package io.github.mvrao94.kubeguard.service.rules;

import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.model.Severity;
import io.kubernetes.client.openapi.models.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Security rules engine for analyzing Kubernetes resources */
@Component
public class SecurityRulesEngine {

  public List<SecurityFinding> analyzeDeployment(V1Deployment deployment) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (deployment.getSpec() == null
        || deployment.getSpec().getTemplate() == null
        || deployment.getSpec().getTemplate().getSpec() == null) {
      return findings;
    }

    V1PodSpec podSpec = deployment.getSpec().getTemplate().getSpec();
    String resourceName = deployment.getMetadata().getName();
    String namespace = deployment.getMetadata().getNamespace();

    // Check containers
    if (podSpec.getContainers() != null) {
      for (V1Container container : podSpec.getContainers()) {
        findings.addAll(analyzeContainer(container, resourceName, "Deployment", namespace));
      }
    }

    // Check init containers
    if (podSpec.getInitContainers() != null) {
      for (V1Container container : podSpec.getInitContainers()) {
        findings.addAll(analyzeContainer(container, resourceName, "Deployment", namespace));
      }
    }

    // Check pod-level security settings
    findings.addAll(
        analyzePodSecurityContext(
            podSpec.getSecurityContext(), resourceName, "Deployment", namespace));

    // Check pod host settings
    findings.addAll(analyzePodHostSettings(podSpec, resourceName, "Deployment", namespace));

    // Check secret management
    findings.addAll(analyzeSecretManagement(podSpec, resourceName, "Deployment", namespace));

    // Check resource quotas
    findings.addAll(analyzeResourceQuotas(podSpec, resourceName, "Deployment", namespace));

    return findings;
  }

  public List<SecurityFinding> analyzePod(V1Pod pod) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (pod.getSpec() == null) {
      return findings;
    }

    String resourceName = pod.getMetadata().getName();
    String namespace = pod.getMetadata().getNamespace();

    // Check containers
    if (pod.getSpec().getContainers() != null) {
      for (V1Container container : pod.getSpec().getContainers()) {
        findings.addAll(analyzeContainer(container, resourceName, "Pod", namespace));
      }
    }

    // Check pod security context
    findings.addAll(
        analyzePodSecurityContext(
            pod.getSpec().getSecurityContext(), resourceName, "Pod", namespace));

    // Check pod host settings
    findings.addAll(analyzePodHostSettings(pod.getSpec(), resourceName, "Pod", namespace));

    // Check secret management
    findings.addAll(analyzeSecretManagement(pod.getSpec(), resourceName, "Pod", namespace));

    // Check resource quotas
    findings.addAll(analyzeResourceQuotas(pod.getSpec(), resourceName, "Pod", namespace));

    return findings;
  }

  public List<SecurityFinding> analyzeService(V1Service service) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (service.getSpec() == null) {
      return findings;
    }

    String resourceName = service.getMetadata().getName();
    String namespace = service.getMetadata().getNamespace();

    // Check for insecure service types
    if ("LoadBalancer".equals(service.getSpec().getType())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType("Service");
      finding.setNamespace(namespace);
      finding.setRuleId("SVC001");
      finding.setTitle("LoadBalancer Service Detected");
      finding.setDescription(
          "LoadBalancer services expose applications to the internet. Ensure this is intentional.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Network Security");
      finding.setRemediation(
          "Consider using ClusterIP or NodePort if external access is not required.");
      findings.add(finding);
    }

    return findings;
  }

  // --- Network Security Rules ---
  public List<SecurityFinding> analyzeNetworkPolicies(
      List<V1NetworkPolicy> networkPolicies, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    // NET001: Missing Network Policies
    if (networkPolicies == null || networkPolicies.isEmpty()) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName("*");
      finding.setResourceType("NetworkPolicy");
      finding.setNamespace(namespace);
      finding.setRuleId("NET001");
      finding.setTitle("Missing Network Policies");
      finding.setDescription(
          "No NetworkPolicy resources found in the namespace. Pods may be open to unrestricted network access.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Network Security");
      finding.setRemediation(
          "Define NetworkPolicy resources to restrict traffic between pods and from external sources.");
      findings.add(finding);
    }
    return findings;
  }

  public List<SecurityFinding> analyzeIngress(V1Ingress ingress) {
    List<SecurityFinding> findings = new ArrayList<>();
    if (ingress == null || ingress.getSpec() == null) {
      return findings;
    }
    // NET002: Ingress Without TLS
    List<V1IngressTLS> tls = ingress.getSpec().getTls();
    if (tls == null || tls.isEmpty()) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(ingress.getMetadata() != null ? ingress.getMetadata().getName() : "");
      finding.setResourceType("Ingress");
      finding.setNamespace(
          ingress.getMetadata() != null ? ingress.getMetadata().getNamespace() : "");
      finding.setRuleId("NET002");
      finding.setTitle("Ingress Without TLS");
      finding.setDescription(
          "Ingress resource does not define TLS configuration. Traffic may be unencrypted.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Network Security");
      finding.setRemediation("Add TLS configuration to your Ingress to secure traffic with HTTPS.");
      findings.add(finding);
    }
    return findings;
  }

  // --- RBAC Rules ---
  public List<SecurityFinding> analyzeRole(V1Role role) {
    List<SecurityFinding> findings = new ArrayList<>();
    if (role == null || role.getRules() == null) {
      return findings;
    }
    // RBAC001: Overly Permissive Roles
    for (V1PolicyRule rule : role.getRules()) {
      if (rule.getVerbs() != null && rule.getVerbs().contains("*")) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(role.getMetadata() != null ? role.getMetadata().getName() : "");
        finding.setResourceType("Role");
        finding.setNamespace(role.getMetadata() != null ? role.getMetadata().getNamespace() : "");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive Role");
        finding.setDescription(
            "Role grants wildcard ('*') permissions. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation(
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
      if (rule.getResources() != null && rule.getResources().contains("*")) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(role.getMetadata() != null ? role.getMetadata().getName() : "");
        finding.setResourceType("Role");
        finding.setNamespace(role.getMetadata() != null ? role.getMetadata().getNamespace() : "");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive Role");
        finding.setDescription(
            "Role grants wildcard ('*') resource access. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation(
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
    }
    return findings;
  }

  public List<SecurityFinding> analyzeClusterRole(V1ClusterRole clusterRole) {
    List<SecurityFinding> findings = new ArrayList<>();
    if (clusterRole == null || clusterRole.getRules() == null) {
      return findings;
    }
    // RBAC001: Overly Permissive ClusterRoles
    for (V1PolicyRule rule : clusterRole.getRules()) {
      if (rule.getVerbs() != null && rule.getVerbs().contains("*")) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(
            clusterRole.getMetadata() != null ? clusterRole.getMetadata().getName() : "");
        finding.setResourceType("ClusterRole");
        finding.setNamespace("");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive ClusterRole");
        finding.setDescription(
            "ClusterRole grants wildcard ('*') permissions. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation(
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
      if (rule.getResources() != null && rule.getResources().contains("*")) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(
            clusterRole.getMetadata() != null ? clusterRole.getMetadata().getName() : "");
        finding.setResourceType("ClusterRole");
        finding.setNamespace("");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive ClusterRole");
        finding.setDescription(
            "ClusterRole grants wildcard ('*') resource access. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation(
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
    }
    return findings;
  }

  public List<SecurityFinding> analyzePodServiceAccount(
      V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    // RBAC002: Use of Default Service Account
    if (podSpec != null
        && (podSpec.getServiceAccountName() == null
            || "default".equals(podSpec.getServiceAccountName()))) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("RBAC002");
      finding.setTitle("Use of Default Service Account");
      finding.setDescription(
          "Resource is using the default service account, which has broad permissions by default.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("RBAC");
      finding.setRemediation(
          "Create and use a dedicated service account with least privilege for this workload.");
      findings.add(finding);
    }
    return findings;
  }

  // --- Secret and Configuration Management Rules ---
  public List<SecurityFinding> analyzeSecretManagement(
      V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (podSpec == null) {
      return findings;
    }

    // SEC001: Check for environment variables that might contain secrets
    if (podSpec.getContainers() != null) {
      for (V1Container container : podSpec.getContainers()) {
        if (container.getEnv() != null) {
          for (V1EnvVar envVar : container.getEnv()) {
            String envName = envVar.getName().toLowerCase();
            if ((envName.contains("password")
                    || envName.contains("secret")
                    || envName.contains("token")
                    || envName.contains("key")
                    || envName.contains("api_key"))
                && envVar.getValue() != null) {
              SecurityFinding finding = new SecurityFinding();
              finding.setResourceName(resourceName);
              finding.setResourceType(resourceType);
              finding.setNamespace(namespace);
              finding.setRuleId("SEC001");
              finding.setTitle("Hardcoded Secret in Environment Variable");
              finding.setDescription(
                  "Environment variable '"
                      + envVar.getName()
                      + "' appears to contain a hardcoded secret.");
              finding.setSeverity(Severity.CRITICAL);
              finding.setCategory("Secret Management");
              finding.setRemediation(
                  "Use Kubernetes Secrets with valueFrom.secretKeyRef instead of hardcoded values.");
              finding.setLocation("Container: " + container.getName());
              findings.add(finding);
            }
          }
        }
      }
    }

    // SEC002: Check for automountServiceAccountToken
    if (podSpec.getAutomountServiceAccountToken() == null
        || Boolean.TRUE.equals(podSpec.getAutomountServiceAccountToken())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("SEC002");
      finding.setTitle("Service Account Token Auto-Mount Enabled");
      finding.setDescription(
          "Pod automatically mounts service account token, which may not be necessary.");
      finding.setSeverity(Severity.LOW);
      finding.setCategory("Secret Management");
      finding.setRemediation(
          "Set automountServiceAccountToken: false if the pod doesn't need to access the Kubernetes API.");
      findings.add(finding);
    }

    return findings;
  }

  // --- Resource Management Rules ---
  public List<SecurityFinding> analyzeResourceQuotas(
      V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (podSpec == null || podSpec.getContainers() == null) {
      return findings;
    }

    for (V1Container container : podSpec.getContainers()) {
      V1ResourceRequirements resources = container.getResources();

      // RES001: Check for missing resource requests
      if (resources == null
          || resources.getRequests() == null
          || resources.getRequests().isEmpty()) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(resourceName);
        finding.setResourceType(resourceType);
        finding.setNamespace(namespace);
        finding.setRuleId("RES001");
        finding.setTitle("Missing Resource Requests");
        finding.setDescription("Container does not have resource requests defined.");
        finding.setSeverity(Severity.MEDIUM);
        finding.setCategory("Resource Management");
        finding.setRemediation(
            "Define CPU and memory requests for proper scheduling and resource allocation.");
        finding.setLocation("Container: " + container.getName());
        findings.add(finding);
      }
    }

    return findings;
  }

  private List<SecurityFinding> analyzeContainer(
      V1Container container, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    // Rule: Check for privileged containers
    V1SecurityContext securityContext = container.getSecurityContext();
    if (securityContext != null && Boolean.TRUE.equals(securityContext.getPrivileged())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON001");
      finding.setTitle("Privileged Container Detected");
      finding.setDescription(
          "Container is running in privileged mode, which grants access to all host devices.");
      finding.setSeverity(Severity.CRITICAL);
      finding.setCategory("Container Security");
      finding.setRemediation(
          "Remove privileged: true from container security context. Use specific capabilities instead.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers running as root
    if (securityContext != null
        && securityContext.getRunAsUser() != null
        && securityContext.getRunAsUser() == 0) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON002");
      finding.setTitle("Container Running as Root");
      finding.setDescription("Container is configured to run as root user (UID 0).");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Container Security");
      finding.setRemediation("Set runAsUser to a non-zero value or set runAsNonRoot: true.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for missing resource limits
    V1ResourceRequirements resources = container.getResources();
    if (resources == null || resources.getLimits() == null || resources.getLimits().isEmpty()) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON003");
      finding.setTitle("Missing Resource Limits");
      finding.setDescription("Container does not have resource limits defined.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Resource Management");
      finding.setRemediation("Define CPU and memory limits for the container.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers without readiness probe
    if (container.getReadinessProbe() == null) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON004");
      finding.setTitle("Missing Readiness Probe");
      finding.setDescription("Container does not have a readiness probe configured.");
      finding.setSeverity(Severity.LOW);
      finding.setCategory("Reliability");
      finding.setRemediation(
          "Add a readiness probe to ensure traffic is only sent to ready containers.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers without liveness probe
    if (container.getLivenessProbe() == null) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON005");
      finding.setTitle("Missing Liveness Probe");
      finding.setDescription("Container does not have a liveness probe configured.");
      finding.setSeverity(Severity.LOW);
      finding.setCategory("Reliability");
      finding.setRemediation(
          "Add a liveness probe to enable automatic restart of unhealthy containers.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for latest tag usage
    String image = container.getImage();
    if (image != null && (image.endsWith(":latest") || !image.contains(":"))) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON006");
      finding.setTitle("Using Latest Tag");
      finding.setDescription("Container image uses 'latest' tag or no tag specified.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Container Security");
      finding.setRemediation(
          "Use specific version tags for container images to ensure reproducible deployments.");
      finding.setLocation("Container: " + container.getName() + ", Image: " + image);
      findings.add(finding);
    }

    // Rule: Check for containers with write access to root filesystem
    if (securityContext == null
        || !Boolean.TRUE.equals(securityContext.getReadOnlyRootFilesystem())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON007");
      finding.setTitle("Root Filesystem Not Read-Only");
      finding.setDescription("Container does not have read-only root filesystem enabled.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Container Security");
      finding.setRemediation("Set readOnlyRootFilesystem: true in container security context.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers without runAsNonRoot
    if (securityContext == null
        || !Boolean.TRUE.equals(securityContext.getRunAsNonRoot())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON008");
      finding.setTitle("RunAsNonRoot Not Enforced");
      finding.setDescription("Container does not enforce running as non-root user.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Container Security");
      finding.setRemediation("Set runAsNonRoot: true in container security context.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers with privilege escalation allowed
    if (securityContext == null
        || !Boolean.FALSE.equals(securityContext.getAllowPrivilegeEscalation())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON009");
      finding.setTitle("Privilege Escalation Allowed");
      finding.setDescription(
          "Container allows privilege escalation, which could be exploited by attackers.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Container Security");
      finding.setRemediation("Set allowPrivilegeEscalation: false in container security context.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers with added capabilities
    if (securityContext != null
        && securityContext.getCapabilities() != null
        && securityContext.getCapabilities().getAdd() != null
        && !securityContext.getCapabilities().getAdd().isEmpty()) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON010");
      finding.setTitle("Additional Capabilities Added");
      finding.setDescription(
          "Container has additional Linux capabilities added: "
              + securityContext.getCapabilities().getAdd());
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Container Security");
      finding.setRemediation(
          "Remove unnecessary capabilities. Only add capabilities that are absolutely required.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers without dropped capabilities
    if (securityContext == null
        || securityContext.getCapabilities() == null
        || securityContext.getCapabilities().getDrop() == null
        || !securityContext.getCapabilities().getDrop().contains("ALL")) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON011");
      finding.setTitle("Capabilities Not Dropped");
      finding.setDescription("Container does not drop all capabilities by default.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Container Security");
      finding.setRemediation(
          "Drop all capabilities by default and only add back what is needed: capabilities.drop: [ALL]");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    // Rule: Check for containers with hostNetwork
    // This is checked at pod level, but we note it here for completeness

    // Rule: Check for containers with hostPID
    // This is checked at pod level

    // Rule: Check for containers with hostIPC
    // This is checked at pod level

    // Rule: Check for missing startup probe
    if (container.getStartupProbe() == null) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("CON012");
      finding.setTitle("Missing Startup Probe");
      finding.setDescription("Container does not have a startup probe configured.");
      finding.setSeverity(Severity.LOW);
      finding.setCategory("Reliability");
      finding.setRemediation(
          "Add a startup probe for applications with slow startup times to prevent premature restarts.");
      finding.setLocation("Container: " + container.getName());
      findings.add(finding);
    }

    return findings;
  }

  private List<SecurityFinding> analyzePodSecurityContext(
      V1PodSecurityContext securityContext,
      String resourceName,
      String resourceType,
      String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (securityContext == null) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD001");
      finding.setTitle("Missing Pod Security Context");
      finding.setDescription("Pod does not have security context defined.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Pod Security");
      finding.setRemediation(
          "Define a security context for the pod with appropriate security settings.");
      findings.add(finding);
      return findings;
    }

    // Rule: Check if pod is running as root
    if (securityContext.getRunAsUser() != null && securityContext.getRunAsUser() == 0) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD002");
      finding.setTitle("Pod Running as Root");
      finding.setDescription("Pod is configured to run as root user.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Pod Security");
      finding.setRemediation("Set runAsUser to a non-zero value in pod security context.");
      findings.add(finding);
    }

    // Rule: Check for missing fsGroup
    if (securityContext.getFsGroup() == null) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD003");
      finding.setTitle("Missing FSGroup");
      finding.setDescription("Pod does not have fsGroup specified in security context.");
      finding.setSeverity(Severity.LOW);
      finding.setCategory("Pod Security");
      finding.setRemediation("Set fsGroup in pod security context for proper volume permissions.");
      findings.add(finding);
    }

    return findings;
  }

  // --- Additional Pod Security Rules ---
  public List<SecurityFinding> analyzePodHostSettings(
      V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (podSpec == null) {
      return findings;
    }

    // POD004: Check for hostNetwork usage
    if (Boolean.TRUE.equals(podSpec.getHostNetwork())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD004");
      finding.setTitle("Host Network Enabled");
      finding.setDescription("Pod is using the host network namespace.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Pod Security");
      finding.setRemediation("Avoid using hostNetwork unless absolutely necessary.");
      findings.add(finding);
    }

    // POD005: Check for hostPID usage
    if (Boolean.TRUE.equals(podSpec.getHostPID())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD005");
      finding.setTitle("Host PID Namespace Enabled");
      finding.setDescription("Pod is using the host PID namespace.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Pod Security");
      finding.setRemediation("Avoid using hostPID unless absolutely necessary.");
      findings.add(finding);
    }

    // POD006: Check for hostIPC usage
    if (Boolean.TRUE.equals(podSpec.getHostIPC())) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("POD006");
      finding.setTitle("Host IPC Namespace Enabled");
      finding.setDescription("Pod is using the host IPC namespace.");
      finding.setSeverity(Severity.HIGH);
      finding.setCategory("Pod Security");
      finding.setRemediation("Avoid using hostIPC unless absolutely necessary.");
      findings.add(finding);
    }

    // POD007: Check for hostPath volumes
    if (podSpec.getVolumes() != null) {
      for (V1Volume volume : podSpec.getVolumes()) {
        if (volume.getHostPath() != null) {
          SecurityFinding finding = new SecurityFinding();
          finding.setResourceName(resourceName);
          finding.setResourceType(resourceType);
          finding.setNamespace(namespace);
          finding.setRuleId("POD007");
          finding.setTitle("HostPath Volume Detected");
          finding.setDescription(
              "Pod uses hostPath volume: " + volume.getHostPath().getPath());
          finding.setSeverity(Severity.CRITICAL);
          finding.setCategory("Pod Security");
          finding.setRemediation(
              "Avoid using hostPath volumes. Use PersistentVolumes or other volume types instead.");
          finding.setLocation("Volume: " + volume.getName());
          findings.add(finding);
        }
      }
    }

    return findings;
  }
}
