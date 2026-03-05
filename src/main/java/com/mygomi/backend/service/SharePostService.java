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
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.ReportRepository;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharePostService {

    private final SharePostRepository sharePostRepository;
    private final SharePostImageRepository sharePostImageRepository;
    private final ReportRepository reportRepository;
    private final AddressService addressService;
    private final UserRepository userRepository;

    private static final List<ReportStatus> PENDING_REPORT_STATUSES = List.of(
            ReportStatus.PENDING,
            ReportStatus.IN_REVIEW
    );
    private static final int MAX_IMAGE_COUNT = 5;

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
            List<MultipartFile> validImages = filterValidImages(images);
            validateImageCount(0, validImages.size());
            uploadAndSaveImages(savedPost, validImages, 0);
        }

        return toResponse(savedPost);
    }

    @Transactional
    public SharePostResponseDto getPost(Long postId) {
        SharePost post = findVisiblePostById(postId);
        post.incrementViewCount();
        return toResponse(post);
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

        return mapPageWithPostMeta(posts, pageable);
    }

    public List<SharePostResponseDto> getMyPosts(Long userId) {
        List<SharePost> posts = sharePostRepository.findByUserIdAndStatusNot(userId, ShareStatus.DELETED);
        return mapListWithPostMeta(posts);
    }

    @Transactional
    public SharePostResponseDto updatePost(Long userId, Long postId, SharePostRequestDto request) {
        return updatePost(userId, postId, request, null, null, false);
    }

    @Transactional
    public SharePostResponseDto updatePost(
            Long userId,
            Long postId,
            SharePostRequestDto request,
            List<MultipartFile> newImages,
            List<Long> deleteImageIds,
            boolean replaceImages
    ) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        // request JSON에 들어온 필드만 반영 (null 값은 무시)
        post.update(
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getPrefecture(),
                request.getWard(),
                request.getTown(),
                request.getLat(),
                request.getLng()
        );

        // 이미지 정책:
        // - 새 images가 오면: 기존 이미지를 모두 비우고 새 파일들로 완전 교체
        // - 새 images가 아예 안 오면: 기존 이미지를 그대로 유지
        boolean hasNewImages = newImages != null && !filterValidImages(newImages).isEmpty();
        if (hasNewImages) {
            applyImageChanges(post, newImages, null, true);
        }

        return toResponse(post);
    }

    @Transactional
    public SharePostResponseDto updateStatus(Long userId, Long postId, ShareStatus status) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        post.updateStatus(status);
        return toResponse(post);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);
        post.softDelete();
    }

    public Page<SharePostResponseDto> getNearbyPosts(Double lat, Double lng, Double radiusKm, String sortBy, Pageable pageable) {
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

        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                pagedPosts.stream().map(SharePost::getId).toList()
        );
        Map<Long, String> authorMap = getAuthorNicknameMap(pagedPosts);

        List<SharePostResponseDto> dtos = pagedPosts.stream()
                .map(post -> {
                    double distance = calculateDistance(lat, lng, post.getLat(), post.getLng());
                    long pendingCount = pendingCounts.getOrDefault(post.getId(), 0L);
                    String author = authorMap.get(post.getUserId());
                    return SharePostResponseDto.fromWithDistance(post, distance, author, pendingCount);
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
        String author = getAuthorNickname(post.getUserId());
        return SharePostResponseDto.from(post, author, pendingCount);
    }

    private Page<SharePostResponseDto> mapPageWithPostMeta(Page<SharePost> posts, Pageable pageable) {
        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                posts.getContent().stream().map(SharePost::getId).toList()
        );
        Map<Long, String> authorMap = getAuthorNicknameMap(posts.getContent());

        List<SharePostResponseDto> dtos = posts.getContent().stream()
                .map(post -> SharePostResponseDto.from(
                        post,
                        authorMap.get(post.getUserId()),
                        pendingCounts.getOrDefault(post.getId(), 0L)
                ))
                .toList();

        return new PageImpl<>(dtos, pageable, posts.getTotalElements());
    }

    private List<SharePostResponseDto> mapListWithPostMeta(List<SharePost> posts) {
        Map<Long, Long> pendingCounts = getPendingReportCountMap(
                posts.stream().map(SharePost::getId).toList()
        );
        Map<Long, String> authorMap = getAuthorNicknameMap(posts);

        return posts.stream()
                .map(post -> SharePostResponseDto.from(
                        post,
                        authorMap.get(post.getUserId()),
                        pendingCounts.getOrDefault(post.getId(), 0L)
                ))
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

    private String getAuthorNickname(Long userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse(null);
    }

    private Map<Long, String> getAuthorNicknameMap(List<SharePost> posts) {
        List<Long> userIds = posts.stream()
                .map(SharePost::getUserId)
                .distinct()
                .toList();

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));
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

    private void applyImageChanges(
            SharePost post,
            List<MultipartFile> newImages,
            List<Long> deleteImageIds,
            boolean replaceImages
    ) {
        List<MultipartFile> validNewImages = filterValidImages(newImages);
        List<Long> targetDeleteIds = deleteImageIds == null ? List.of() : deleteImageIds;

        int currentCount = post.getImages().size();
        int deleteCount;

        if (replaceImages) {
            deleteCount = currentCount;
            validateImageCount(0, validNewImages.size());
            removeImages(post, List.copyOf(post.getImages()));
            uploadAndSaveImages(post, validNewImages, 0);
            normalizeImageOrder(post);
            return;
        }

        if (!targetDeleteIds.isEmpty()) {
            List<SharePostImage> deleteTargets = sharePostImageRepository.findByIdInAndSharePostId(targetDeleteIds, post.getId());
            if (deleteTargets.size() != targetDeleteIds.size()) {
                throw new IllegalArgumentException("Some image ids do not belong to this post.");
            }
            deleteCount = deleteTargets.size();
            validateImageCount(currentCount - deleteCount, validNewImages.size());
            removeImages(post, deleteTargets);
        } else {
            validateImageCount(currentCount, validNewImages.size());
        }

        if (!validNewImages.isEmpty()) {
            int startOrder = post.getImages().stream()
                    .map(SharePostImage::getDisplayOrder)
                    .max(Integer::compareTo)
                    .map(maxOrder -> maxOrder + 1)
                    .orElse(0);
            uploadAndSaveImages(post, validNewImages, startOrder);
        }

        normalizeImageOrder(post);
    }

    private List<MultipartFile> filterValidImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private void validateImageCount(int baseCount, int addCount) {
        if (baseCount + addCount > MAX_IMAGE_COUNT) {
            throw new IllegalArgumentException("A post can have up to 5 images.");
        }
    }

    private void removeImages(SharePost post, List<SharePostImage> deleteTargets) {
        for (SharePostImage image : deleteTargets) {
            deleteImageFile(image.getImageUrl());
        }
        post.getImages().removeIf(image ->
                deleteTargets.stream().anyMatch(target -> target.getId().equals(image.getId()))
        );
    }

    private void normalizeImageOrder(SharePost post) {
        for (int i = 0; i < post.getImages().size(); i++) {
            post.getImages().get(i).updateOrder(i);
        }
    }

    private void uploadAndSaveImages(SharePost post, List<MultipartFile> images, int startOrder) {
        String uploadFolder = System.getProperty("user.dir") + "/uploads/";

        File folder = new File(uploadFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        int order = startOrder;
        for (MultipartFile file : images) {
            String originalFileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFileName = uuid + "_" + originalFileName;
            File destination = new File(uploadFolder + savedFileName);

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

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return;
        }
        Path uploadRoot = Paths.get(System.getProperty("user.dir"), "uploads").normalize();
        Path target = Paths.get(System.getProperty("user.dir"))
                .resolve(imageUrl.substring(1))
                .normalize();

        if (!target.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete image file. path={}, message={}", target, e.getMessage());
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
}
