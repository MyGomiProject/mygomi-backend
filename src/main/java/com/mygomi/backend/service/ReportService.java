package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.InfoReportRequestDto;
import com.mygomi.backend.api.dto.request.SharePostReportRequestDto;
import com.mygomi.backend.api.dto.response.ReportResponseDto;
import com.mygomi.backend.domain.report.Report;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.ReportRepository;
import com.mygomi.backend.repository.SharePostRepository;
import com.mygomi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SharePostRepository sharePostRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    private static final String UPLOAD_DIR = "C:/mygomi-uploads/reports/";
    private static final List<ReportStatus> PENDING_REPORT_STATUSES = List.of(
            ReportStatus.PENDING,
            ReportStatus.IN_REVIEW
    );

    @Transactional
    public ReportResponseDto reportSharePost(Long postId, SharePostReportRequestDto dto, Long reporterId) {
        SharePost post = sharePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post does not exist."));

        User reporter = null;
        if (reporterId != null) {
            reporter = userRepository.findById(reporterId).orElse(null);
        }

        Report report = Report.builder()
                .type(ReportType.SHARE_POST)
                .reporter(reporter)
                .reporterEmail(dto.getReporterEmail())
                .emailReply(dto.isEmailReply())
                .targetPost(post)
                .reason(dto.getReason())
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        reportRepository.save(report);

        log.info("[Report submitted] share-post postId={}, reason={}, emailReply={}",
                postId, dto.getReason(), dto.isEmailReply());

        return ReportResponseDto.from(report);
    }

    @Transactional
    public ReportResponseDto reportInfo(InfoReportRequestDto dto, MultipartFile file, Long reporterId) {
        User reporter = null;
        if (reporterId != null) {
            reporter = userRepository.findById(reporterId).orElse(null);
        }

        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            attachmentUrl = saveAttachment(file);
        }

        Report report = Report.builder()
                .type(ReportType.INFO)
                .reporter(reporter)
                .reporterEmail(dto.getReporterEmail())
                .emailReply(dto.isEmailReply())
                .title(dto.getTitle())
                .content(dto.getContent())
                .attachmentUrl(attachmentUrl)
                .build();

        reportRepository.save(report);

        log.info("[Report submitted] info title={}, hasFile={}, emailReply={}",
                dto.getTitle(), attachmentUrl != null, dto.isEmailReply());

        return ReportResponseDto.from(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponseDto> getReports(ReportType type, ReportStatus status, Pageable pageable) {
        Page<Report> reports;

        if (type != null && status != null) {
            reports = reportRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            reports = reportRepository.findByType(type, pageable);
        } else if (status != null) {
            reports = reportRepository.findByStatus(status, pageable);
        } else {
            reports = reportRepository.findAll(pageable);
        }

        return reports.map(ReportResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report does not exist."));
        return ReportResponseDto.from(report);
    }

    @Transactional
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatus status, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report does not exist."));

        report.updateStatus(status, adminNote);
        // 신고를 'RESOLVED'로 변경하고, 그것이 '나눔 게시글' 신고일 경우
        if (status == ReportStatus.RESOLVED && report.getType() == ReportType.SHARE_POST) {
            SharePost targetPost = report.getTargetPost();
            if (targetPost != null) {
                targetPost.softDelete(); // 원본 게시글을  DELETED 상태로 변경
            }

        }

        log.info("[Report handled] reportId={}, status={}", reportId, status);

        return ReportResponseDto.from(report);
    }

    @Transactional
    public int dismissSharePostReports(Long postId, String adminNote) {
        sharePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post does not exist."));

        int updatedCount = reportRepository.bulkUpdateSharePostReportStatus(
                postId,
                ReportType.SHARE_POST,
                PENDING_REPORT_STATUSES,
                ReportStatus.DISMISSED,
                adminNote
        );

        log.info("[Reports dismissed] postId={}, dismissedCount={}", postId, updatedCount);
        return updatedCount;
    }

    @Transactional
    public int deleteSharePostAndResolveReports(Long postId, String adminNote) {
        SharePost post = sharePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post does not exist."));

        post.softDelete();

        int updatedCount = reportRepository.bulkUpdateSharePostReportStatus(
                postId,
                ReportType.SHARE_POST,
                PENDING_REPORT_STATUSES,
                ReportStatus.RESOLVED,
                adminNote
        );

        log.info("[Post deleted and reports resolved] postId={}, resolvedCount={}", postId, updatedCount);
        return updatedCount;
    }

    private String saveAttachment(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 10MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF or image files are allowed.");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String ext = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String savedFilename = UUID.randomUUID() + ext;

            Path filePath = uploadPath.resolve(savedFilename);
            file.transferTo(filePath.toFile());

            return "/uploads/reports/" + savedFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachment.", e);
        }
    }
}