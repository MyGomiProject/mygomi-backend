package com.mygomi.backend.repository;

import com.mygomi.backend.domain.report.Report;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByTypeAndStatus(ReportType type, ReportStatus status, Pageable pageable);
    Page<Report> findByType(ReportType type, Pageable pageable);
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    long countByTargetPostId(Long postId);

    long countByTargetPostIdAndTypeAndStatusIn(Long postId, ReportType type, List<ReportStatus> statuses);

    @Query("""
            SELECT r.targetPost.id, COUNT(r)
            FROM Report r
            WHERE r.type = :type
              AND r.status IN :statuses
              AND r.targetPost.id IN :postIds
            GROUP BY r.targetPost.id
            """)
    List<Object[]> countByTargetPostIdsAndTypeAndStatusIn(@Param("postIds") List<Long> postIds,
                                                           @Param("type") ReportType type,
                                                           @Param("statuses") List<ReportStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Report r
            SET r.status = :newStatus,
                r.adminNote = :adminNote
            WHERE r.type = :type
              AND r.targetPost.id = :postId
              AND r.status IN :statuses
            """)
    int bulkUpdateSharePostReportStatus(@Param("postId") Long postId,
                                        @Param("type") ReportType type,
                                        @Param("statuses") List<ReportStatus> statuses,
                                        @Param("newStatus") ReportStatus newStatus,
                                        @Param("adminNote") String adminNote);
}