package io.github.mvrao94.kubeguard.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Standard error response returned when an API request fails")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  @Schema(description = "HTTP status code", example = "400", requiredMode = Schema.RequiredMode.REQUIRED)
  private int status;

  @Schema(description = "Error type or category", example = "BAD_REQUEST", requiredMode = Schema.RequiredMode.REQUIRED)
  private String error;

  @Schema(description = "Human-readable error message", example = "Invalid request parameters", requiredMode = Schema.RequiredMode.REQUIRED)
  private String message;

  @Schema(description = "API path where the error occurred", example = "/api/v1/scan/manifests")
  private String path;

  @Schema(description = "Timestamp when the error occurred", example = "2025-11-30T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamp;

  @Schema(description = "List of detailed validation errors")
  private List<String> errors;

  @Schema(description = "Unique trace ID for debugging purposes", example = "a1b2c3d4-e5f6-7890")
  private String traceId;

  public ErrorResponse() {
    this.timestamp = LocalDateTime.now();
  }

  public ErrorResponse(int status, String error, String message) {
    this.status = status;
    this.error = error;
    this.message = message;
    this.timestamp = LocalDateTime.now();
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }
}
