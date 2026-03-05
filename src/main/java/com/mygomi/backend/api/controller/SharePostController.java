package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.SharePostRequestDto;
import com.mygomi.backend.api.dto.response.CommonResponse;
import com.mygomi.backend.api.dto.response.ReservationStatusResponseDto;
import com.mygomi.backend.api.dto.response.SharePostResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.share.ShareCategory;
import com.mygomi.backend.domain.share.ShareStatus;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.service.AddressService;
import com.mygomi.backend.service.SharePostReservationService;
import com.mygomi.backend.service.SharePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Share Posts", description = "Share post API")
@RestController
@RequestMapping("/api/share-posts")
@RequiredArgsConstructor
public class SharePostController {

    private final SharePostService sharePostService;
    private final SharePostReservationService sharePostReservationService;
    private final UserRepository userRepository;
    private final AddressService addressService;

    @Operation(summary = "Create post", description = "Creates a share post with optional images (max 5).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<SharePostResponseDto>> createPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") @Valid SharePostRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        Long userId = getUserIdFromToken(userDetails);
        SharePostResponseDto response = sharePostService.createPost(userId, request, images);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success(response));
    }

    @Operation(summary = "Get post detail", description = "Returns a single share post by id.")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<SharePostResponseDto>> getPost(@PathVariable Long id) {
        SharePostResponseDto response = sharePostService.getPost(id);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "Get post list", description = "Returns share posts with optional ward/category/status filters.")
    @GetMapping
    public ResponseEntity<CommonResponse<Page<SharePostResponseDto>>> getPosts(
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) ShareCategory category,
            @RequestParam(defaultValue = "OPEN") ShareStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SharePostResponseDto> response = sharePostService.getPosts(ward, category, status, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "Get my posts", description = "Returns posts created by the logged-in user.")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<SharePostResponseDto>>> getMyPosts(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getUserIdFromToken(userDetails);
        List<SharePostResponseDto> response = sharePostService.getMyPosts(userId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "Update post", description = "Updates a share post. Supports PATCH (recommended) and PUT.")
    @RequestMapping(
            value = "/{id}",
            method = {RequestMethod.PATCH, RequestMethod.PUT},
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CommonResponse<SharePostResponseDto>> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody SharePostRequestDto request) {

        Long userId = getUserIdFromToken(userDetails);
        SharePostResponseDto response = sharePostService.updatePost(userId, id, request);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "Update post with image changes", description = "Updates post fields and supports image replace/delete/add in one request. Supports PATCH (recommended) and PUT.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
            )
    )
    @RequestMapping(
            value = "/{id}",
            method = {RequestMethod.PATCH, RequestMethod.PUT},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommonResponse<SharePostResponseDto>> updatePostWithImages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestPart("request") @Valid SharePostRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "deleteImageIds", required = false) List<Long> deleteImageIds,
            @RequestParam(value = "replaceImages", defaultValue = "false") boolean replaceImages) {

        Long userId = getUserIdFromToken(userDetails);
        SharePostResponseDto response = sharePostService.updatePost(userId, id, request, images, deleteImageIds, replaceImages);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "Delete post", description = "Soft-deletes a share post. Only the owner can delete.")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<String>> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getUserIdFromToken(userDetails);
        sharePostService.deletePost(userId, id);
        return ResponseEntity.ok(CommonResponse.success("Post deleted successfully."));
    }

    @Operation(summary = "Get categories", description = "Returns available categories for share posts.")
    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<List<Map<String, String>>>> getCategories() {
        List<Map<String, String>> categories = java.util.Arrays.stream(ShareCategory.values())
                .map(category -> java.util.Map.of(
                        "code", category.name(),
                        "label", category.getDescription()
                ))
                .toList();

        return ResponseEntity.ok(CommonResponse.success(categories));
    }

    @Operation(summary = "Update post status", description = "Updates post status (OPEN/RESERVED/COMPLETED).")
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
            summary = "Get nearby posts by my primary address",
            description = "Returns posts near the logged-in user's primary address."
    )
    @GetMapping("/nearby/me")
    public ResponseEntity<CommonResponse<Page<SharePostResponseDto>>> getNearbyPostsByMyAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "distance") String sortBy,
            @PageableDefault(size = 20) Pageable pageable) {

        Long userId = getUserIdFromToken(userDetails);
        UserAddress primaryAddress = addressService.getPrimaryAddress(userId);

        Page<SharePostResponseDto> response = sharePostService.getNearbyPosts(
                primaryAddress.getLat(),
                primaryAddress.getLng(),
                radiusKm,
                sortBy,
                pageable
        );

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "예약 상태 조회", description = "해당 게시글/채팅방 기준 예약 동의 상태를 반환합니다.")
    @GetMapping("/{postId}/reservation/status")
    public ResponseEntity<CommonResponse<ReservationStatusResponseDto>> getReservationStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam Long roomId) {
        Long userId = getUserIdFromToken(userDetails);
        ReservationStatusResponseDto response = sharePostReservationService.getStatus(postId, roomId, userId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "예약 동의", description = "현재 사용자가 예약 동의하고, 양측 동의 시 게시글 상태를 RESERVED로 변경합니다.")
    @PostMapping("/{postId}/reservation/agree")
    public ResponseEntity<CommonResponse<ReservationStatusResponseDto>> agreeReservation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam Long roomId) {
        Long userId = getUserIdFromToken(userDetails);
        ReservationStatusResponseDto response = sharePostReservationService.agree(postId, roomId, userId);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    private Long getUserIdFromToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UsernameNotFoundException("Login information is missing.");
        }

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found. email=" + email));

        return user.getId();
    }
}
