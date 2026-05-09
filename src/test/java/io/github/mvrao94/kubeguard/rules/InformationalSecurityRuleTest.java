package io.github.mvrao94.kubeguard.rules;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InformationalSecurityRuleTest {

  private InformationalSecurityRule rule;

  @BeforeEach
  void setUp() {
    // Concrete anonymous subclass — only getMetadata() needs implementing
    rule = new InformationalSecurityRule() {
      @Override
      public RuleMetadata getMetadata() {
        RuleMetadata meta = new RuleMetadata();
        meta.setId("TEST-001");
        meta.setTitle("Test Rule");
        return meta;
      }
    };
  }

  @Test
  void evaluate_alwaysReturnsEmptyList() {
    assertThat(rule.evaluate(new Object())).isEmpty();
    assertThat(rule.evaluate(null)).isEmpty();
  }

  @Test
  void appliesTo_alwaysReturnsFalse() {
    assertThat(rule.appliesTo(String.class)).isFalse();
    assertThat(rule.appliesTo(Object.class)).isFalse();
  }

  @Test
  void getSupportedResourceTypes_alwaysReturnsEmptyList() {
    List<Class<?>> types = rule.getSupportedResourceTypes();
    assertThat(types).isEmpty();
  }

  @Test
  void getMetadata_returnsConfiguredMetadata() {
    assertThat(rule.getMetadata().getId()).isEqualTo("TEST-001");
    assertThat(rule.getMetadata().getTitle()).isEqualTo("Test Rule");
  }
}
