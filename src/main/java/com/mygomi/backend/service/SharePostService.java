package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.SharePostRequestDto;
import com.mygomi.backend.api.dto.response.SharePostResponseDto;
import com.mygomi.backend.domain.share.ShareCategory;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.share.SharePostImage;
import com.mygomi.backend.domain.share.ShareStatus;
import com.mygomi.backend.repository.SharePostImageRepository;
import com.mygomi.backend.repository.SharePostRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharePostService {

    private final SharePostRepository sharePostRepository;
    private final SharePostImageRepository sharePostImageRepository;
    private final AddressService addressService; // [추가] 주소 서비스


    @Transactional
    public SharePostResponseDto createPost(Long userId, SharePostRequestDto request, List<MultipartFile> images) {
        // 1. 작성자의 대표 주소 가져오기
        com.mygomi.backend.domain.address.UserAddress primaryAddress = addressService.getPrimaryAddress(userId);

        log.info("게시글 작성 - userId: {}, 대표주소: {} {}, 좌표: ({}, {})",
                userId, primaryAddress.getWard(), primaryAddress.getTown(),
                primaryAddress.getLat(), primaryAddress.getLng());

        // 2. 게시글 엔티티 생성 (대표 주소의 좌표 사용)
        SharePost post = SharePost.builder()
                .userId(userId)
                .title(request.getTitle())           // ← RequestDto에서
                .description(request.getDescription()) // ← RequestDto에서
                .category(request.getCategory())      // ← RequestDto에서
                .prefecture(primaryAddress.getPrefecture()) // ← 대표 주소에서
                .ward(primaryAddress.getWard())             // ← 대표 주소에서
                .town(primaryAddress.getTown())             // ← 대표 주소에서
                .lat(primaryAddress.getLat())               // ← 대표 주소에서
                .lng(primaryAddress.getLng())               // ← 대표 주소에서
                .build();

        // 3. 게시글 저장
        SharePost savedPost = sharePostRepository.save(post);

        // 4. 이미지 처리 (파일이 있다면)
        if (images != null && !images.isEmpty()) {
            uploadAndSaveImages(savedPost, images);
        }

        return SharePostResponseDto.from(savedPost);
    }

    /**
     * 게시글 단건 조회 (조회수 증가)
     */
    @Transactional
    public SharePostResponseDto getPost(Long postId) {
        SharePost post = findPostById(postId);

        // 조회수 증가
        post.incrementViewCount();

        return SharePostResponseDto.from(post);
    }
    /**
     * 게시글 목록 조회 (지역 + 카테고리 필터링)
     */
    public Page<SharePostResponseDto> getPosts(String ward, ShareCategory category, ShareStatus status, Pageable pageable) {
        Page<SharePost> posts;

        // 1. 지역 & 카테고리 둘 다 있음
        if (ward != null && !ward.isBlank() && category != null) {
            posts = sharePostRepository.findByWardAndCategoryAndStatusOrderByCreatedAtDesc(ward, category, status, pageable);
        }
        // 2. 지역만 있음
        else if (ward != null && !ward.isBlank()) {
            posts = sharePostRepository.findByWardAndStatusOrderByCreatedAtDesc(ward, status, pageable);
        }
        // 3. 카테고리만 있음
        else if (category != null) {
            posts = sharePostRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status, pageable);
        }
        // 4. 아무것도 없음 (전체 조회)
        else {
            posts = sharePostRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        }

        return posts.map(SharePostResponseDto::from);
    }
    /**
     * 내 게시글 조회
     */
    public List<SharePostResponseDto> getMyPosts(Long userId) {
        List<SharePost> posts = sharePostRepository.findByUserId(userId);
        return posts.stream()
                .map(SharePostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public SharePostResponseDto updatePost(Long userId, Long postId, SharePostRequestDto request) {
        SharePost post = findPostById(postId);

        // 작성자 검증
        validateOwner(post, userId);

        // 내용 수정
        post.update(request.getTitle(), request.getDescription(), request.getCategory());

        return SharePostResponseDto.from(post);
    }

    /**
     * 게시글 상태 변경 (예약중, 완료 등)
     */
    @Transactional
    public SharePostResponseDto updateStatus(Long userId, Long postId, ShareStatus status) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);

        post.updateStatus(status);

        return SharePostResponseDto.from(post);
    }

    /**
     * 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePost(Long userId, Long postId) {
        SharePost post = findPostById(postId);
        validateOwner(post, userId);

        post.softDelete(); // 상태를 DELETED로 변경
    }

    /**
     * 반경 내 게시글 조회 (지도용)
     */
    public Page<SharePostResponseDto> getNearbyPosts(Double lat, Double lng, Double radiusKm, String sortBy, Pageable pageable) {
        // 1. 반경 내 데이터 1차 조회 (OPEN 상태인 것만)
        List<SharePost> posts = sharePostRepository.findNearbyPosts(lat, lng, radiusKm);

        // 2. 정렬 처리 (거리순 vs 최신순)
        if ("distance".equalsIgnoreCase(sortBy)) {
            posts = sortByDistance(posts, lat, lng);
        }
        // latest는 쿼리에서 이미 created_at desc 라고 가정하거나, 여기서 다시 sort

        // 3. 페이징 처리 (List -> Page)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), posts.size());

        if (start > posts.size()) {
            return new PageImpl<>(List.of(), pageable, posts.size());
        }

        List<SharePost> pagedPosts = posts.subList(start, end);

        // 4. DTO 변환 (거리 포함)
        List<SharePostResponseDto> dtos = pagedPosts.stream()
                .map(post -> {
                    double distance = calculateDistance(lat, lng, post.getLat(), post.getLng());
                    return SharePostResponseDto.fromWithDistance(post, distance);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, posts.size());
    }


    private SharePost findPostById(Long id) {
        return sharePostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id=" + id));
    }

    private void validateOwner(SharePost post, Long userId) {
        if (!post.isOwnedBy(userId)) {
            throw new IllegalArgumentException("게시글 수정/삭제 권한이 없습니다.");
        }
    }

    private void uploadAndSaveImages(SharePost post, List<MultipartFile> images) {
        // 1. 이미지를 저장할 로컬 폴더 지정 (프로젝트 루트 경로/uploads)
        String uploadFolder = System.getProperty("user.dir") + "/uploads/";

        java.io.File folder = new java.io.File(uploadFolder);
        if (!folder.exists()) {
            folder.mkdirs(); // 폴더가 없으면 생성
        }

        int order = 0;
        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            // 2. 파일 이름 중복 방지 (UUID 사용)
            String originalFileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String savedFileName = uuid + "_" + originalFileName;

            // 3. 실제 파일 저장 경로
            java.io.File destination = new java.io.File(uploadFolder + savedFileName);

            try {
                // 4. 파일 저장 (내 컴퓨터 하드디스크로 복사)
                file.transferTo(destination);

                // 5. DB에는 "저장된 파일 경로"를 저장
                // (나중에 프론트에서 접근하려면 /uploads/파일명 형태로 줘야 함)
                String imageUrl = "/uploads/" + savedFileName;

                SharePostImage postImage = SharePostImage.builder()
                        .sharePost(post)
                        .imageUrl(imageUrl)
                        .displayOrder(order++)
                        .build();

                post.addImage(postImage);

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패: " + originalFileName, e);
            }
        }
    }

    // 거리 정렬
    private List<SharePost> sortByDistance(List<SharePost> posts, Double lat, Double lng) {
        return posts.stream()
                .sorted(Comparator.comparingDouble(post ->
                        calculateDistance(lat, lng, post.getLat(), post.getLng())))
                .collect(Collectors.toList());
    }

    // Haversine 거리 계산
    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}