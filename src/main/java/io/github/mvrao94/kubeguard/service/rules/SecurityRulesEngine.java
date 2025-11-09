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
  public List<SecurityFinding> analyzeNetworkPolicies(List<V1NetworkPolicy> networkPolicies, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    // NET001: Missing Network Policies
    if (networkPolicies == null || networkPolicies.isEmpty()) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName("*");
      finding.setResourceType("NetworkPolicy");
      finding.setNamespace(namespace);
      finding.setRuleId("NET001");
      finding.setTitle("Missing Network Policies");
      finding.setDescription("No NetworkPolicy resources found in the namespace. Pods may be open to unrestricted network access.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("Network Security");
      finding.setRemediation("Define NetworkPolicy resources to restrict traffic between pods and from external sources.");
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
      finding.setNamespace(ingress.getMetadata() != null ? ingress.getMetadata().getNamespace() : "");
      finding.setRuleId("NET002");
      finding.setTitle("Ingress Without TLS");
      finding.setDescription("Ingress resource does not define TLS configuration. Traffic may be unencrypted.");
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
        finding.setDescription("Role grants wildcard ('*') permissions. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation("Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
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
        finding.setDescription("Role grants wildcard ('*') resource access. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation("Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
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
        finding.setResourceName(clusterRole.getMetadata() != null ? clusterRole.getMetadata().getName() : "");
        finding.setResourceType("ClusterRole");
        finding.setNamespace("");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive ClusterRole");
        finding.setDescription("ClusterRole grants wildcard ('*') permissions. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation("Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
      if (rule.getResources() != null && rule.getResources().contains("*")) {
        SecurityFinding finding = new SecurityFinding();
        finding.setResourceName(clusterRole.getMetadata() != null ? clusterRole.getMetadata().getName() : "");
        finding.setResourceType("ClusterRole");
        finding.setNamespace("");
        finding.setRuleId("RBAC001");
        finding.setTitle("Overly Permissive ClusterRole");
        finding.setDescription("ClusterRole grants wildcard ('*') resource access. This is overly permissive and a security risk.");
        finding.setSeverity(Severity.HIGH);
        finding.setCategory("RBAC");
        finding.setRemediation("Restrict verbs and resources to the minimum required for operation. Avoid using '*'.");
        findings.add(finding);
        break;
      }
    }
    return findings;
  }

  public List<SecurityFinding> analyzePodServiceAccount(V1PodSpec podSpec, String resourceName, String resourceType, String namespace) {
    List<SecurityFinding> findings = new ArrayList<>();
    // RBAC002: Use of Default Service Account
    if (podSpec != null && (podSpec.getServiceAccountName() == null || "default".equals(podSpec.getServiceAccountName()))) {
      SecurityFinding finding = new SecurityFinding();
      finding.setResourceName(resourceName);
      finding.setResourceType(resourceType);
      finding.setNamespace(namespace);
      finding.setRuleId("RBAC002");
      finding.setTitle("Use of Default Service Account");
      finding.setDescription("Resource is using the default service account, which has broad permissions by default.");
      finding.setSeverity(Severity.MEDIUM);
      finding.setCategory("RBAC");
      finding.setRemediation("Create and use a dedicated service account with least privilege for this workload.");
      findings.add(finding);
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
}
