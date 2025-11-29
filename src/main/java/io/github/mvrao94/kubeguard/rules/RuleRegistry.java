package io.github.mvrao94.kubeguard.rules;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Central registry for all security rules.
 * Supports dynamic rule registration, filtering, and querying.
 * Designed to scale to hundreds of rules efficiently.
 */
@Component
public class RuleRegistry {
  
  private static final Logger logger = LoggerFactory.getLogger(RuleRegistry.class);
  
  private final Map<String, SecurityRule> rulesById = new ConcurrentHashMap<>();
  private final Map<RuleCategory, List<SecurityRule>> rulesByCategory = new ConcurrentHashMap<>();
  private final Map<RuleSeverity, List<SecurityRule>> rulesBySeverity = new ConcurrentHashMap<>();
  private final Map<Class<?>, List<SecurityRule>> rulesByResourceType = new ConcurrentHashMap<>();
  
  /**
   * Register a new security rule
   */
  public void registerRule(SecurityRule rule) {
    RuleMetadata metadata = rule.getMetadata();
    String ruleId = metadata.getId();
    
    if (rulesById.containsKey(ruleId)) {
      logger.warn("Rule {} already registered, replacing", ruleId);
    }
    
    rulesById.put(ruleId, rule);
    
    // Index by category
    rulesByCategory
        .computeIfAbsent(metadata.getCategory(), k -> new ArrayList<>())
        .add(rule);
    
    // Index by severity
    rulesBySeverity
        .computeIfAbsent(metadata.getSeverity(), k -> new ArrayList<>())
        .add(rule);
    
    // Index by resource type
    for (Class<?> resourceType : rule.getSupportedResourceTypes()) {
      rulesByResourceType
          .computeIfAbsent(resourceType, k -> new ArrayList<>())
          .add(rule);
    }
    
    logger.info("Registered rule: {} - {} ({})", ruleId, metadata.getTitle(), metadata.getSeverity());
  }
  
  /**
   * Register multiple rules at once
   */
  public void registerRules(Collection<SecurityRule> rules) {
    rules.forEach(this::registerRule);
  }
  
  /**
   * Get all registered rules
   */
  public Collection<SecurityRule> getAllRules() {
    return Collections.unmodifiableCollection(rulesById.values());
  }
  
  /**
   * Get enabled rules only
   */
  public List<SecurityRule> getEnabledRules() {
    return rulesById.values().stream()
        .filter(rule -> rule.getMetadata().isEnabled())
        .collect(Collectors.toList());
  }
  
  /**
   * Get rule by ID
   */
  public Optional<SecurityRule> getRuleById(String ruleId) {
    return Optional.ofNullable(rulesById.get(ruleId));
  }
  
  /**
   * Get rules by category
   */
  public List<SecurityRule> getRulesByCategory(RuleCategory category) {
    return rulesByCategory.getOrDefault(category, Collections.emptyList());
  }
  
  /**
   * Get rules by severity
   */
  public List<SecurityRule> getRulesBySeverity(RuleSeverity severity) {
    return rulesBySeverity.getOrDefault(severity, Collections.emptyList());
  }
  
  /**
   * Get rules applicable to a resource type
   */
  public List<SecurityRule> getRulesForResourceType(Class<?> resourceType) {
    return rulesByResourceType.getOrDefault(resourceType, Collections.emptyList());
  }
  
  /**
   * Get rules by compliance framework
   */
  public List<SecurityRule> getRulesByCisBenchmark(String benchmarkId) {
    return rulesById.values().stream()
        .filter(rule -> {
          Set<String> benchmarks = rule.getMetadata().getCisBenchmarks();
          return benchmarks != null && benchmarks.contains(benchmarkId);
        })
        .collect(Collectors.toList());
  }
  
  /**
   * Get rules by NSA/CISA guideline
   */
  public List<SecurityRule> getRulesByNsaCisa(String guidelineId) {
    return rulesById.values().stream()
        .filter(rule -> {
          Set<String> guidelines = rule.getMetadata().getNsaCisaGuidelines();
          return guidelines != null && guidelines.contains(guidelineId);
        })
        .collect(Collectors.toList());
  }
  
  /**
   * Get rules by MITRE ATT&CK technique
   */
  public List<SecurityRule> getRulesByMitreAttack(String techniqueId) {
    return rulesById.values().stream()
        .filter(rule -> {
          Set<String> techniques = rule.getMetadata().getMitreAttack();
          return techniques != null && techniques.contains(techniqueId);
        })
        .collect(Collectors.toList());
  }
  
  /**
   * Get total rule count
   */
  public int getRuleCount() {
    return rulesById.size();
  }
  
  /**
   * Get enabled rule count
   */
  public int getEnabledRuleCount() {
    return (int) rulesById.values().stream()
        .filter(rule -> rule.getMetadata().isEnabled())
        .count();
  }
  
  /**
   * Get rule statistics
   */
  public Map<String, Object> getStatistics() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalRules", getRuleCount());
    stats.put("enabledRules", getEnabledRuleCount());
    stats.put("rulesByCategory", getRuleCategoryStats());
    stats.put("rulesBySeverity", getRuleSeverityStats());
    return stats;
  }
  
  private Map<RuleCategory, Integer> getRuleCategoryStats() {
    return rulesByCategory.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().size()
        ));
  }
  
  private Map<RuleSeverity, Integer> getRuleSeverityStats() {
    return rulesBySeverity.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> e.getValue().size()
        ));
  }
  
  /**
   * Clear all rules (useful for testing)
   */
  public void clear() {
    rulesById.clear();
    rulesByCategory.clear();
    rulesBySeverity.clear();
    rulesByResourceType.clear();
  }
}
