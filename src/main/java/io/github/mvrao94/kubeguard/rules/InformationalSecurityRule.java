package io.github.mvrao94.kubeguard.rules;

import java.util.Collections;
import java.util.List;

/**
 * Base class for informational security rules that provide context (e.g. NVD, MITRE)
 * but do not evaluate resources directly. Eliminates copy-pasted no-op overrides.
 */
public abstract class InformationalSecurityRule implements SecurityRule {

  @Override
  public List<RuleViolation> evaluate(Object resource) {
    return Collections.emptyList();
  }

  @Override
  public boolean appliesTo(Class<?> resourceType) {
    return false;
  }

  @Override
  public List<Class<?>> getSupportedResourceTypes() {
    return Collections.emptyList();
  }
}
