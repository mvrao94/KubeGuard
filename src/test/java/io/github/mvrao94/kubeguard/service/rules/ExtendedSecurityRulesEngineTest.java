package io.github.mvrao94.kubeguard.service.rules;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.model.Severity;
import io.kubernetes.client.openapi.models.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Extended unit tests for SecurityRulesEngine - testing new rules */
class ExtendedSecurityRulesEngineTest {

  private SecurityRulesEngine rulesEngine;

  @BeforeEach
  void setUp() {
    rulesEngine = new SecurityRulesEngine();
  }

  @Test
  void testAnalyzeDeployment_WithHostNetwork() {
    // Given
    V1Deployment deployment = createDeploymentWithHostNetwork();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("POD004")
                    && finding.getSeverity() == Severity.HIGH
                    && finding.getTitle().contains("Host Network"));
  }

  @Test
  void testAnalyzeDeployment_WithPrivilegeEscalation() {
    // Given
    V1Deployment deployment = createDeploymentWithPrivilegeEscalation();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("CON009")
                    && finding.getSeverity() == Severity.HIGH
                    && finding.getTitle().contains("Privilege Escalation"));
  }

  @Test
  void testAnalyzeDeployment_WithHostPath() {
    // Given
    V1Deployment deployment = createDeploymentWithHostPath();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("POD007")
                    && finding.getSeverity() == Severity.CRITICAL
                    && finding.getTitle().contains("HostPath"));
  }

  @Test
  void testAnalyzeDeployment_WithHardcodedSecret() {
    // Given
    V1Deployment deployment = createDeploymentWithHardcodedSecret();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeDeployment(deployment);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("SEC001")
                    && finding.getSeverity() == Severity.CRITICAL
                    && finding.getTitle().contains("Hardcoded Secret"));
  }

  @Test
  void testAnalyzeIngress_WithoutTLS() {
    // Given
    V1Ingress ingress = createIngressWithoutTLS();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeIngress(ingress);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("NET002")
                    && finding.getSeverity() == Severity.HIGH
                    && finding.getTitle().contains("TLS"));
  }

  @Test
  void testAnalyzeRole_WithWildcardPermissions() {
    // Given
    V1Role role = createRoleWithWildcardPermissions();

    // When
    List<SecurityFinding> findings = rulesEngine.analyzeRole(role);

    // Then
    assertThat(findings).isNotEmpty();
    assertThat(findings)
        .anyMatch(
            finding ->
                finding.getRuleId().equals("RBAC001")
                    && finding.getSeverity() == Severity.HIGH
                    && finding.getTitle().contains("Overly Permissive"));
  }

  // Helper methods to create test resources

  private V1Deployment createDeploymentWithHostNetwork() {
    V1Deployment deployment = new V1Deployment();
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-deployment");
    metadata.setNamespace("default");
    deployment.setMetadata(metadata);

    V1DeploymentSpec spec = new V1DeploymentSpec();
    V1PodTemplateSpec template = new V1PodTemplateSpec();
    V1PodSpec podSpec = new V1PodSpec();
    podSpec.setHostNetwork(true);

    V1Container container = new V1Container();
    container.setName("test-container");
    container.setImage("nginx:1.20");
    podSpec.setContainers(List.of(container));

    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Deployment createDeploymentWithPrivilegeEscalation() {
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
    securityContext.setAllowPrivilegeEscalation(true);
    container.setSecurityContext(securityContext);

    podSpec.setContainers(List.of(container));
    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Deployment createDeploymentWithHostPath() {
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
    podSpec.setContainers(List.of(container));

    V1Volume volume = new V1Volume();
    volume.setName("host-volume");
    V1HostPathVolumeSource hostPath = new V1HostPathVolumeSource();
    hostPath.setPath("/var/run/docker.sock");
    volume.setHostPath(hostPath);
    podSpec.setVolumes(List.of(volume));

    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Deployment createDeploymentWithHardcodedSecret() {
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

    V1EnvVar envVar = new V1EnvVar();
    envVar.setName("DATABASE_PASSWORD");
    envVar.setValue("hardcoded-password-123");
    container.setEnv(List.of(envVar));

    podSpec.setContainers(List.of(container));
    template.setSpec(podSpec);
    spec.setTemplate(template);
    deployment.setSpec(spec);

    return deployment;
  }

  private V1Ingress createIngressWithoutTLS() {
    V1Ingress ingress = new V1Ingress();
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-ingress");
    metadata.setNamespace("default");
    ingress.setMetadata(metadata);

    V1IngressSpec spec = new V1IngressSpec();
    // No TLS configuration
    ingress.setSpec(spec);

    return ingress;
  }

  private V1Role createRoleWithWildcardPermissions() {
    V1Role role = new V1Role();
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setName("test-role");
    metadata.setNamespace("default");
    role.setMetadata(metadata);

    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(List.of("*"));
    rule.setResources(List.of("pods"));
    rule.setApiGroups(List.of(""));

    role.setRules(List.of(rule));

    return role;
  }
}
