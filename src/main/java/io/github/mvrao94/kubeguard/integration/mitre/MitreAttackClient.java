package io.github.mvrao94.kubeguard.integration.mitre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Client for MITRE ATT&CK framework Provides Kubernetes-specific attack techniques Optimized with
 * caching and fast lookups
 */
@Service
public class MitreAttackClient {

  private static final Logger logger = LoggerFactory.getLogger(MitreAttackClient.class);

  @Getter
  @Value("${kubeguard.mitre.enabled:true}")
  private boolean enabled;

  // Cached immutable collections for performance
  private volatile List<MitreTechnique> cachedTechniques;
  private volatile Map<String, MitreTechnique> techniquesById;
  private volatile Map<String, List<MitreTechnique>> techniquesByTactic;

  /** Get Kubernetes-relevant MITRE ATT&CK techniques Uses cached immutable list for performance */
  public List<MitreTechnique> getKubernetesTechniques() {
    if (!enabled) {
      logger.info("MITRE ATT&CK integration is disabled");
      return List.of();
    }

    // Double-checked locking for thread-safe lazy initialization
    if (cachedTechniques == null) {
      synchronized (this) {
        if (cachedTechniques == null) {
          // Fully initialize all fields before assigning to volatile
          List<MitreTechnique> techniques = loadTechniques();
          Map<String, MitreTechnique> idMap =
              techniques.stream()
                  .collect(
                      Collectors.toConcurrentMap(
                          MitreTechnique::getTechniqueId, Function.identity()));
          Map<String, List<MitreTechnique>> tacticMap =
              techniques.stream()
                  .collect(Collectors.groupingByConcurrent(MitreTechnique::getTactic));

          // Assign to volatile fields as the last step
          techniquesById = idMap;
          techniquesByTactic = tacticMap;
          cachedTechniques = techniques;

          logger.info("Loaded and cached {} MITRE ATT&CK techniques", cachedTechniques.size());
        }
      }
    }

    return cachedTechniques;
  }

