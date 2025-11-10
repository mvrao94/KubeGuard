package io.github.mvrao94.kubeguard.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO for scan operations */
@Schema(description = "Response object for scan operations")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScanResponse {

  @Schema(
      description = "Unique identifier for the scan",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private String scanId;

  @Schema(description = "Response message", example = "Scan started successfully")
  private String message;

  @Schema(
      description = "Current status of the scan",
      example = "RUNNING",
      allowableValues = {"RUNNING", "COMPLETED", "FAILED"})
  private String status;

  @Schema(description = "Error message if scan failed")
  private String error;

  @Schema(description = "Additional metadata")
  private Object metadata;

  // Constructors
  public ScanResponse() {}

  public ScanResponse(String message, String status) {
    this.message = message;
    this.status = status;
  }

  public ScanResponse(String scanId, String message, String status) {
    this.scanId = scanId;
    this.message = message;
    this.status = status;
  }

  // Getters and setters
  public String getScanId() {
    return scanId;
  }

  public void setScanId(String scanId) {
    this.scanId = scanId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Object getMetadata() {
    return metadata;
  }

  public void setMetadata(Object metadata) {
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    return "ScanResponse{"
        + "scanId='"
        + scanId
        + '\''
        + ", message='"
        + message
        + '\''
        + ", status='"
        + status
        + '\''
        + ", error='"
        + error
        + '\''
        + ", metadata="
        + metadata
        + '}';
  }
}
