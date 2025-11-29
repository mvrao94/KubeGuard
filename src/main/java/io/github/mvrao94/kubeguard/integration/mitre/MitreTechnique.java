package io.github.mvrao94.kubeguard.integration.mitre;

import java.util.List;

/**
 * Represents a MITRE ATT&CK technique
 */
public class MitreTechnique {
  
  private String techniqueId;
  private String name;
  private String description;
  private String tactic;
  private List<String> platforms;
  private List<String> dataSourcesDetection;
  private List<String> mitigations;
  private String url;
  
  // Kubernetes-specific fields
  private boolean appliesToKubernetes;
  private String kubernetesContext;
  private List<String> kubernetesResources;
  
  public MitreTechnique() {}
  
  public MitreTechnique(String techniqueId, String name, String tactic) {
    this.techniqueId = techniqueId;
    this.name = name;
    this.tactic = tactic;
  }

  // Getters and setters
  public String getTechniqueId() {
    return techniqueId;
  }

  public void setTechniqueId(String techniqueId) {
    this.techniqueId = techniqueId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getTactic() {
    return tactic;
  }

  public void setTactic(String tactic) {
    this.tactic = tactic;
  }

  public List<String> getPlatforms() {
    return platforms;
  }

  public void setPlatforms(List<String> platforms) {
    this.platforms = platforms;
  }

  public List<String> getDataSourcesDetection() {
    return dataSourcesDetection;
  }

  public void setDataSourcesDetection(List<String> dataSourcesDetection) {
    this.dataSourcesDetection = dataSourcesDetection;
  }

  public List<String> getMitigations() {
    return mitigations;
  }

  public void setMitigations(List<String> mitigations) {
    this.mitigations = mitigations;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isAppliesToKubernetes() {
    return appliesToKubernetes;
  }

  public void setAppliesToKubernetes(boolean appliesToKubernetes) {
    this.appliesToKubernetes = appliesToKubernetes;
  }

  public String getKubernetesContext() {
    return kubernetesContext;
  }

  public void setKubernetesContext(String kubernetesContext) {
    this.kubernetesContext = kubernetesContext;
  }

  public List<String> getKubernetesResources() {
    return kubernetesResources;
  }

  public void setKubernetesResources(List<String> kubernetesResources) {
    this.kubernetesResources = kubernetesResources;
  }
}
