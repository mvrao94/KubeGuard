package io.github.mvrao94.kubeguard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a security scan")
public enum ScanStatus {
  @Schema(description = "Scan is currently in progress")
  RUNNING,

  @Schema(description = "Scan completed successfully")
  COMPLETED,

  @Schema(description = "Scan failed due to an error")
  FAILED
}
