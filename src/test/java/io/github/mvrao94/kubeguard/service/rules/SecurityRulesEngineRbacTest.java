package io.github.mvrao94.kubeguard.service.rules;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.mvrao94.kubeguard.model.SecurityFinding;
import io.github.mvrao94.kubeguard.model.Severity;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PolicyRule;
import io.kubernetes.client.openapi.models.V1Role;

/**
 * Tests for analyzeClusterRole and the resolveName/resolveNamespace helpers
 * introduced during the duplication refactor.
 */
class SecurityRulesEngineRbacTest {

  private SecurityRulesEngine rulesEngine;

  @BeforeEach
  void setUp() {
    rulesEngine = new SecurityRulesEngine();
  }

  // --- analyzeClusterRole ---

  @Test
  void analyzeClusterRole_withWildcardVerb_returnsRbac001Finding() {
    V1ClusterRole clusterRole = clusterRoleWithVerbs(List.of("*"));

    List<SecurityFinding> findings = rulesEngine.analyzeClusterRole(clusterRole);

    assertThat(findings).isNotEmpty();
    assertThat(findings).anyMatch(f ->
        f.getRuleId().equals("RBAC001") &&
        f.getSeverity() == Severity.HIGH &&
        f.getResourceType().equals("ClusterRole"));
  }

  @Test
  void analyzeClusterRole_withWildcardResource_returnsRbac001Finding() {
    V1ClusterRole clusterRole = clusterRoleWithResources(List.of("*"));

    List<SecurityFinding> findings = rulesEngine.analyzeClusterRole(clusterRole);

    assertThat(findings).isNotEmpty();
    assertThat(findings).anyMatch(f -> f.getRuleId().equals("RBAC001"));
  }

  @Test
  void analyzeClusterRole_withRestrictedPermissions_returnsNoFindings() {
    V1ClusterRole clusterRole = clusterRoleWithVerbs(List.of("get", "list"));

    List<SecurityFinding> findings = rulesEngine.analyzeClusterRole(clusterRole);

    assertThat(findings).noneMatch(f -> f.getRuleId().equals("RBAC001"));
  }

  @Test
  void analyzeClusterRole_withNullInput_returnsEmptyList() {
    assertThat(rulesEngine.analyzeClusterRole(null)).isEmpty();
  }

  @Test
  void analyzeClusterRole_withNullMetadata_returnsEmptyList() {
    V1ClusterRole clusterRole = new V1ClusterRole();
    assertThat(rulesEngine.analyzeClusterRole(clusterRole)).isEmpty();
  }

  @Test
  void analyzeClusterRole_withNullRules_returnsEmptyList() {
    V1ClusterRole clusterRole = new V1ClusterRole();
    clusterRole.setMetadata(new V1ObjectMeta().name("test"));
    assertThat(rulesEngine.analyzeClusterRole(clusterRole)).isEmpty();
  }

  // --- resolveName / resolveNamespace via analyzeRole ---

  @Test
  void analyzeRole_withNullName_usesUnknownFallback() {
    V1Role role = new V1Role();
    V1ObjectMeta meta = new V1ObjectMeta();
    meta.setName(null);
    meta.setNamespace("default");
    role.setMetadata(meta);
    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(List.of("*"));
    role.setRules(List.of(rule));

    List<SecurityFinding> findings = rulesEngine.analyzeRole(role);

    assertThat(findings).isNotEmpty();
    assertThat(findings.get(0).getResourceName()).isEqualTo("unknown");
  }

  @Test
  void analyzeRole_withNullNamespace_usesDefaultFallback() {
    V1Role role = new V1Role();
    V1ObjectMeta meta = new V1ObjectMeta();
    meta.setName("my-role");
    meta.setNamespace(null);
    role.setMetadata(meta);
    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(List.of("*"));
    role.setRules(List.of(rule));

    List<SecurityFinding> findings = rulesEngine.analyzeRole(role);

    assertThat(findings).isNotEmpty();
    assertThat(findings.get(0).getNamespace()).isEqualTo("default");
  }

  @Test
  void analyzeClusterRole_withNullName_usesUnknownFallback() {
    V1ClusterRole clusterRole = new V1ClusterRole();
    V1ObjectMeta meta = new V1ObjectMeta();
    meta.setName(null);
    clusterRole.setMetadata(meta);
    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(List.of("*"));
    clusterRole.setRules(List.of(rule));

    List<SecurityFinding> findings = rulesEngine.analyzeClusterRole(clusterRole);

    assertThat(findings).isNotEmpty();
    assertThat(findings.get(0).getResourceName()).isEqualTo("unknown");
  }

  // --- Helpers ---

  private V1ClusterRole clusterRoleWithVerbs(List<String> verbs) {
    V1ClusterRole clusterRole = new V1ClusterRole();
    V1ObjectMeta meta = new V1ObjectMeta();
    meta.setName("test-cluster-role");
    clusterRole.setMetadata(meta);
    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(verbs);
    rule.setResources(List.of("pods"));
    rule.setApiGroups(List.of(""));
    clusterRole.setRules(List.of(rule));
    return clusterRole;
  }

  private V1ClusterRole clusterRoleWithResources(List<String> resources) {
    V1ClusterRole clusterRole = new V1ClusterRole();
    V1ObjectMeta meta = new V1ObjectMeta();
    meta.setName("test-cluster-role");
    clusterRole.setMetadata(meta);
    V1PolicyRule rule = new V1PolicyRule();
    rule.setVerbs(List.of("get"));
    rule.setResources(resources);
    rule.setApiGroups(List.of(""));
    clusterRole.setRules(List.of(rule));
    return clusterRole;
  }
}
