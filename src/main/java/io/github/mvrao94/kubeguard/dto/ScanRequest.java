package io.github.mvrao94.kubeguard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for initiating a security scan */
@Schema(description = "Request object for initiating a manifest scan")
public class ScanRequest {

  @NotBlank(message = "Path is required")
  @Size(max = 500, message = "Path must be less than 500 characters")
  @Schema(
      description =
          "File system path to the directory containing Kubernetes YAML/YML manifest files. "
              + "The path should be accessible by the KubeGuard service. "
              + "All .yaml and .yml files in the directory will be scanned recursively.",
      example = "/home/user/k8s-manifests",
      requiredMode = Schema.RequiredMode.REQUIRED,
      minLength = 1,
      maxLength = 500)
  private String path;

  @Schema(
      description = "Optional human-readable description for the scan to help identify it later",
      example = "Production deployment manifests - Q4 2025",
      maxLength = 255)
  private String description;

  // Constructors
  public ScanRequest() {}

  public ScanRequest(String path) {
    this.path = path;
  }

  public ScanRequest(String path, String description) {
    this.path = path;
    this.description = description;
  }

  // Getters and setters
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "ScanRequest{" + "path='" + path + '\'' + ", description='" + description + '\'' + '}';
  }
}
