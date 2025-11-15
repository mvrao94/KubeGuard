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

  /**
   * Helper method to create a SecurityFinding object
   */
  private SecurityFinding createFinding(
      String resourceName,
      String resourceType,
      String namespace,
      String ruleId,
      String title,
      String description,
      Severity severity,
      String category,
      String remediation,
      String location) {
    SecurityFinding finding = new SecurityFinding();
    finding.setResourceName(resourceName);
    finding.setResourceType(resourceType);
    finding.setNamespace(namespace);
    finding.setRuleId(ruleId);
    finding.setTitle(title);
    finding.setDescription(description);
    finding.setSeverity(severity);
    finding.setCategory(category);
    finding.setRemediation(remediation);
    if (location != null) {
      finding.setLocation(location);
    }
    return finding;
  }

  public List<SecurityFinding> analyzeDeployment(V1Deployment deployment) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (deployment == null 
        || deployment.getMetadata() == null
        || deployment.getSpec() == null) {
      return findings;
    }

    V1DeploymentSpec spec = deployment.getSpec();
    V1PodTemplateSpec template = spec.getTemplate();
    if (template == null || template.getSpec() == null) {
      return findings;
    }

    V1PodSpec podSpec = template.getSpec();
    V1ObjectMeta metadata = deployment.getMetadata();
    String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";

    // Check containers
    List<V1Container> containers = podSpec.getContainers();
    if (containers != null) {
      for (V1Container container : containers) {
        findings.addAll(analyzeContainer(container, resourceName, "Deployment", namespace));
      }
    }

    // Check init containers
    List<V1Container> initContainers = podSpec.getInitContainers();
    if (initContainers != null) {
      for (V1Container container : initContainers) {
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

    if (pod == null || pod.getMetadata() == null || pod.getSpec() == null) {
      return findings;
    }

    V1ObjectMeta metadata = pod.getMetadata();
    String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";

    // Check containers
    List<V1Container> containers = pod.getSpec().getContainers();
    if (containers != null) {
      for (V1Container container : containers) {
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

    if (service == null || service.getMetadata() == null || service.getSpec() == null) {
      return findings;
    }

    V1ObjectMeta metadata = service.getMetadata();
    V1ServiceSpec spec = service.getSpec();
    String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
    String serviceType = spec.getType();

    // Check for insecure service types
    if ("LoadBalancer".equals(serviceType)) {
      findings.add(createFinding(
          resourceName, "Service", namespace,
          "SVC001", "LoadBalancer Service Detected",
          "LoadBalancer services expose applications to the internet. Ensure this is intentional.",
          Severity.MEDIUM, "Network Security",
          "Consider using ClusterIP or NodePort if external access is not required.",
          null));
    }

    return findings;
  }

  // --- Network Security Rules ---
  public List<SecurityFinding> analyzeNetworkPolicies(
      List<V1NetworkPolicy> networkPolicies, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    // NET001: Missing Network Policies
    if (networkPolicies == null || networkPolicies.isEmpty()) {
      findings.add(createFinding(
          "*", "NetworkPolicy", namespace,
          "NET001", "Missing Network Policies",
          "No NetworkPolicy resources found in the namespace. Pods may be open to unrestricted network access.",
          Severity.MEDIUM, "Network Security",
          "Define NetworkPolicy resources to restrict traffic between pods and from external sources.",
          null));
    }
    return findings;
  }

  public List<SecurityFinding> analyzeIngress(V1Ingress ingress) {
    List<SecurityFinding> findings = new ArrayList<>();
    if (ingress == null || ingress.getMetadata() == null || ingress.getSpec() == null) {
      return findings;
    }
    
    V1ObjectMeta metadata = ingress.getMetadata();
    V1IngressSpec spec = ingress.getSpec();
    
    // NET002: Ingress Without TLS
    List<V1IngressTLS> tls = spec.getTls();
    if (tls == null || tls.isEmpty()) {
      String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
      String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
      findings.add(createFinding(
          resourceName, "Ingress", namespace,
          "NET002", "Ingress Without TLS",
          "Ingress resource does not define TLS configuration. Traffic may be unencrypted.",
          Severity.HIGH, "Network Security",
          "Add TLS configuration to your Ingress to secure traffic with HTTPS.",
          null));
    }
    return findings;
  }

  // --- RBAC Rules ---
  public List<SecurityFinding> analyzeRole(V1Role role) {
    if (role == null || role.getMetadata() == null || role.getRules() == null) {
      return new ArrayList<>();
    }
    V1ObjectMeta metadata = role.getMetadata();
    String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
    return analyzeRbacRules(role.getRules(), resourceName, "Role", namespace);
  }

  public List<SecurityFinding> analyzeClusterRole(V1ClusterRole clusterRole) {
    if (clusterRole == null || clusterRole.getMetadata() == null || clusterRole.getRules() == null) {
      return new ArrayList<>();
    }
    V1ObjectMeta metadata = clusterRole.getMetadata();
    String resourceName = metadata.getName() != null ? metadata.getName() : "unknown";
    return analyzeRbacRules(clusterRole.getRules(), resourceName, "ClusterRole", "");
  }

  private List<SecurityFinding> analyzeRbacRules(
      List<V1PolicyRule> rules, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    
    for (V1PolicyRule rule : rules) {
      List<String> verbs = rule.getVerbs();
      if (verbs != null && verbs.contains("*")) {
        findings.add(createFinding(
            resourceName, resourceType, namespace,
            "RBAC001", "Overly Permissive " + resourceType,
            resourceType + " grants wildcard ('*') permissions. This is overly permissive and a security risk.",
            Severity.HIGH, "RBAC",
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.",
            null));
        break;
      }
      List<String> resources = rule.getResources();
      if (resources != null && resources.contains("*")) {
        findings.add(createFinding(
            resourceName, resourceType, namespace,
            "RBAC001", "Overly Permissive " + resourceType,
            resourceType + " grants wildcard ('*') resource access. This is overly permissive and a security risk.",
            Severity.HIGH, "RBAC",
            "Restrict verbs and resources to the minimum required for operation. Avoid using '*'.",
            null));
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
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "RBAC002", "Use of Default Service Account",
          "Resource is using the default service account, which has broad permissions by default.",
          Severity.MEDIUM, "RBAC",
          "Create and use a dedicated service account with least privilege for this workload.",
          null));
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
    List<V1Container> containers = podSpec.getContainers();
    if (containers != null) {
      for (V1Container container : containers) {
        findings.addAll(checkHardcodedSecrets(container, resourceName, resourceType, namespace));
      }
    }

    // SEC002: Check for automountServiceAccountToken
    if (podSpec.getAutomountServiceAccountToken() == null
        || Boolean.TRUE.equals(podSpec.getAutomountServiceAccountToken())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "SEC002", "Service Account Token Auto-Mount Enabled",
          "Pod automatically mounts service account token, which may not be necessary.",
          Severity.LOW, "Secret Management",
          "Set automountServiceAccountToken: false if the pod doesn't need to access the Kubernetes API.",
          null));
    }

    return findings;
  }

  private List<SecurityFinding> checkHardcodedSecrets(
      V1Container container, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    
    if (container == null) {
      return findings;
    }

    List<V1EnvVar> envVars = container.getEnv();
    if (envVars == null) {
      return findings;
    }

    for (V1EnvVar envVar : envVars) {
      if (envVar == null || envVar.getName() == null) {
        continue;
      }
      String envName = envVar.getName().toLowerCase();
      if ((envName.contains("password")
              || envName.contains("secret")
              || envName.contains("token")
              || envName.contains("key")
              || envName.contains("api_key"))
          && envVar.getValue() != null) {
        findings.add(createFinding(
            resourceName, resourceType, namespace,
            "SEC001", "Hardcoded Secret in Environment Variable",
            "Environment variable '" + envVar.getName() + "' appears to contain a hardcoded secret.",
            Severity.CRITICAL, "Secret Management",
            "Use Kubernetes Secrets with valueFrom.secretKeyRef instead of hardcoded values.",
            "Container: " + container.getName()));
      }
    }
    return findings;
  }

  // --- Resource Management Rules ---
  public List<SecurityFinding> analyzeResourceQuotas(
      V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();

    if (podSpec == null) {
      return findings;
    }

    List<V1Container> containers = podSpec.getContainers();
    if (containers == null) {
      return findings;
    }

    for (V1Container container : containers) {
      if (container == null) {
        continue;
      }
      V1ResourceRequirements resources = container.getResources();

      // RES001: Check for missing resource requests
      if (resources == null
          || resources.getRequests() == null
          || resources.getRequests().isEmpty()) {
        findings.add(createFinding(
            resourceName, resourceType, namespace,
            "RES001", "Missing Resource Requests",
            "Container does not have resource requests defined.",
            Severity.MEDIUM, "Resource Management",
            "Define CPU and memory requests for proper scheduling and resource allocation.",
            "Container: " + container.getName()));
      }
    }

    return findings;
  }

  private List<SecurityFinding> analyzeContainer(
      V1Container container, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    
    if (container == null || container.getName() == null) {
      return findings;
    }
    
    String containerLocation = "Container: " + container.getName();
    V1SecurityContext securityContext = container.getSecurityContext();
    
    // CON001: Check for privileged containers
    if (securityContext != null && Boolean.TRUE.equals(securityContext.getPrivileged())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON001", "Privileged Container Detected",
          "Container is running in privileged mode, which grants access to all host devices.",
          Severity.CRITICAL, "Container Security",
          "Remove privileged: true from container security context. Use specific capabilities instead.",
          containerLocation));
    }

    // CON002: Check for containers running as root
    if (securityContext != null
        && securityContext.getRunAsUser() != null
        && securityContext.getRunAsUser() == 0) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON002", "Container Running as Root",
          "Container is configured to run as root user (UID 0).",
          Severity.HIGH, "Container Security",
          "Set runAsUser to a non-zero value or set runAsNonRoot: true.",
          containerLocation));
    }

    // CON003: Check for missing resource limits
    V1ResourceRequirements resources = container.getResources();
    if (resources == null || resources.getLimits() == null || resources.getLimits().isEmpty()) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON003", "Missing Resource Limits",
          "Container does not have resource limits defined.",
          Severity.MEDIUM, "Resource Management",
          "Define CPU and memory limits for the container.",
          containerLocation));
    }

    // CON004: Check for containers without readiness probe
    if (container.getReadinessProbe() == null) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON004", "Missing Readiness Probe",
          "Container does not have a readiness probe configured.",
          Severity.LOW, "Reliability",
          "Add a readiness probe to ensure traffic is only sent to ready containers.",
          containerLocation));
    }

    // CON005: Check for containers without liveness probe
    if (container.getLivenessProbe() == null) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON005", "Missing Liveness Probe",
          "Container does not have a liveness probe configured.",
          Severity.LOW, "Reliability",
          "Add a liveness probe to enable automatic restart of unhealthy containers.",
          containerLocation));
    }

    // CON006: Check for latest tag usage
    String image = container.getImage();
    if (image != null && (image.endsWith(":latest") || !image.contains(":"))) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON006", "Using Latest Tag",
          "Container image uses 'latest' tag or no tag specified.",
          Severity.MEDIUM, "Container Security",
          "Use specific version tags for container images to ensure reproducible deployments.",
          containerLocation + ", Image: " + image));
    }

    // CON007: Check for containers with write access to root filesystem
    if (securityContext == null
        || !Boolean.TRUE.equals(securityContext.getReadOnlyRootFilesystem())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON007", "Root Filesystem Not Read-Only",
          "Container does not have read-only root filesystem enabled.",
          Severity.MEDIUM, "Container Security",
          "Set readOnlyRootFilesystem: true in container security context.",
          containerLocation));
    }

    // CON008: Check for containers without runAsNonRoot
    if (securityContext == null
        || !Boolean.TRUE.equals(securityContext.getRunAsNonRoot())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON008", "RunAsNonRoot Not Enforced",
          "Container does not enforce running as non-root user.",
          Severity.HIGH, "Container Security",
          "Set runAsNonRoot: true in container security context.",
          containerLocation));
    }

    // CON009: Check for containers with privilege escalation allowed
    if (securityContext == null
        || !Boolean.FALSE.equals(securityContext.getAllowPrivilegeEscalation())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON009", "Privilege Escalation Allowed",
          "Container allows privilege escalation, which could be exploited by attackers.",
          Severity.HIGH, "Container Security",
          "Set allowPrivilegeEscalation: false in container security context.",
          containerLocation));
    }

    // CON010: Check for containers with added capabilities
    if (securityContext != null && securityContext.getCapabilities() != null) {
      List<String> addedCapabilities = securityContext.getCapabilities().getAdd();
      if (addedCapabilities != null && !addedCapabilities.isEmpty()) {
        findings.add(createFinding(
            resourceName, resourceType, namespace,
            "CON010", "Additional Capabilities Added",
            "Container has additional Linux capabilities added: " + addedCapabilities,
            Severity.MEDIUM, "Container Security",
            "Remove unnecessary capabilities. Only add capabilities that are absolutely required.",
            containerLocation));
      }
    }

    // CON011: Check for containers without dropped capabilities
    boolean capabilitiesDropped = false;
    if (securityContext != null && securityContext.getCapabilities() != null) {
      List<String> droppedCapabilities = securityContext.getCapabilities().getDrop();
      if (droppedCapabilities != null && droppedCapabilities.contains("ALL")) {
        capabilitiesDropped = true;
      }
    }
    
    if (!capabilitiesDropped) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON011", "Capabilities Not Dropped",
          "Container does not drop all capabilities by default.",
          Severity.MEDIUM, "Container Security",
          "Drop all capabilities by default and only add back what is needed: capabilities.drop: [ALL]",
          containerLocation));
    }

    // CON012: Check for missing startup probe
    if (container.getStartupProbe() == null) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "CON012", "Missing Startup Probe",
          "Container does not have a startup probe configured.",
          Severity.LOW, "Reliability",
          "Add a startup probe for applications with slow startup times to prevent premature restarts.",
          containerLocation));
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
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD001", "Missing Pod Security Context",
          "Pod does not have security context defined.",
          Severity.MEDIUM, "Pod Security",
          "Define a security context for the pod with appropriate security settings.",
          null));
      return findings;
    }

    // POD002: Check if pod is running as root
    if (securityContext.getRunAsUser() != null && securityContext.getRunAsUser() == 0) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD002", "Pod Running as Root",
          "Pod is configured to run as root user.",
          Severity.HIGH, "Pod Security",
          "Set runAsUser to a non-zero value in pod security context.",
          null));
    }

    // POD003: Check for missing fsGroup
    if (securityContext.getFsGroup() == null) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD003", "Missing FSGroup",
          "Pod does not have fsGroup specified in security context.",
          Severity.LOW, "Pod Security",
          "Set fsGroup in pod security context for proper volume permissions.",
          null));
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
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD004", "Host Network Enabled",
          "Pod is using the host network namespace.",
          Severity.HIGH, "Pod Security",
          "Avoid using hostNetwork unless absolutely necessary.",
          null));
    }

    // POD005: Check for hostPID usage
    if (Boolean.TRUE.equals(podSpec.getHostPID())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD005", "Host PID Namespace Enabled",
          "Pod is using the host PID namespace.",
          Severity.HIGH, "Pod Security",
          "Avoid using hostPID unless absolutely necessary.",
          null));
    }

    // POD006: Check for hostIPC usage
    if (Boolean.TRUE.equals(podSpec.getHostIPC())) {
      findings.add(createFinding(
          resourceName, resourceType, namespace,
          "POD006", "Host IPC Namespace Enabled",
          "Pod is using the host IPC namespace.",
          Severity.HIGH, "Pod Security",
          "Avoid using hostIPC unless absolutely necessary.",
          null));
    }

    // POD007: Check for hostPath volumes
    List<V1Volume> volumes = podSpec.getVolumes();
    if (volumes != null) {
      for (V1Volume volume : volumes) {
        if (volume != null && volume.getHostPath() != null) {
          String volumePath = volume.getHostPath().getPath();
          String volumeName = volume.getName();
          findings.add(createFinding(
              resourceName, resourceType, namespace,
              "POD007", "HostPath Volume Detected",
              "Pod uses hostPath volume: " + (volumePath != null ? volumePath : "unknown"),
              Severity.CRITICAL, "Pod Security",
              "Avoid using hostPath volumes. Use PersistentVolumes or other volume types instead.",
              "Volume: " + (volumeName != null ? volumeName : "unknown")));
        }
      }
    }

    return findings;
  }
}
