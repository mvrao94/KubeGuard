package io.github.mvrao94.kubeguard.rules;

import java.util.List;

/**
 * Base interface for all security rules.
 * Extensible design allows for dynamic rule loading and custom rule implementations.
 */
public interface SecurityRule {
  
  /**
   * Get rule metadata including compliance mappings
   */
  RuleMetadata getMetadata();
  
  /**
   * Evaluate the rule against a Kubernetes resource
   * @param resource The Kubernetes resource to evaluate
   * @return List of violations found, empty if compliant
   */
  List<RuleViolation> evaluate(Object resource);
  
  /**
   * Check if this rule applies to the given resource type
   */
  boolean appliesTo(Class<?> resourceType);
  
  /**
   * Get the resource types this rule can evaluate
   */
  List<Class<?>> getSupportedResourceTypes();
}
