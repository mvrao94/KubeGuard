package io.github.mvrao94.kubeguard.integration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.mvrao94.kubeguard.rules.RuleRegistry;
import io.github.mvrao94.kubeguard.rules.SecurityRule;

@ExtendWith(MockitoExtension.class)
class BaseIntegrationServiceTest {

  @Mock private RuleRegistry ruleRegistry;
  @Mock private SecurityRule mockRule;

  private TestIntegrationService service;
  private boolean clientEnabled;

  @BeforeEach
  void setUp() {
    clientEnabled = true;
    service = new TestIntegrationService();
  }

  @Test
  void loadRules_whenClientEnabled_registersRulesAndReturnsCount() throws Exception {
    List<SecurityRule> rules = List.of(mockRule, mockRule);
    int count = service.loadRules(() -> rules, "test context");

    assertThat(count).isEqualTo(2);
    verify(ruleRegistry).registerRules(rules);
  }

  @Test
  void loadRules_whenClientDisabled_returnsZeroAndSkipsRegistration() {
    clientEnabled = false;
    int count = service.loadRules(() -> List.of(mockRule), "test context");

    assertThat(count).isZero();
    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void loadRules_whenSupplierThrows_returnsZeroAndDoesNotPropagate() {
    int count = service.loadRules(() -> { throw new RuntimeException("network error"); }, "test context");

    assertThat(count).isZero();
    verifyNoInteractions(ruleRegistry);
  }

  @Test
  void loadRules_withEmptyRuleList_returnsZero() throws Exception {
    int count = service.loadRules(List::of, "empty context");

    assertThat(count).isZero();
    verify(ruleRegistry).registerRules(List.of());
  }

  /** Minimal concrete subclass for testing the abstract base. */
  private class TestIntegrationService extends BaseIntegrationService {
    @Override protected RuleRegistry getRuleRegistry() { return ruleRegistry; }
    @Override protected boolean isClientEnabled() { return clientEnabled; }
    @Override protected String getIntegrationName() { return "TEST"; }
  }
}
