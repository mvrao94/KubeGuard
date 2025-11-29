package io.github.mvrao94.kubeguard.rules;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * High-performance rule engine that evaluates resources against security rules.
 * Designed to handle hundreds of rules efficiently with parallel processing.
 */
@Service
public class RuleEngine {
  
  private static final Logger logger = LoggerFactory.getLogger(RuleEngine.class);
  
  @Autowired
  private RuleRegistry ruleRegistry;
  
  private final ExecutorService executorService;
  
  public RuleEngine() {
    // Use virtual threads for efficient parallel rule evaluation
    this.executorService = Executors.newVirtualThreadPerTaskExecutor();
  }
  
  /**
   * Evaluate a single resource against all applicable rules
   */
  public List<RuleViolation> evaluateResource(Object resource) {
    if (resource == null) {
      return Collections.emptyList();
    }
    
    Class<?> resourceType = resource.getClass();
    List<SecurityRule> applicableRules = ruleRegistry.getRulesForResourceType(resourceType)
        .stream()
        .filter(rule -> rule.getMetadata().isEnabled())
        .collect(Collectors.toList());
    
    logger.debug("Evaluating {} rules for resource type {}", 
        applicableRules.size(), resourceType.getSimpleName());
    
    return evaluateWithRules(resource, applicableRules);
  }
  
  /**
   * Evaluate multiple resources in parallel
   */
  public Map<Object, List<RuleViolation>> evaluateResources(List<Object> resources) {
    Map<Object, List<RuleViolation>> results = new ConcurrentHashMap<>();
    
    List<CompletableFuture<Void>> futures = resources.stream()
        .map(resource -> CompletableFuture.runAsync(() -> {
          List<RuleViolation> violations = evaluateResource(resource);
          if (!violations.isEmpty()) {
            results.put(resource, violations);
          }
        }, executorService))
        .collect(Collectors.toList());
    
    // Wait for all evaluations to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    return results;
  }
  
  /**
   * Evaluate resource with specific rules
   */
  public List<RuleViolation> evaluateWithRules(Object resource, List<SecurityRule> rules) {
    List<RuleViolation> allViolations = new CopyOnWriteArrayList<>();
    
    // Parallel rule evaluation for performance
    List<CompletableFuture<Void>> futures = rules.stream()
        .map(rule -> CompletableFuture.runAsync(() -> {
          try {
            List<RuleViolation> violations = rule.evaluate(resource);
            if (violations != null && !violations.isEmpty()) {
              allViolations.addAll(violations);
            }
          } catch (Exception e) {
            logger.error("Error evaluating rule {}: {}", 
                rule.getMetadata().getId(), e.getMessage(), e);
          }
        }, executorService))
        .collect(Collectors.toList());
    
    // Wait for all rule evaluations
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    return allViolations;
  }
  
  /**
   * Evaluate with compliance framework filter
   */
  public List<RuleViolation> evaluateForCompliance(Object resource, String framework, String id) {
    List<SecurityRule> rules = switch (framework.toUpperCase()) {
      case "CIS" -> ruleRegistry.getRulesByCisBenchmark(id);
      case "NSA_CISA" -> ruleRegistry.getRulesByNsaCisa(id);
      case "MITRE" -> ruleRegistry.getRulesByMitreAttack(id);
      default -> Collections.emptyList();
    };
    
    return evaluateWithRules(resource, rules);
  }
  
  /**
   * Get rule engine statistics
   */
  public Map<String, Object> getStatistics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("ruleRegistry", ruleRegistry.getStatistics());
    stats.put("executorService", "VirtualThreadPerTaskExecutor");
    return stats;
  }
  
  public void shutdown() {
    executorService.shutdown();
  }
}
