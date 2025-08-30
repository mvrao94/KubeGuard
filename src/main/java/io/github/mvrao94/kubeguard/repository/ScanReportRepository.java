package io.github.mvrao94.kubeguard.repository;

import io.github.mvrao94.kubeguard.model.ScanReport;
import io.github.mvrao94.kubeguard.model.ScanStatus;
import io.github.mvrao94.kubeguard.model.ScanType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for ScanReport entities */
@Repository
public interface ScanReportRepository extends JpaRepository<ScanReport, Long> {

  /** Find scan report by scan ID */
  Optional<ScanReport> findByScanId(String scanId);

  /** Find all scan reports by status */
  List<ScanReport> findByStatus(ScanStatus status);

  /** Find scan reports by scan type */
  Page<ScanReport> findByScanType(ScanType scanType, Pageable pageable);

  /** Find scan reports by target (namespace or path) */
  Page<ScanReport> findByTarget(String target, Pageable pageable);

  /** Find scan reports created after a specific timestamp */
  List<ScanReport> findByTimestampAfter(LocalDateTime timestamp);

  /** Find scan reports by target and scan type */
  List<ScanReport> findByTargetAndScanTypeOrderByTimestampDesc(String target, ScanType scanType);

  /** Get latest scan report for a target */
  @Query(
      "SELECT sr FROM ScanReport sr WHERE sr.target = :target "
          + "AND sr.status = 'COMPLETED' ORDER BY sr.timestamp DESC")
  Optional<ScanReport> findLatestCompletedScanForTarget(@Param("target") String target);

  /** Count scan reports by status */
  long countByStatus(ScanStatus status);

  /** Get scan reports with high priority findings */
  @Query(
      "SELECT sr FROM ScanReport sr WHERE sr.criticalIssues > 0 OR sr.highIssues > 0 "
          + "ORDER BY sr.criticalIssues DESC, sr.highIssues DESC")
  List<ScanReport> findReportsWithHighPriorityFindings();

  /** Delete old scan reports */
  @Query("DELETE FROM ScanReport sr WHERE sr.timestamp < :cutoffDate")
  void deleteOldReports(@Param("cutoffDate") LocalDateTime cutoffDate);
}
