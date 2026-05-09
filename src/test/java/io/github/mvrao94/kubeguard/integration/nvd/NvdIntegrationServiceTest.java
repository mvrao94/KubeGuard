package io.github.mvrao94.kubeguard.integration.nvd;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;

@ExtendWith(MockitoExtension.class)
class NvdIntegrationServiceTest {

  @Mock private NvdClient nvdClient;
  @Mock private NvdRuleConverter ruleConverter;
  @Mock private RuleRegistry ruleRegistry;

  @InjectMocks private NvdIntegrationService service;

  @Mock private SecurityRule mockRule;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(service, "syncEnabled", false);
    ReflectionTestUtils.setField(service, "resultsPerPage", 20);
  }

  @Test
  void syncVulnerabilities_whenEnabled_returnsRuleCount() {
    when(nvdClient.isEnabled()).thenReturn(true);
    when(nvdClient.searchKubernetesVulnerabilities(20)).thenReturn(List.of());
    when(ruleConverter.convertToRules(any())).thenReturn(List.of(mockRule));

    int count = service.syncVulnerabilities();

    assertThat(count).isEqualTo(1);
    verify(ruleRegistry).registerRules(List.of(mockRule));
  }

  @Test
  void syncVulnerabilities_whenDisabled_returnsZero() {
    when(nvdClient.isEnabled()).thenReturn(false);

    int count = service.syncVulnerabilities();

    assertThat(count).isZero();
    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void syncRecentVulnerabilities_whenEnabled_returnsRuleCount() {
    when(nvdClient.isEnabled()).thenReturn(true);
    when(nvdClient.getRecentVulnerabilities(7, 20)).thenReturn(List.of());
    when(ruleConverter.convertToRules(any())).thenReturn(List.of(mockRule, mockRule));

    int count = service.syncRecentVulnerabilities(7);

    assertThat(count).isEqualTo(2);
  }

  @Test
  void scheduledSync_whenSyncDisabled_doesNothing() {
    ReflectionTestUtils.setField(service, "syncEnabled", false);

    service.scheduledSync();

    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void scheduledSync_whenClientDisabled_doesNothing() {
    ReflectionTestUtils.setField(service, "syncEnabled", true);
    when(nvdClient.isEnabled()).thenReturn(false);

    service.scheduledSync();

    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void scheduledSync_whenBothEnabled_syncsRecentVulnerabilities() {
    ReflectionTestUtils.setField(service, "syncEnabled", true);
    when(nvdClient.isEnabled()).thenReturn(true);
    when(nvdClient.getRecentVulnerabilities(7, 20)).thenReturn(Collections.emptyList());
    when(ruleConverter.convertToRules(any())).thenReturn(Collections.emptyList());

    service.scheduledSync();

    verify(nvdClient).getRecentVulnerabilities(7, 20);
  }

  @Test
  void getStats_returnsCorrectCounts() {
    when(nvdClient.isEnabled()).thenReturn(true);
    when(ruleRegistry.getAllRules()).thenReturn(Collections.emptyList());

    NvdIntegrationService.NvdSyncStats stats = service.getStats();

    assertThat(stats.nvdEnabled()).isTrue();
    assertThat(stats.nvdRuleCount()).isZero();
  }

  @Test
  void getIntegrationName_returnsNVD() {
    assertThat(service.getIntegrationName()).isEqualTo("NVD");
  }
}
