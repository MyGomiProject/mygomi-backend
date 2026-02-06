package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.CommonResponse;
import com.mygomi.backend.api.dto.request.SharePostRequestDto;
import com.mygomi.backend.api.dto.response.SharePostResponseDto;
import com.mygomi.backend.domain.share.ShareStatus;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.domain.user.UserRepository;
import com.mygomi.backend.service.SharePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "Share Posts", description = "나눔 게시글 API")
@RestController
@RequestMapping("/api/share-posts")
@RequiredArgsConstructor
public class SharePostController {

    private final SharePostService sharePostService;
    private final UserRepository userRepository;

    @Operation(summary = "게시글 등록", description = "이미지와 함께 게시글을 등록합니다 (최대 5장)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<SharePostResponseDto>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute SharePostRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        
        Long userId = getUserIdFromToken(userDetails);
        
        SharePostResponseDto response = sharePostService.createPost(userId, request, images);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success(response));
    }

    @Operation(summary = "게시글 조회 (단건)", description = "게시글 ID로 상세 정보를 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<SharePostResponseDto>> getPost(@PathVariable Long id) {
        SharePostResponseDto response = sharePostService.getPost(id);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "게시글 목록 조회", description = "지역별/상태별로 게시글 목록을 조회합니다")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<SharePostResponseDto>>> getPosts(
            @RequestParam(required = false) String ward,
            @RequestParam(defaultValue = "OPEN") ShareStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<SharePostResponseDto> response = sharePostService.getPosts(ward, status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "내 게시글 조회", description = "로그인한 사용자의 게시글 목록을 조회합니다")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<SharePostResponseDto>>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        List<SharePostResponseDto> response = sharePostService.getMyPosts(userId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다 (작성자만 가능)")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<SharePostResponseDto>> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SharePostRequestDto request) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        SharePostResponseDto response = sharePostService.updatePost(userId, id, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다 (작성자만 가능, Soft Delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        sharePostService.deletePost(userId, id);
        return ResponseEntity.ok(CommonResponse.success("게시글이 삭제되었습니다"));
    }

    // 카테고리 목록 조회
    @Operation(summary = "카테고리 목록 조회", description = "게시글 작성 시 선택할 수 있는 카테고리 목록을 반환합니다.")
    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getCategories() {
        // Enum -> List<Map> 변환 로직
        List<Map<String, String>> categories = java.util.Arrays.stream(com.mygomi.backend.domain.share.ShareCategory.values())
                .map(category -> java.util.Map.of(
                        "code", category.name(),           // 예: FURNITURE
                        "label", category.getDescription() // 예: 가구/인테리어
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(CommonResponse.success(categories));
    }

    @Operation(summary = "게시글 상태 변경", description = "게시글 상태를 변경합니다 (OPEN/RESERVED/COMPLETED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<SharePostResponseDto>> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam ShareStatus status) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        SharePostResponseDto response = sharePostService.updateStatus(userId, id, status);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
        summary = "근처 게시글 조회 (지도용)", 
        description = "반경 내의 게시글을 조회합니다. sortBy: distance(거리순), latest(최신순)"
    )
    @GetMapping("/nearby")
    public ResponseEntity<CommonResponse<Page<SharePostResponseDto>>> getNearbyPosts(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "distance") String sortBy,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<SharePostResponseDto> response = sharePostService.getNearbyPosts(lat, lng, radiusKm, sortBy, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    // 🕵️‍♂️ 편의 메서드: 토큰 정보(UserDetails)로 실제 유저 ID 찾기
    private Long getUserIdFromToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 사용자입니다. email=" + email));

        return user.getId();
    }
}
