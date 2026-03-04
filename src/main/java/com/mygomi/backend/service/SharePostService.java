package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.SharePostRequestDto;
import com.mygomi.backend.api.dto.response.SharePostResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.report.ReportStatus;
import com.mygomi.backend.domain.report.ReportType;
import com.mygomi.backend.domain.share.ShareCategory;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.share.SharePostImage;
import com.mygomi.backend.domain.share.ShareStatus;
<<<<<<< HEAD
import com.mygomi.backend.repository.ReportRepository;
=======
import com.mygomi.backend.domain.user.User;
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
import com.mygomi.backend.repository.SharePostImageRepository;
import com.mygomi.backend.repository.SharePostRepository;
import com.mygomi.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharePostService {

    private final SharePostRepository sharePostRepository;
    private final SharePostImageRepository sharePostImageRepository;
<<<<<<< HEAD
    private final ReportRepository reportRepository;
    private final AddressService addressService;
=======
    private final AddressService addressService; // [추가] 주소 서비스
    private final UserRepository userRepository; // 작성자 닉네임 조회용
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4

    private static final List<ReportStatus> PENDING_REPORT_STATUSES = List.of(
            ReportStatus.PENDING,
            ReportStatus.IN_REVIEW
    );

    @Transactional
    public SharePostResponseDto createPost(Long userId, SharePostRequestDto request, List<MultipartFile> images) {
        UserAddress primaryAddress = addressService.getPrimaryAddress(userId);

        log.info("Create post - userId: {}, ward/town: {} {}, coordinates: ({}, {})",
                userId,
                primaryAddress.getWard(),
                primaryAddress.getTown(),
                primaryAddress.getLat(),
                primaryAddress.getLng());

        SharePost post = SharePost.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .prefecture(primaryAddress.getPrefecture())
                .ward(primaryAddress.getWard())
                .town(primaryAddress.getTown())
                .lat(primaryAddress.getLat())
                .lng(primaryAddress.getLng())
                .build();

        SharePost savedPost = sharePostRepository.save(post);

        if (images != null && !images.isEmpty()) {
            uploadAndSaveImages(savedPost, images);
        }

<<<<<<< HEAD
        return toResponse(savedPost);
=======
        // 5. 작성자 닉네임 조회
        String author = getAuthorNickname(savedPost.getUserId());
        return SharePostResponseDto.from(savedPost, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    }

    @Transactional
    public SharePostResponseDto getPost(Long postId) {
        SharePost post = findVisiblePostById(postId);
        post.incrementViewCount();
<<<<<<< HEAD
        return toResponse(post);
=======

        // 작성자 닉네임 조회
        String author = getAuthorNickname(post.getUserId());
        return SharePostResponseDto.from(post, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    }

    public Page<SharePostResponseDto> getPosts(String ward, ShareCategory category, ShareStatus status, Pageable pageable) {
        Page<SharePost> posts;

        if (ward != null && !ward.isBlank() && category != null) {
            posts = sharePostRepository.findByWardAndCategoryAndStatusOrderByCreatedAtDesc(ward, category, status, pageable);
        } else if (ward != null && !ward.isBlank()) {
            posts = sharePostRepository.findByWardAndStatusOrderByCreatedAtDesc(ward, status, pageable);
        } else if (category != null) {
            posts = sharePostRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status, pageable);
        } else {
            posts = sharePostRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

<<<<<<< HEAD
        return mapPageWithReportInfo(posts, pageable);
    }

    public List<SharePostResponseDto> getMyPosts(Long userId) {
        List<SharePost> posts = sharePostRepository.findByUserIdAndStatusNot(userId, ShareStatus.DELETED);
        return mapListWithReportInfo(posts);
=======
        // 작성자 닉네임 조회 (배치 조회로 성능 최적화)
        Map<Long, String> authorMap = getAuthorNicknameMap(posts.getContent());
        
        return posts.map(post -> {
            String author = authorMap.getOrDefault(post.getUserId(), "알 수 없음");
            return SharePostResponseDto.from(post, author);
        });
    }
    /**
     * 내 게시글 조회
     */
    public List<SharePostResponseDto> getMyPosts(Long userId) {
        List<SharePost> posts = sharePostRepository.findByUserId(userId);
        
        // 작성자 닉네임 조회 (배치 조회로 성능 최적화)
        Map<Long, String> authorMap = getAuthorNicknameMap(posts);
        
        return posts.stream()
                .map(post -> {
                    String author = authorMap.getOrDefault(post.getUserId(), "알 수 없음");
                    return SharePostResponseDto.from(post, author);
                })
                .collect(Collectors.toList());
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    }

    @Transactional
    public SharePostResponseDto updatePost(Long userId, Long postId, SharePostRequestDto request) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        post.update(request.getTitle(), request.getDescription(), request.getCategory());
<<<<<<< HEAD
        return toResponse(post);
=======

        // 작성자 닉네임 조회
        String author = getAuthorNickname(post.getUserId());
        return SharePostResponseDto.from(post, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    }

    @Transactional
    public SharePostResponseDto updateStatus(Long userId, Long postId, ShareStatus status) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        post.updateStatus(status);
<<<<<<< HEAD
        return toResponse(post);
=======

        // 작성자 닉네임 조회
        String author = getAuthorNickname(post.getUserId());
        return SharePostResponseDto.from(post, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        post.softDelete();
    }

<<<<<<< HEAD
    public Page<SharePostResponseDto> getNearbyPosts(Double lat, Double lng, Double radiusKm, String sortBy, Pageable pageable) {
=======
    /**
     * 반경 내 게시글 조회 (지도용)
     * OPEN과 RESERVED 상태 게시물만 반환 (COMPLETED, DELETED 제외)
     */
    public Page<SharePostResponseDto> getNearbyPosts(Double lat, Double lng, Double radiusKm, String sortBy, Pageable pageable) {
        // 1. 반경 내 데이터 1차 조회 (OPEN, RESERVED 상태만)
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
        List<SharePost> posts = sharePostRepository.findNearbyPosts(lat, lng, radiusKm);

        if ("distance".equalsIgnoreCase(sortBy)) {
            posts = sortByDistance(posts, lat, lng);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), posts.size());

        if (start > posts.size()) {
            return new PageImpl<>(List.of(), pageable, posts.size());
        }

        List<SharePost> pagedPosts = posts.subList(start, end);

<<<<<<< HEAD
        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                pagedPosts.stream().map(SharePost::getId).toList()
        );

        List<SharePostResponseDto> dtos = pagedPosts.stream()
                .map(post -> {
                    double distance = calculateDistance(lat, lng, post.getLat(), post.getLng());
                    long pendingCount = pendingCounts.getOrDefault(post.getId(), 0L);
                    return SharePostResponseDto.fromWithDistance(post, distance, pendingCount);
=======
        // 4. 작성자 닉네임 조회 (배치 조회로 성능 최적화)
        Map<Long, String> authorMap = getAuthorNicknameMap(pagedPosts);
        
        // 5. DTO 변환 (거리 포함)
        List<SharePostResponseDto> dtos = pagedPosts.stream()
                .map(post -> {
                    double distance = calculateDistance(lat, lng, post.getLat(), post.getLng());
                    String author = authorMap.getOrDefault(post.getUserId(), "알 수 없음");
                    return SharePostResponseDto.fromWithDistance(post, distance, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, posts.size());
    }

    private SharePostResponseDto toResponse(SharePost post) {
        long pendingCount = reportRepository.countByTargetPostIdAndTypeAndStatusIn(
                post.getId(),
                ReportType.SHARE_POST,
                PENDING_REPORT_STATUSES
        );
        return SharePostResponseDto.from(post, pendingCount);
    }

    private Page<SharePostResponseDto> mapPageWithReportInfo(Page<SharePost> posts, Pageable pageable) {
        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                posts.getContent().stream().map(SharePost::getId).toList()
        );

        List<SharePostResponseDto> dtos = posts.getContent().stream()
                .map(post -> SharePostResponseDto.from(post, pendingCounts.getOrDefault(post.getId(), 0L)))
                .toList();

        return new PageImpl<>(dtos, pageable, posts.getTotalElements());
    }

    private List<SharePostResponseDto> mapListWithReportInfo(List<SharePost> posts) {
        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                posts.stream().map(SharePost::getId).toList()
        );

        return posts.stream()
                .map(post -> SharePostResponseDto.from(post, pendingCounts.getOrDefault(post.getId(), 0L)))
                .toList();
    }

    private Map<Long, Long> getPendingReportCountMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = reportRepository.countByTargetPostIdsAndTypeAndStatusIn(
                postIds,
                ReportType.SHARE_POST,
                PENDING_REPORT_STATUSES
        );

        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : rows) {
            Long postId = (Long) row[0];
            Long count = (Long) row[1];
            countMap.put(postId, count);
        }
        return countMap;
    }

    private SharePost findPostById(Long id) {
        return sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found. id=" + id));
    }

    private SharePost findVisiblePostById(Long id) {
        return sharePostRepository.findByIdAndStatusNot(id, ShareStatus.DELETED)
                .orElseThrow(() -> new IllegalArgumentException("Post not found. id=" + id));
    }

    private void validateOwner(SharePost post, Long userId) {
        if (!post.isOwnedBy(userId)) {
            throw new IllegalArgumentException("No permission to modify this post.");
        }
    }

    private void uploadAndSaveImages(SharePost post, List<MultipartFile> images) {
        String uploadFolder = System.getProperty("user.dir") + "/uploads/";

        java.io.File folder = new java.io.File(uploadFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        int order = 0;
        for (MultipartFile file : images) {
            if (file.isEmpty()) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFileName = uuid + "_" + originalFileName;
            java.io.File destination = new java.io.File(uploadFolder + savedFileName);

            try {
                file.transferTo(destination);

                String imageUrl = "/uploads/" + savedFileName;
                SharePostImage postImage = SharePostImage.builder()
                        .sharePost(post)
                        .imageUrl(imageUrl)
                        .displayOrder(order++)
                        .build();

                post.addImage(postImage);
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed: " + originalFileName, e);
            }
        }
    }

    private List<SharePost> sortByDistance(List<SharePost> posts, Double lat, Double lng) {
        return posts.stream()
                .sorted(Comparator.comparingDouble(post -> calculateDistance(lat, lng, post.getLat(), post.getLng())))
                .collect(Collectors.toList());
    }

    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int r = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
<<<<<<< HEAD
}
=======
    
    /**
     * 작성자 닉네임 조회 (단건)
     */
    private String getAuthorNickname(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("알 수 없음");
    }
    
    /**
     * 작성자 닉네임 조회 (배치 - 성능 최적화)
     */
    private Map<Long, String> getAuthorNicknameMap(List<SharePost> posts) {
        List<Long> userIds = posts.stream()
                .map(SharePost::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
    }
}
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
