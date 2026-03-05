package com.mygomi.backend.api.dto.request;

import com.mygomi.backend.domain.share.ShareCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "나눔 게시글 작성/수정 요청 DTO")
public class SharePostRequestDto {

    // ✅ 프론트에서 입력하는 것만!
    @Schema(description = "게시글 제목", example = "이사 가서 무인양품 선반 나눔합니다")
    private String title;

    @Schema(description = "게시글 내용", example = "상태 깨끗합니다. 이번 주말에 가져가실 분 구해요.")
    private String description;

    @Schema(description = "카테고리 (FURNITURE, ELECTRONICS, CLOTHING, BOOKS, KITCHENWARE, SPORTS, TOYS, ETC)", example = "FURNITURE")
    private ShareCategory category;

    // 위치 정보 (선택) — 수정 시에만 들어올 수 있음
    @Schema(description = "도/부/현", example = "도쿄도")
    private String prefecture;

    @Schema(description = "구/시", example = "신주쿠구")
    private String ward;

    @Schema(description = "동/정", example = "오쿠보")
    private String town;

    @Schema(description = "위도", example = "35.701")
    private Double lat;

    @Schema(description = "경도", example = "139.700")
    private Double lng;
}