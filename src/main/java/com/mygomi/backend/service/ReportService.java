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

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    private static final String UPLOAD_DIR = "C:/mygomi-uploads/reports/";

    // ========================================
    // 게시글 신고 접수
    // ========================================
    @Transactional
    public ReportResponseDto reportSharePost(Long postId, SharePostReportRequestDto dto, Long reporterId) {

        SharePost post = sharePostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

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

        log.info("📢 [신고 접수] 게시글 신고 - postId={}, reason={}, emailReply={}",
                postId, dto.getReason(), dto.isEmailReply());

        return ReportResponseDto.from(report);
    }

    // ========================================
    // 잘못된 정보 신고 접수 (첨부파일 포함)
    // ========================================
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

        log.info("📢 [신고 접수] 정보 신고 - title={}, hasFile={}, emailReply={}",
                dto.getTitle(), attachmentUrl != null, dto.isEmailReply());

        return ReportResponseDto.from(report);
    }

    // ========================================
    // 관리자: 신고 목록 조회
    // ========================================
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

    // ========================================
    // 관리자: 신고 상세 조회
    // ========================================
    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));
        return ReportResponseDto.from(report);
    }

    // ========================================
    // 관리자: 신고 상태 변경
    // ========================================
    @Transactional
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatus status, String adminNote) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));

        report.updateStatus(status, adminNote);

        log.info("✅ [신고 처리] reportId={}, status={}", reportId, status);

        return ReportResponseDto.from(report);
    }

    // ========================================
    // 첨부파일 저장 (PDF / 이미지, 10MB 제한)
    // ========================================
    private String saveAttachment(MultipartFile file) {
        // 파일 크기 검사
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // 파일 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("PDF 또는 이미지 파일만 첨부 가능합니다.");
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
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