  /** Load techniques with optimized data structure */
  private List<MitreTechnique> loadTechniques() {
    logger.info("Loading MITRE ATT&CK techniques for Kubernetes...");

    List<MitreTechnique> techniques = new ArrayList<>(20); // Pre-size for performance

    // Use data-driven approach with records for better performance
    TechniqueData[] techniqueData = {
      // Initial Access
      new TechniqueData(
          "T1190",
          "Exploit Public-Facing Application",
          "Initial Access",
          "Adversaries may exploit vulnerabilities in public-facing Kubernetes services",
          List.of("Service", "Ingress"),
          List.of("Use network segmentation", "Implement WAF", "Regular patching")),

      // Execution
      new TechniqueData(
          "T1609",
          "Container Administration Command",
          "Execution",
          "Adversaries may abuse container administration commands like kubectl exec",
          List.of("Pod", "Container"),
          List.of("Restrict kubectl access", "Enable audit logging", "Use RBAC")),
      new TechniqueData(
          "T1610",
          "Deploy Container",
          "Execution",
          "Adversaries may deploy malicious containers to execute code",
          List.of("Pod", "Deployment"),
          List.of("Image scanning", "Admission controllers", "Registry restrictions")),

      // Persistence
      new TechniqueData(
          "T1525",
          "Implant Internal Image",
          "Persistence",
          "Adversaries may implant malicious code in container images",
          List.of("Image", "Registry"),
          List.of("Image signing", "Vulnerability scanning", "Registry access control")),
      new TechniqueData(
          "T1053",
          "Scheduled Task/Job",
          "Persistence",
          "Adversaries may use CronJobs for persistence",
          List.of("CronJob", "Job"),
          List.of("Monitor CronJob creation", "Restrict scheduling permissions")),

      // Privilege Escalation
      new TechniqueData(
          "T1611",
          "Escape to Host",
          "Privilege Escalation",
          "Adversaries may break out of containers to gain host access",
          List.of("Pod", "Container"),
          List.of("Disable privileged containers", "Use seccomp/AppArmor", "Drop capabilities")),
      new TechniqueData(
          "T1548",
          "Abuse Elevation Control Mechanism",
          "Privilege Escalation",
          "Adversaries may abuse sudo or setuid to escalate privileges",
          List.of("Pod", "SecurityContext"),
          List.of("Run as non-root", "Drop all capabilities", "Read-only filesystem")),

      // Defense Evasion
      new TechniqueData(
          "T1562",
          "Impair Defenses",
          "Defense Evasion",
          "Adversaries may disable security tools or logging",
          List.of("Pod", "ServiceAccount"),
          List.of("Protect security pods", "Immutable infrastructure", "RBAC restrictions")),
      new TechniqueData(
          "T1070",
          "Indicator Removal",
          "Defense Evasion",
          "Adversaries may delete logs or modify audit trails",
          List.of("Pod", "PersistentVolume"),
          List.of("Centralized logging", "Immutable logs", "Audit policies")),

      // Credential Access
      new TechniqueData(
          "T1552",
          "Unsecured Credentials",
          "Credential Access",
          "Adversaries may search for exposed credentials in configs or secrets",
          List.of("Secret", "ConfigMap", "Pod"),
          List.of("Encrypt secrets", "Use secret management tools", "Avoid env vars for secrets")),
      new TechniqueData(
          "T1078",
          "Valid Accounts",
          "Credential Access",
          "Adversaries may use stolen service account tokens",
          List.of("ServiceAccount", "Secret"),
          List.of("Rotate tokens", "Limit token scope", "Disable auto-mounting")),

      // Discovery
      new TechniqueData(
          "T1613",
          "Container and Resource Discovery",
          "Discovery",
          "Adversaries may enumerate containers and resources",
          List.of("Pod", "Service", "Namespace"),
          List.of("Network policies", "RBAC restrictions", "Audit logging")),
      new TechniqueData(
          "T1046",
          "Network Service Discovery",
          "Discovery",
          "Adversaries may scan for services in the cluster",
          List.of("Service", "NetworkPolicy"),
          List.of("Network segmentation", "Service mesh", "Egress filtering")),

      // Lateral Movement
      new TechniqueData(
          "T1021",
          "Remote Services",
          "Lateral Movement",
          "Adversaries may use remote services to move laterally",
          List.of("Service", "Pod"),
          List.of("Network policies", "Service mesh", "mTLS")),
      new TechniqueData(
          "T1534",
          "Internal Spearphishing",
          "Lateral Movement",
          "Adversaries may use compromised pods to attack other pods",
          List.of("Pod", "NetworkPolicy"),
          List.of("Network segmentation", "Zero trust", "Micro-segmentation")),

      // Collection
      new TechniqueData(
          "T1530",
          "Data from Cloud Storage",
          "Collection",
          "Adversaries may access data from persistent volumes",
          List.of("PersistentVolume", "PersistentVolumeClaim"),
          List.of("Encryption at rest", "Access controls", "Audit logging")),

      // Impact
      new TechniqueData(
          "T1496",
          "Resource Hijacking",
          "Impact",
          "Adversaries may use cluster resources for cryptomining",
          List.of("Pod", "ResourceQuota"),
          List.of("Resource limits", "Monitoring", "Admission controllers")),
      new TechniqueData(
          "T1499",
          "Endpoint Denial of Service",
          "Impact",
          "Adversaries may cause DoS by exhausting resources",
          List.of("Pod", "Service", "ResourceQuota"),
          List.of("Resource quotas", "Rate limiting", "PodDisruptionBudgets")),
      new TechniqueData(
          "T1485",
          "Data Destruction",
          "Impact",
          "Adversaries may destroy data in persistent volumes",
          List.of("PersistentVolume", "StatefulSet"),
          List.of("Backups", "RBAC", "Immutable infrastructure")),

      // Network
      new TechniqueData(
          "T1599",
          "Network Boundary Bridging",
          "Defense Evasion",
          "Adversaries may bridge network boundaries using compromised pods",
          List.of("Pod", "NetworkPolicy", "Service"),
          List.of("Network policies", "Service mesh", "Egress controls")),
      new TechniqueData(
          "T1040",
          "Network Sniffing",
          "Credential Access",
          "Adversaries may sniff network traffic for credentials",
          List.of("Pod", "NetworkPolicy"),
          List.of("mTLS", "Network encryption", "Network policies"))
    };

    // Create techniques using optimized builder
    for (TechniqueData data : techniqueData) {
      techniques.add(createTechnique(data));
    }

    return Collections.unmodifiableList(techniques);
  }

  /** Get technique by ID - O(1) lookup using cached map */
  public MitreTechnique getTechniqueById(String techniqueId) {
    if (!enabled || techniqueId == null) {
      return null;
    }
    // Ensure techniques are loaded
    getKubernetesTechniques();
    return techniquesById.get(techniqueId);
  }

  /** Get techniques by tactic - O(1) lookup using cached map */
  public List<MitreTechnique> getTechniquesByTactic(String tactic) {
    if (!enabled || tactic == null) {
      return List.of();
    }
    // Ensure techniques are loaded
    getKubernetesTechniques();
    return techniquesByTactic.getOrDefault(tactic, List.of());
  }

  /** Optimized technique creation */
  private MitreTechnique createTechnique(TechniqueData data) {
    MitreTechnique technique = new MitreTechnique(data.id, data.name, data.tactic);
    technique.setDescription(data.description);
    technique.setAppliesToKubernetes(true);
    technique.setKubernetesContext(data.description);
    technique.setKubernetesResources(data.k8sResources);
    technique.setMitigations(data.mitigations);
    technique.setPlatforms(List.of("Containers", "Kubernetes"));
    technique.setUrl("https://attack.mitre.org/techniques/" + data.id);
    return technique;
  }

  /** Data record for technique information - immutable and efficient */
  private record TechniqueData(
      String id,
      String name,
      String tactic,
      String description,
      List<String> k8sResources,
      List<String> mitigations) {}

  public boolean isEnabled() {
    return enabled;
  }
}
