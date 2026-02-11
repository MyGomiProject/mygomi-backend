package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.InfoReportRequestDto;
import com.mygomi.backend.api.dto.request.SharePostReportRequestDto;
import com.mygomi.backend.api.dto.response.ReportResponseDto;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Report", description = "신고 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    // ========================================
    // 게시글 신고 접수
    // POST /api/reports/share-posts/{postId}
    // ========================================
    @Operation(summary = "게시글 신고", description = "나눔 게시글을 신고합니다.")
    @PostMapping("/share-posts/{postId}")
    public ResponseEntity<ReportResponseDto> reportSharePost(
            @PathVariable Long postId,
            @Valid @RequestBody SharePostReportRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = getUserId(userDetails);
        ReportResponseDto response = reportService.reportSharePost(postId, dto, reporterId);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // 잘못된 정보 신고 접수 (첨부파일 포함)
    // POST /api/reports/info  (multipart)
    // ========================================
    @Operation(summary = "잘못된 정보 신고", description = "웹사이트의 잘못된 정보를 신고합니다. PDF/이미지 첨부 가능 (최대 10MB)")
    @PostMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDto> reportInfo(
            @Valid @RequestPart("data") InfoReportRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = getUserId(userDetails);
        ReportResponseDto response = reportService.reportInfo(dto, file, reporterId);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // 관리자: 신고 목록 조회
    // GET /api/reports/admin?type=SHARE_POST&status=PENDING
    // ========================================
    @Operation(summary = "[관리자] 신고 목록 조회", description = "타입/상태 필터 가능. 최신순 정렬.")
    @GetMapping("/admin")
    public ResponseEntity<Page<ReportResponseDto>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReportResponseDto> response = reportService.getReports(type, status, pageable);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // 관리자: 신고 상세 조회
    // GET /api/reports/admin/{reportId}
    // ========================================
    @Operation(summary = "[관리자] 신고 상세 조회")
    @GetMapping("/admin/{reportId}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    // ========================================
    // 관리자: 신고 처리 상태 변경
    // PATCH /api/reports/admin/{reportId}/status
    // ========================================
    @Operation(summary = "[관리자] 신고 처리 상태 변경", description = "PENDING → IN_REVIEW → RESOLVED / DISMISSED")
    @PatchMapping("/admin/{reportId}/status")
    public ResponseEntity<ReportResponseDto> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String adminNote) {

        return ResponseEntity.ok(reportService.updateReportStatus(reportId, status, adminNote));
    }

    // ========================================
    // 신고 사유 목록 조회 (프론트 드롭다운용)
    // GET /api/reports/reasons
    // ========================================
    @Operation(summary = "신고 사유 목록", description = "게시글 신고 시 선택 가능한 사유 목록을 반환합니다.")
    @GetMapping("/reasons")
    public ResponseEntity<?> getReasons() {
        var reasons = java.util.Arrays.stream(
                com.mygomi.backend.domain.report.ReportReason.values())
                .map(r -> Map.of("code", r.name(), "label", r.getDescription()))
                .toList();
        return ResponseEntity.ok(reasons);
    }

    // ========================================
    // 편의 메서드
    // ========================================
    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return user.getId();
    }
}
