package io.github.mvrao94.kubeguard.observability.info;

import io.github.mvrao94.kubeguard.repository.ScanReportRepository;
import io.github.mvrao94.kubeguard.repository.SecurityFindingRepository;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** Custom info contributor for application statistics */
@Component
public class CustomInfoContributor implements InfoContributor {

  private final ScanReportRepository scanReportRepository;
  private final SecurityFindingRepository securityFindingRepository;

  public CustomInfoContributor(
      ScanReportRepository scanReportRepository,
      SecurityFindingRepository securityFindingRepository) {
    this.scanReportRepository = scanReportRepository;
    this.securityFindingRepository = securityFindingRepository;
  }

  @Override
  public void contribute(Info.Builder builder) {
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalScans", scanReportRepository.count());
    stats.put("totalFindings", securityFindingRepository.count());
    stats.put("lastUpdated", LocalDateTime.now());

    builder.withDetail("kubeguard", stats);
  }
}
