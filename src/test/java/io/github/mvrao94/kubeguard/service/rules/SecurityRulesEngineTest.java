package io.github.mvrao94.kubeguard.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.model.Severity;
import io.kubernetes.client.openapi.models.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for SecurityRulesEngine */
class SecurityRulesEngineTest {

  private SecurityRulesEngine rulesEngine;

  @BeforeEach
  void setUp() {
    rulesEngine = new SecurityRulesEngine();
  }

  @Test
  void testAnalyzeDeployment_WithPrivilegedContainer() {
    // Given
    V1Deployment deployment = createDeploymentWithPrivilegedContainer();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("CON001")
                    && finding.getSeverity() == Severity.CRITICAL
                    && finding.getTitle().contains("Privileged Container"));
  }

  @Test
  void testAnalyzeDeployment_WithRootUser() {
    // Given
    V1Deployment deployment = createDeploymentWithRootUser();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("CON002")
                    && finding.getSeverity() == Severity.HIGH
                    && finding.getTitle().contains("Container Running as Root"));
  }

  @Test
  void testAnalyzeDeployment_WithLatestTag() {
    // Given
    V1Deployment deployment = createDeploymentWithLatestTag();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("CON006")
                    && finding.getSeverity() == Severity.MEDIUM
                    && finding.getTitle().contains("Using Latest Tag"));
  }

  @Test
  void testAnalyzeService_WithLoadBalancer() {
    // Given
    V1Service service = createLoadBalancerService();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeService(service);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("SVC001")
                    && finding.getSeverity() == Severity.MEDIUM
                    && finding.getTitle().contains("LoadBalancer Service"));
  }

  private V1Deployment createDeploymentWithPrivilegedContainer() {
    V1Deployment deployment = new V1Deployment();

    // Metadata
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-deployment");
    metadata.setNamespace("default");
    deployment.setMetadata(metadata);

    // Spec
    V1DeploymentSpec spec = new V1DeploymentSpec();
    V1PodTemplateSpec template = new V1PodTemplateSpec();
    V1PodSpec podSpec = new V1PodSpec();

    // Container with privileged security context
    V1Container container = new V1Container();
    container.setName("test-container");
    container.setImage("nginx:1.20");

    V1SecurityContext securityContext = new V1SecurityContext();
    securityContext.setPrivileged(true);
    container.setSecurityContext(securityContext);

    podSpec.setContainers(List.of(container));
    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Deployment createDeploymentWithRootUser() {
    V1Deployment deployment = new V1Deployment();

    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-deployment");
    metadata.setNamespace("default");
    deployment.setMetadata(metadata);

    V1DeploymentSpec spec = new V1DeploymentSpec();
    V1PodTemplateSpec template = new V1PodTemplateSpec();
    V1PodSpec podSpec = new V1PodSpec();

    V1Container container = new V1Container();
    container.setName("test-container");
    container.setImage("nginx:1.20");

    V1SecurityContext securityContext = new V1SecurityContext();
    securityContext.setRunAsUser(0L); // Root user
    container.setSecurityContext(securityContext);

    podSpec.setContainers(List.of(container));
    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Deployment createDeploymentWithLatestTag() {
    V1Deployment deployment = new V1Deployment();

    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-deployment");
    metadata.setNamespace("default");
    deployment.setMetadata(metadata);

    V1DeploymentSpec spec = new V1DeploymentSpec();
    V1PodTemplateSpec template = new V1PodTemplateSpec();
    V1PodSpec podSpec = new V1PodSpec();

    V1Container container = new V1Container();
    container.setName("test-container");
    container.setImage("nginx:latest"); // Using latest tag

    podSpec.setContainers(List.of(container));
    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Service createLoadBalancerService() {
    V1Service service = new V1Service();

    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-service");
    metadata.setNamespace("default");
    service.setMetadata(metadata);

    V1ServiceSpec spec = new V1ServiceSpec();
    spec.setType("LoadBalancer");
    service.setSpec(spec);

    return service;
  }
}
