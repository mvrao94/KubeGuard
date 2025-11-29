package io.github.mvrao94.kubeguard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Severity level of a security finding")
public enum Severity {
  @Schema(description = "Critical severity - immediate action required")
  CRITICAL,

  @Schema(description = "High severity - should be addressed urgently")
  HIGH,

  @Schema(description = "Medium severity - should be addressed in near term")
  MEDIUM,

  @Schema(description = "Low severity - should be addressed when convenient")
  LOW,

  @Schema(description = "Informational - no immediate action required")
  INFO
}
