package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.InfoReportRequestDto;
import com.mygomi.backend.api.dto.request.SharePostReportRequestDto;
import com.mygomi.backend.api.dto.response.ReportResponseDto;
import com.mygomi.backend.domain.report.ReportReason;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "Report", description = "Report API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @Operation(summary = "Report share post", description = "Submit a report for a share post.")
    @PostMapping("/share-posts/{postId}")
    public ResponseEntity<ReportResponseDto> reportSharePost(
            @PathVariable Long postId,
            @Valid @RequestBody SharePostReportRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = getUserId(userDetails);
        return ResponseEntity.ok(reportService.reportSharePost(postId, dto, reporterId));
    }

    @Operation(summary = "Report wrong info", description = "Submit a wrong information report. PDF/image attachment supported up to 10MB.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    @PostMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDto> reportInfo(
            @Valid @RequestPart("data") InfoReportRequestDto dto,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long reporterId = getUserId(userDetails);
        return ResponseEntity.ok(reportService.reportInfo(dto, file, reporterId));
    }

    @Operation(summary = "[Admin] Get reports", description = "Get reports with optional type/status filters.")
    @GetMapping("/admin")
    public ResponseEntity<Page<ReportResponseDto>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(reportService.getReports(type, status, pageable));
    }

    @Operation(summary = "[Admin] Get report detail")
    @GetMapping("/admin/{reportId}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable Long reportId) {
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @Operation(summary = "[Admin] Update report status", description = "PENDING -> IN_REVIEW -> RESOLVED / DISMISSED")
    @PatchMapping("/admin/{reportId}/status")
    public ResponseEntity<ReportResponseDto> updateStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String adminNote) {

        log.info("[Admin report status API] reportId={}, status={}, hasAdminNote={}",
                reportId, status, adminNote != null && !adminNote.isBlank());
        return ResponseEntity.ok(reportService.updateReportStatus(reportId, status, adminNote));
    }

    @Operation(summary = "[Admin] Dismiss share-post reports", description = "Bulk update PENDING/IN_REVIEW reports to DISMISSED for a post.")
    @PatchMapping("/admin/share-posts/{postId}/dismiss")
    public ResponseEntity<Map<String, Object>> dismissSharePostReports(
            @PathVariable Long postId,
            @RequestParam(required = false) String adminNote) {

        log.info("[Admin dismiss API] postId={}, hasAdminNote={}",
                postId, adminNote != null && !adminNote.isBlank());
        int updatedCount = reportService.dismissSharePostReports(postId, adminNote);
        return ResponseEntity.ok(Map.of("postId", postId, "updatedCount", updatedCount));
    }

    @Operation(summary = "[Admin] Delete reported post", description = "Soft-delete the post and bulk update PENDING/IN_REVIEW reports to RESOLVED.")
    @PatchMapping("/admin/share-posts/{postId}/delete")
    public ResponseEntity<Map<String, Object>> deleteSharePostAndResolveReports(
            @PathVariable Long postId,
            @RequestParam(required = false) String adminNote) {

        log.info("[Admin delete API] postId={}, hasAdminNote={}",
                postId, adminNote != null && !adminNote.isBlank());
        int updatedCount = reportService.deleteSharePostAndResolveReports(postId, adminNote);
        return ResponseEntity.ok(Map.of("postId", postId, "updatedCount", updatedCount));
    }

    @Operation(summary = "Get report reasons", description = "Return selectable reasons for share-post report.")
    @GetMapping("/reasons")
    public ResponseEntity<List<Map<String, String>>> getReasons() {
        List<Map<String, String>> reasons = Arrays.stream(ReportReason.values())
                .map(r -> Map.of("code", r.name(), "label", r.getDescription()))
                .toList();
        return ResponseEntity.ok(reasons);
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
        return user.getId();
    }
}
