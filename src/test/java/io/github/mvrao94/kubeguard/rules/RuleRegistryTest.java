package io.github.mvrao94.kubeguard.rules;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleRegistryTest {

  private RuleRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new RuleRegistry();
  }

  private SecurityRule mockRule(String id, RuleSeverity severity, RuleCategory category, boolean enabled) {
    SecurityRule rule = mock(SecurityRule.class);
    RuleMetadata meta = new RuleMetadata();
    meta.setId(id);
    meta.setTitle("Title for " + id);
    meta.setSeverity(severity);
    meta.setCategory(category);
    meta.setEnabled(enabled);
    when(rule.getMetadata()).thenReturn(meta);
    when(rule.getSupportedResourceTypes()).thenReturn(List.of());
    return rule;
  }

  @Test
  void getEnabledRuleCount_delegatesToGetEnabledRules() {
    registry.registerRule(mockRule("R1", RuleSeverity.HIGH, RuleCategory.POD_SECURITY, true));
    registry.registerRule(mockRule("R2", RuleSeverity.LOW, RuleCategory.NETWORK_SECURITY, false));
    registry.registerRule(mockRule("R3", RuleSeverity.CRITICAL, RuleCategory.RBAC, true));

    assertThat(registry.getEnabledRuleCount()).isEqualTo(registry.getEnabledRules().size());
    assertThat(registry.getEnabledRuleCount()).isEqualTo(2);
  }

  @Test
  void getEnabledRuleCount_isZeroWhenNoRulesRegistered() {
    assertThat(registry.getEnabledRuleCount()).isZero();
  }

  @Test
  void getEnabledRules_excludesDisabledRules() {
    registry.registerRule(mockRule("ENABLED", RuleSeverity.HIGH, RuleCategory.POD_SECURITY, true));
    registry.registerRule(mockRule("DISABLED", RuleSeverity.HIGH, RuleCategory.POD_SECURITY, false));

    List<SecurityRule> enabled = registry.getEnabledRules();
    assertThat(enabled).hasSize(1);
    assertThat(enabled.get(0).getMetadata().getId()).isEqualTo("ENABLED");
  }

  @Test
  void registerRule_replacesExistingRuleWithSameId() {
    SecurityRule first = mockRule("DUP", RuleSeverity.LOW, RuleCategory.POD_SECURITY, true);
    SecurityRule second = mockRule("DUP", RuleSeverity.HIGH, RuleCategory.POD_SECURITY, true);

    registry.registerRule(first);
    registry.registerRule(second);

    assertThat(registry.getRuleCount()).isEqualTo(1);
    assertThat(registry.getRuleById("DUP")).isPresent();
  }

  @Test
  void clear_removesAllRules() {
    registry.registerRule(mockRule("R1", RuleSeverity.HIGH, RuleCategory.POD_SECURITY, true));
    registry.clear();

    assertThat(registry.getRuleCount()).isZero();
    assertThat(registry.getEnabledRuleCount()).isZero();
    assertThat(registry.getAllRules()).isEmpty();
  }
}
