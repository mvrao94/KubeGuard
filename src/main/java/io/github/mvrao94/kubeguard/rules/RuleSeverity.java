package io.github.mvrao94.kubeguard.rules;

public enum RuleSeverity {
  CRITICAL(4),
  HIGH(3),
  MEDIUM(2),
  LOW(1),
  INFO(0);
  
  private final int level;
  
  RuleSeverity(int level) {
    this.level = level;
  }
  
  public int getLevel() {
    return level;
  }
}
