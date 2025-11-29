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
      description = "File system path to the directory containing Kubernetes manifests",
      example = "/path/to/manifests",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String path;

  @Schema(description = "Optional description for the scan")
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
