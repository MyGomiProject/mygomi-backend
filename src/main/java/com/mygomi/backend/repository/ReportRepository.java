package com.mygomi.backend.repository;

import com.mygomi.backend.domain.report.Report;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 관리자: 전체 신고 목록 (타입/상태 필터)
    Page<Report> findByTypeAndStatus(ReportType type, ReportStatus status, Pageable pageable);
    Page<Report> findByType(ReportType type, Pageable pageable);
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    // 특정 게시글의 신고 수
    long countByTargetPostId(Long postId);
}
