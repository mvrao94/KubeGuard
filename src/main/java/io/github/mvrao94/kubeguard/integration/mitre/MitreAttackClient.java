package io.github.mvrao94.kubeguard.integration.mitre;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Client for MITRE ATT&CK framework
 * Provides Kubernetes-specific attack techniques
 */
@Service
public class MitreAttackClient {
  
  private static final Logger logger = LoggerFactory.getLogger(MitreAttackClient.class);
  
  @Value("${kubeguard.mitre.enabled:true}")
  private boolean enabled;
  
  /**
   * Get Kubernetes-relevant MITRE ATT&CK techniques
   * Based on MITRE ATT&CK for Containers matrix
   */
  public List<MitreTechnique> getKubernetesTechniques() {
    if (!enabled) {
      logger.info("MITRE ATT&CK integration is disabled");
      return List.of();
    }
    
    logger.info("Loading MITRE ATT&CK techniques for Kubernetes...");
    
    List<MitreTechnique> techniques = new ArrayList<>();
    
    // Initial Access
    techniques.add(createTechnique(
        "T1190", "Exploit Public-Facing Application", "Initial Access",
        "Adversaries may exploit vulnerabilities in public-facing Kubernetes services",
        List.of("Service", "Ingress"),
        List.of("Use network segmentation", "Implement WAF", "Regular patching")
    ));
    
    // Execution
    techniques.add(createTechnique(
        "T1609", "Container Administration Command", "Execution",
        "Adversaries may abuse container administration commands like kubectl exec",
        List.of("Pod", "Container"),
        List.of("Restrict kubectl access", "Enable audit logging", "Use RBAC")
    ));
    
    techniques.add(createTechnique(
        "T1610", "Deploy Container", "Execution",
        "Adversaries may deploy malicious containers to execute code",
        List.of("Pod", "Deployment"),
        List.of("Image scanning", "Admission controllers", "Registry restrictions")
    ));
    
    // Persistence
    techniques.add(createTechnique(
        "T1525", "Implant Internal Image", "Persistence",
        "Adversaries may implant malicious code in container images",
        List.of("Image", "Registry"),
        List.of("Image signing", "Vulnerability scanning", "Registry access control")
    ));
    
    techniques.add(createTechnique(
        "T1053", "Scheduled Task/Job", "Persistence",
        "Adversaries may use CronJobs for persistence",
        List.of("CronJob", "Job"),
        List.of("Monitor CronJob creation", "Restrict scheduling permissions")
    ));
    
    // Privilege Escalation
    techniques.add(createTechnique(
        "T1611", "Escape to Host", "Privilege Escalation",
        "Adversaries may break out of containers to gain host access",
        List.of("Pod", "Container"),
        List.of("Disable privileged containers", "Use seccomp/AppArmor", "Drop capabilities")
    ));
    
    techniques.add(createTechnique(
        "T1548", "Abuse Elevation Control Mechanism", "Privilege Escalation",
        "Adversaries may abuse sudo or setuid to escalate privileges",
        List.of("Pod", "SecurityContext"),
        List.of("Run as non-root", "Drop all capabilities", "Read-only filesystem")
    ));
    
    // Defense Evasion
    techniques.add(createTechnique(
        "T1562", "Impair Defenses", "Defense Evasion",
        "Adversaries may disable security tools or logging",
        List.of("Pod", "ServiceAccount"),
        List.of("Protect security pods", "Immutable infrastructure", "RBAC restrictions")
    ));
    
    techniques.add(createTechnique(
        "T1070", "Indicator Removal", "Defense Evasion",
        "Adversaries may delete logs or modify audit trails",
        List.of("Pod", "PersistentVolume"),
        List.of("Centralized logging", "Immutable logs", "Audit policies")
    ));
    
    // Credential Access
    techniques.add(createTechnique(
        "T1552", "Unsecured Credentials", "Credential Access",
        "Adversaries may search for exposed credentials in configs or secrets",
        List.of("Secret", "ConfigMap", "Pod"),
        List.of("Encrypt secrets", "Use secret management tools", "Avoid env vars for secrets")
    ));
    
    techniques.add(createTechnique(
        "T1078", "Valid Accounts", "Credential Access",
        "Adversaries may use stolen service account tokens",
        List.of("ServiceAccount", "Secret"),
        List.of("Rotate tokens", "Limit token scope", "Disable auto-mounting")
    ));
    
    // Discovery
    techniques.add(createTechnique(
        "T1613", "Container and Resource Discovery", "Discovery",
        "Adversaries may enumerate containers and resources",
        List.of("Pod", "Service", "Namespace"),
        List.of("Network policies", "RBAC restrictions", "Audit logging")
    ));
    
    techniques.add(createTechnique(
        "T1046", "Network Service Discovery", "Discovery",
        "Adversaries may scan for services in the cluster",
        List.of("Service", "NetworkPolicy"),
        List.of("Network segmentation", "Service mesh", "Egress filtering")
    ));
    
    // Lateral Movement
    techniques.add(createTechnique(
        "T1021", "Remote Services", "Lateral Movement",
        "Adversaries may use remote services to move laterally",
        List.of("Service", "Pod"),
        List.of("Network policies", "Service mesh", "mTLS")
    ));
    
    techniques.add(createTechnique(
        "T1534", "Internal Spearphishing", "Lateral Movement",
        "Adversaries may use compromised pods to attack other pods",
        List.of("Pod", "NetworkPolicy"),
        List.of("Network segmentation", "Zero trust", "Micro-segmentation")
    ));
    
    // Collection
    techniques.add(createTechnique(
        "T1530", "Data from Cloud Storage", "Collection",
        "Adversaries may access data from persistent volumes",
        List.of("PersistentVolume", "PersistentVolumeClaim"),
        List.of("Encryption at rest", "Access controls", "Audit logging")
    ));
    
    // Impact
    techniques.add(createTechnique(
        "T1496", "Resource Hijacking", "Impact",
        "Adversaries may use cluster resources for cryptomining",
        List.of("Pod", "ResourceQuota"),
        List.of("Resource limits", "Monitoring", "Admission controllers")
    ));
    
    techniques.add(createTechnique(
        "T1499", "Endpoint Denial of Service", "Impact",
        "Adversaries may cause DoS by exhausting resources",
        List.of("Pod", "Service", "ResourceQuota"),
        List.of("Resource quotas", "Rate limiting", "PodDisruptionBudgets")
    ));
    
    techniques.add(createTechnique(
        "T1485", "Data Destruction", "Impact",
        "Adversaries may destroy data in persistent volumes",
        List.of("PersistentVolume", "StatefulSet"),
        List.of("Backups", "RBAC", "Immutable infrastructure")
    ));
    
    // Network
    techniques.add(createTechnique(
        "T1599", "Network Boundary Bridging", "Defense Evasion",
        "Adversaries may bridge network boundaries using compromised pods",
        List.of("Pod", "NetworkPolicy", "Service"),
        List.of("Network policies", "Service mesh", "Egress controls")
    ));
    
    techniques.add(createTechnique(
        "T1040", "Network Sniffing", "Credential Access",
        "Adversaries may sniff network traffic for credentials",
        List.of("Pod", "NetworkPolicy"),
        List.of("mTLS", "Network encryption", "Network policies")
    ));
    
    logger.info("Loaded {} MITRE ATT&CK techniques", techniques.size());
    
    return techniques;
  }
  
  private MitreTechnique createTechnique(
      String id, 
      String name, 
      String tactic,
      String description,
      List<String> k8sResources,
      List<String> mitigations) {
    
    MitreTechnique technique = new MitreTechnique(id, name, tactic);
    technique.setDescription(description);
    technique.setAppliesToKubernetes(true);
    technique.setKubernetesContext(description);
    technique.setKubernetesResources(k8sResources);
    technique.setMitigations(mitigations);
    technique.setPlatforms(List.of("Containers", "Kubernetes"));
    technique.setUrl("https://attack.mitre.org/techniques/" + id);
    
    return technique;
  }
  
  /**
   * Get technique by ID
   */
  public MitreTechnique getTechniqueById(String techniqueId) {
    return getKubernetesTechniques().stream()
        .filter(t -> t.getTechniqueId().equals(techniqueId))
        .findFirst()
        .orElse(null);
  }
  
  /**
   * Get techniques by tactic
   */
  public List<MitreTechnique> getTechniquesByTactic(String tactic) {
    return getKubernetesTechniques().stream()
        .filter(t -> t.getTactic().equalsIgnoreCase(tactic))
        .toList();
  }
  
  public boolean isEnabled() {
    return enabled;
  }
}
