package io.github.mvrao94.kubeguard.integration.mitre;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;

@ExtendWith(MockitoExtension.class)
class MitreIntegrationServiceTest {

  @Mock private MitreAttackClient mitreClient;
  @Mock private MitreRuleConverter ruleConverter;
  @Mock private RuleRegistry ruleRegistry;

  @InjectMocks private MitreIntegrationService service;

  @Mock private SecurityRule mockRule;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "autoLoad", false);
  }

  @Test
  void init_whenAutoLoadAndClientEnabled_loadsTechniques() {
    ReflectionTestUtils.setField(service, "autoLoad", true);
    when(mitreClient.isEnabled()).thenReturn(true);
    when(mitreClient.getKubernetesTechniques()).thenReturn(Collections.emptyList());
    when(ruleConverter.convertToRules(any())).thenReturn(Collections.emptyList());

    service.init();

    verify(mitreClient).getKubernetesTechniques();
  }

  @Test
  void init_whenAutoLoadFalse_doesNotLoadTechniques() {
    ReflectionTestUtils.setField(service, "autoLoad", false);

    service.init();

    verifyNoInteractions(mitreClient);
  }

  @Test
  void init_whenClientDisabled_doesNotLoadTechniques() {
    ReflectionTestUtils.setField(service, "autoLoad", true);
    when(mitreClient.isEnabled()).thenReturn(false);

    service.init();

    verify(mitreClient, never()).getKubernetesTechniques();
  }

  @Test
  void loadTechniques_whenEnabled_returnsRuleCount() {
    when(mitreClient.isEnabled()).thenReturn(true);
    when(mitreClient.getKubernetesTechniques()).thenReturn(Collections.emptyList());
    when(ruleConverter.convertToRules(any())).thenReturn(List.of(mockRule));

    int count = service.loadTechniques();

    assertThat(count).isEqualTo(1);
    verify(ruleRegistry).registerRules(List.of(mockRule));
  }

  @Test
  void loadTechniques_whenDisabled_returnsZero() {
    when(mitreClient.isEnabled()).thenReturn(false);

    int count = service.loadTechniques();

    assertThat(count).isZero();
    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void loadTechniquesByTactic_whenEnabled_returnsRuleCount() {
    when(mitreClient.isEnabled()).thenReturn(true);
    when(mitreClient.getTechniquesByTactic("Execution")).thenReturn(Collections.emptyList());
    when(ruleConverter.convertToRules(any())).thenReturn(List.of(mockRule, mockRule));

    int count = service.loadTechniquesByTactic("Execution");

    assertThat(count).isEqualTo(2);
  }

  @Test
  void getStats_returnsCorrectCounts() {
    when(mitreClient.isEnabled()).thenReturn(true);
    when(ruleRegistry.getAllRules()).thenReturn(Collections.emptyList());

    MitreIntegrationService.MitreStats stats = service.getStats();

    assertThat(stats.mitreEnabled()).isTrue();
    assertThat(stats.mitreRuleCount()).isZero();
    assertThat(stats.rulesByTactic()).isEmpty();
  }

  @Test
  void getAvailableTactics_returnsAllTenTactics() {
    List<String> tactics = service.getAvailableTactics();
    assertThat(tactics).hasSize(10).contains("Initial Access", "Execution", "Persistence",
        "Privilege Escalation", "Defense Evasion", "Credential Access",
        "Discovery", "Lateral Movement", "Collection", "Impact");
  }

  @Test
  void getIntegrationName_returnsMitreAttack() {
    assertThat(service.getIntegrationName()).isEqualTo("MITRE ATT&CK");
  }
}
