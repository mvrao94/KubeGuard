package io.github.mvrao94.kubeguard.repository;

import io.github.mvrao94.kubeguard.model.SecurityFinding;
import java.util.List;
import javax.print.attribute.standard.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for SecurityFinding entities */
@Repository
public interface SecurityFindingRepository extends JpaRepository<SecurityFinding, Long> {

  /** Find findings by scan report ID */
  Page<SecurityFinding> findByScanReportId(Long scanReportId, Pageable pageable);

  /** Find findings by severity */
  List<SecurityFinding> findBySeverity(Severity severity);

  /** Find findings by rule ID */
  List<SecurityFinding> findByRuleId(String ruleId);

  /** Find findings by resource name and type */
  List<SecurityFinding> findByResourceNameAndResourceType(String resourceName, String resourceType);

  /** Find findings by category */
  Page<SecurityFinding> findByCategory(String category, Pageable pageable);

  /** Count findings by severity for a scan report */
  @Query(
      "SELECT COUNT(sf) FROM SecurityFinding sf WHERE sf.scanReport.id = :scanReportId "
          + "AND sf.severity = :severity")
  long countByScanReportIdAndSeverity(
      @Param("scanReportId") Long scanReportId, @Param("severity") Severity severity);

  /** Get top failing rules across all scans */
  @Query(
      "SELECT sf.ruleId, COUNT(sf) as count FROM SecurityFinding sf "
          + "GROUP BY sf.ruleId ORDER BY count DESC")
  List<Object[]> getTopFailingRules();

  /** Get findings summary by category */
  @Query(
      "SELECT sf.category, COUNT(sf) as count FROM SecurityFinding sf "
          + "WHERE sf.scanReport.id = :scanReportId GROUP BY sf.category")
  List<Object[]> getFindingsSummaryByCategory(@Param("scanReportId") Long scanReportId);
}
