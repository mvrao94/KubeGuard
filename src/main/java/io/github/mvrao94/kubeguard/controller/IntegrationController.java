package io.github.mvrao94.kubeguard.controller;

import io.github.mvrao94.kubeguard.integration.mitre.MitreIntegrationService;
import io.github.mvrao94.kubeguard.integration.nvd.NvdIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for external security framework integrations
 */
@RestController
@RequestMapping("/api/v1/integrations")
@Tag(
    name = "Security Framework Integrations",
    description = "APIs for integrating external security frameworks (NIST NVD, MITRE ATT&CK)")
public class IntegrationController {
  
  @Autowired
  private NvdIntegrationService nvdService;
  
  @Autowired
  private MitreIntegrationService mitreService;
  
  @Operation(
      summary = "Sync NIST NVD vulnerabilities",
      description = "Fetches Kubernetes-related vulnerabilities from NIST National Vulnerability Database "
          + "and converts them to security rules. Requires NVD API key configuration.",
      tags = {"Security Framework Integrations"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Vulnerabilities synced successfully"),
        @ApiResponse(
            responseCode = "503",
            description = "NVD integration is disabled or unavailable")
      })
  @PostMapping("/nvd/sync")
  public ResponseEntity<Map<String, Object>> syncNvd() {
    int ruleCount = nvdService.syncVulnerabilities();
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "NVD sync completed");
    response.put("rulesAdded", ruleCount);
    response.put("stats", nvdService.getStats());
    
    return ResponseEntity.ok(response);
  }
  
  @Operation(
      summary = "Sync recent NVD vulnerabilities",
      description = "Fetches vulnerabilities published in the last N days",
      tags = {"Security Framework Integrations"})
  @PostMapping("/nvd/sync/recent")
  public ResponseEntity<Map<String, Object>> syncRecentNvd(
      @Parameter(description = "Number of days to look back", example = "7")
      @RequestParam(defaultValue = "7") int days) {
    
    int ruleCount = nvdService.syncRecentVulnerabilities(days);
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Recent NVD vulnerabilities synced");
    response.put("days", days);
    response.put("rulesAdded", ruleCount);
    
    return ResponseEntity.ok(response);
  }
  
  @Operation(
      summary = "Get NVD integration statistics",
      description = "Returns statistics about NVD integration status and synced rules",
      tags = {"Security Framework Integrations"})
  @GetMapping("/nvd/stats")
  public ResponseEntity<NvdIntegrationService.NvdSyncStats> getNvdStats() {
    return ResponseEntity.ok(nvdService.getStats());
  }
  
  @Operation(
      summary = "Load MITRE ATT&CK techniques",
      description = "Loads MITRE ATT&CK for Containers techniques and converts them to security rules. "
          + "Provides attack pattern context for Kubernetes security.",
      tags = {"Security Framework Integrations"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "MITRE ATT&CK techniques loaded successfully")
      })
  @PostMapping("/mitre/load")
  public ResponseEntity<Map<String, Object>> loadMitre() {
    int ruleCount = mitreService.loadTechniques();
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "MITRE ATT&CK techniques loaded");
    response.put("rulesAdded", ruleCount);
    response.put("stats", mitreService.getStats());
    
    return ResponseEntity.ok(response);
  }
  
  @Operation(
      summary = "Load MITRE techniques by tactic",
      description = "Loads MITRE ATT&CK techniques for a specific tactic (e.g., Initial Access, Execution)",
      tags = {"Security Framework Integrations"})
  @PostMapping("/mitre/load/{tactic}")
  public ResponseEntity<Map<String, Object>> loadMitreByTactic(
      @Parameter(
          description = "MITRE ATT&CK tactic",
          example = "Privilege Escalation")
      @PathVariable String tactic) {
    
    int ruleCount = mitreService.loadTechniquesByTactic(tactic);
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "MITRE ATT&CK techniques loaded for tactic");
    response.put("tactic", tactic);
    response.put("rulesAdded", ruleCount);
    
    return ResponseEntity.ok(response);
  }
  
  @Operation(
      summary = "Get MITRE integration statistics",
      description = "Returns statistics about MITRE ATT&CK integration and loaded techniques",
      tags = {"Security Framework Integrations"})
  @GetMapping("/mitre/stats")
  public ResponseEntity<MitreIntegrationService.MitreStats> getMitreStats() {
    return ResponseEntity.ok(mitreService.getStats());
  }
  
  @Operation(
      summary = "Get available MITRE tactics",
      description = "Returns list of available MITRE ATT&CK tactics",
      tags = {"Security Framework Integrations"})
  @GetMapping("/mitre/tactics")
  public ResponseEntity<Map<String, Object>> getMitreTactics() {
    Map<String, Object> response = new HashMap<>();
    response.put("tactics", mitreService.getAvailableTactics());
    return ResponseEntity.ok(response);
  }
  
  @Operation(
      summary = "Get all integration statistics",
      description = "Returns combined statistics for all security framework integrations",
      tags = {"Security Framework Integrations"})
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getAllStats() {
    Map<String, Object> stats = new HashMap<>();
    stats.put("nvd", nvdService.getStats());
    stats.put("mitre", mitreService.getStats());
    
    return ResponseEntity.ok(stats);
  }
}
