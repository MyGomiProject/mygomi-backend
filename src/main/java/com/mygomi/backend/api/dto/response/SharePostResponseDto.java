package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.share.ShareCategory;
import com.mygomi.backend.domain.share.SharePost;
import com.mygomi.backend.domain.share.ShareStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class SharePostResponseDto {
    private Long id;
    private Long userId;
    private String author;  // 작성자 닉네임
    private String title;
    private String description;

    private Integer viewCount;

    private ShareCategory category;
    private String categoryName;
    private ShareStatus status;
    private String statusName;

    private String prefecture;
    private String ward;
    private String town;
    private Double lat;
    private Double lng;

    private List<String> imageUrls;
    private String thumbnailUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Double distance;
    private boolean reported;
    private int pendingReportCount;

    public static SharePostResponseDto from(SharePost post) {
<<<<<<< HEAD
        return from(post, 0L);
    }

    public static SharePostResponseDto from(SharePost post, long pendingReportCount) {
=======
        return from(post, null);
    }
    
    public static SharePostResponseDto from(SharePost post, String author) {
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
        List<String> imageUrls = post.getImages().stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        return SharePostResponseDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .author(author)
                .title(post.getTitle())
                .description(post.getDescription())
                .viewCount(post.getViewCount())
                .category(post.getCategory())
                .categoryName(post.getCategory().getDescription())
                .status(post.getStatus())
                .statusName(post.getStatus().getDescription())
                .prefecture(post.getPrefecture())
                .ward(post.getWard())
                .town(post.getTown())
                .lat(post.getLat())
                .lng(post.getLng())
                .imageUrls(imageUrls)
                .thumbnailUrl(imageUrls.isEmpty() ? null : imageUrls.get(0))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .reported(pendingReportCount > 0)
                .pendingReportCount(Math.toIntExact(pendingReportCount))
                .build();
    }

    public static SharePostResponseDto fromWithDistance(SharePost post, Double distance) {
<<<<<<< HEAD
        return fromWithDistance(post, distance, 0L);
    }

    public static SharePostResponseDto fromWithDistance(SharePost post, Double distance, long pendingReportCount) {
        SharePostResponseDto dto = from(post, pendingReportCount);
=======
        return fromWithDistance(post, distance, null);
    }
    
    public static SharePostResponseDto fromWithDistance(SharePost post, Double distance, String author) {
        SharePostResponseDto dto = from(post, author);
>>>>>>> f66f16c56fb2959d9d5fa9c657da3ebcad2222e4
        return SharePostResponseDto.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .author(dto.getAuthor())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .viewCount(dto.getViewCount())
                .category(dto.getCategory())
                .categoryName(dto.getCategoryName())
                .status(dto.getStatus())
                .statusName(dto.getStatusName())
                .prefecture(dto.getPrefecture())
                .ward(dto.getWard())
                .town(dto.getTown())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .imageUrls(dto.getImageUrls())
                .thumbnailUrl(dto.getThumbnailUrl())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .distance(distance)
                .reported(dto.isReported())
                .pendingReportCount(dto.getPendingReportCount())
                .build();
    }
}
