package io.github.mvrao94.kubeguard.integration.nvd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Client for NIST National Vulnerability Database (NVD) API
 * API Documentation: https://nvd.nist.gov/developers/vulnerabilities
 */
@Service
public class NvdClient {
  
  private static final Logger logger = LoggerFactory.getLogger(NvdClient.class);
  private static final String NVD_API_BASE_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";
  
  @Value("${kubeguard.nvd.api-key:}")
  private String apiKey;
  
  @Value("${kubeguard.nvd.enabled:false}")
  private boolean enabled;
  
  private final RestClient restClient;
  private final ObjectMapper objectMapper;
  
  public NvdClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.restClient = RestClient.builder()
        .baseUrl(NVD_API_BASE_URL)
        .build();
  }
  
  /**
   * Search for Kubernetes-related vulnerabilities
   */
  public List<NvdVulnerability> searchKubernetesVulnerabilities(int resultsPerPage) {
    if (!enabled) {
      logger.info("NVD integration is disabled");
      return List.of();
    }
    
    try {
      logger.info("Fetching Kubernetes vulnerabilities from NVD...");
      
      String response = restClient.get()
          .uri(uriBuilder -> uriBuilder
              .queryParam("keywordSearch", "kubernetes")
              .queryParam("resultsPerPage", resultsPerPage)
              .build())
          .header("apiKey", apiKey)
          .retrieve()
          .body(String.class);
      
      return parseNvdResponse(response);
      
    } catch (Exception e) {
      logger.error("Error fetching vulnerabilities from NVD: {}", e.getMessage(), e);
      return List.of();
    }
  }
  
  /**
   * Get vulnerabilities by CVE ID
   */
  public NvdVulnerability getVulnerabilityByCveId(String cveId) {
    if (!enabled) {
      return null;
    }
    
    try {
      String response = restClient.get()
          .uri(uriBuilder -> uriBuilder
              .queryParam("cveId", cveId)
              .build())
          .header("apiKey", apiKey)
          .retrieve()
          .body(String.class);
      
      List<NvdVulnerability> vulns = parseNvdResponse(response);
      return vulns.isEmpty() ? null : vulns.get(0);
      
    } catch (Exception e) {
      logger.error("Error fetching CVE {}: {}", cveId, e.getMessage());
      return null;
    }
  }
  
  /**
   * Get recent vulnerabilities (last 30 days)
   */
  public List<NvdVulnerability> getRecentVulnerabilities(int days, int resultsPerPage) {
    if (!enabled) {
      return List.of();
    }
    
    try {
      LocalDateTime endDate = LocalDateTime.now();
      LocalDateTime startDate = endDate.minusDays(days);
      
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
      
      String response = restClient.get()
          .uri(uriBuilder -> uriBuilder
              .queryParam("pubStartDate", startDate.format(formatter))
              .queryParam("pubEndDate", endDate.format(formatter))
              .queryParam("keywordSearch", "kubernetes OR container OR docker")
              .queryParam("resultsPerPage", resultsPerPage)
              .build())
          .header("apiKey", apiKey)
          .retrieve()
          .body(String.class);
      
      return parseNvdResponse(response);
      
    } catch (Exception e) {
      logger.error("Error fetching recent vulnerabilities: {}", e.getMessage());
      return List.of();
    }
  }
  
  /**
   * Parse NVD API response
   */
  private List<NvdVulnerability> parseNvdResponse(String jsonResponse) {
    List<NvdVulnerability> vulnerabilities = new ArrayList<>();
    
    try {
      JsonNode root = objectMapper.readTree(jsonResponse);
      JsonNode vulnsNode = root.path("vulnerabilities");
      
      for (JsonNode vulnNode : vulnsNode) {
        JsonNode cveNode = vulnNode.path("cve");
        
        NvdVulnerability vuln = new NvdVulnerability();
        
        // Basic info
        vuln.setCveId(cveNode.path("id").asText());
        
        // Description
        JsonNode descriptionsNode = cveNode.path("descriptions");
        if (descriptionsNode.isArray() && descriptionsNode.size() > 0) {
          vuln.setDescription(descriptionsNode.get(0).path("value").asText());
        }
        
        // Dates
        String published = cveNode.path("published").asText();
        String modified = cveNode.path("lastModified").asText();
        if (!published.isEmpty()) {
          vuln.setPublishedDate(LocalDateTime.parse(published, DateTimeFormatter.ISO_DATE_TIME));
        }
        if (!modified.isEmpty()) {
          vuln.setLastModifiedDate(LocalDateTime.parse(modified, DateTimeFormatter.ISO_DATE_TIME));
        }
        
        // CVSS metrics
        JsonNode metricsNode = cveNode.path("metrics");
        JsonNode cvssV3Node = metricsNode.path("cvssMetricV31");
        if (cvssV3Node.isArray() && cvssV3Node.size() > 0) {
          JsonNode cvssData = cvssV3Node.get(0).path("cvssData");
          vuln.setBaseScore(cvssData.path("baseScore").asDouble());
          vuln.setBaseSeverity(cvssData.path("baseSeverity").asText());
          vuln.setAttackVector(cvssData.path("attackVector").asText());
          vuln.setAttackComplexity(cvssData.path("attackComplexity").asText());
          vuln.setPrivilegesRequired(cvssData.path("privilegesRequired").asText());
          vuln.setUserInteraction(cvssData.path("userInteraction").asText());
          vuln.setScope(cvssData.path("scope").asText());
          vuln.setConfidentialityImpact(cvssData.path("confidentialityImpact").asText());
          vuln.setIntegrityImpact(cvssData.path("integrityImpact").asText());
          vuln.setAvailabilityImpact(cvssData.path("availabilityImpact").asText());
        }
        
        // CWE IDs
        JsonNode weaknessesNode = cveNode.path("weaknesses");
        List<String> cweIds = new ArrayList<>();
        for (JsonNode weaknessNode : weaknessesNode) {
          JsonNode descArray = weaknessNode.path("description");
          for (JsonNode desc : descArray) {
            String cweId = desc.path("value").asText();
            if (cweId.startsWith("CWE-")) {
              cweIds.add(cweId);
            }
          }
        }
        vuln.setCweIds(cweIds);
        
        // References
        JsonNode referencesNode = cveNode.path("references");
        List<String> references = new ArrayList<>();
        for (JsonNode refNode : referencesNode) {
          references.add(refNode.path("url").asText());
        }
        vuln.setReferences(references);
        
        vulnerabilities.add(vuln);
      }
      
      logger.info("Parsed {} vulnerabilities from NVD", vulnerabilities.size());
      
    } catch (Exception e) {
      logger.error("Error parsing NVD response: {}", e.getMessage(), e);
    }
    
    return vulnerabilities;
  }
  
  public boolean isEnabled() {
    return enabled;
  }
}
