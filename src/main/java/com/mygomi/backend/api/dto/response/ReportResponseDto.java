package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.report.Report;
import com.mygomi.backend.domain.report.ReportReason;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponseDto {

    private Long id;
    private ReportType type;
    private String typeName;

    // 신고자
    private Long reporterId;
    private String reporterEmail;
    private boolean emailReply;

    // 게시글 신고 전용
    private Long targetPostId;
    private ReportReason reason;
    private String reasonName;

    // 공통
    private String title;
    private String content;
    private String attachmentUrl;

    // 처리 상태
    private ReportStatus status;
    private String statusName;
    private String adminNote;

    private LocalDateTime createdAt;

    public static ReportResponseDto from(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .type(report.getType())
                .typeName(report.getType().getDescription())
                .reporterId(report.getReporter() != null ? report.getReporter().getId() : null)
                .reporterEmail(report.getReporterEmail())
                .emailReply(report.isEmailReply())
                .targetPostId(report.getTargetPost() != null ? report.getTargetPost().getId() : null)
                .reason(report.getReason())
                .reasonName(report.getReason() != null ? report.getReason().getDescription() : null)
                .title(report.getTitle())
                .content(report.getContent())
                .attachmentUrl(report.getAttachmentUrl())
                .status(report.getStatus())
                .statusName(report.getStatus().getDescription())
                .adminNote(report.getAdminNote())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
