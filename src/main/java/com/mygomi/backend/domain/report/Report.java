package com.mygomi.backend.domain.report;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(name = "reporter_email")
    private String reporterEmail;

    @Column(name = "email_reply", nullable = false)
    private boolean emailReply;

    // 신고 대상 게시글 (SHARE_POST 타입일 때만)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_post_id")
    private SharePost targetPost;

    // 신고 사유 (SHARE_POST 타입일 때만)
    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private ReportReason reason;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 첨부파일 (INFO 타입일 때만)
    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    // 관리자가 상태 변경 + 메모 작성
    public void updateStatus(ReportStatus status, String adminNote) {
        this.status = status;
        this.adminNote = adminNote;
    }
}
