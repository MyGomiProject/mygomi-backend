package com.mygomi.backend.api.dto.request;

import com.mygomi.backend.domain.share.ShareCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter // @ModelAttribute(Form 데이터) 바인딩을 위해 Setter 필요
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharePostRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String description;

    @NotNull(message = "카테고리는 필수입니다.")
    private ShareCategory category;
    // 프론트에서 "FURNITURE"라고 보내면 자동으로 Enum으로 변환됩니다.

    // ==========================
    // 위치 정보 (지도 마커용)
    // ==========================

    private String prefecture; // 예: 도쿄도

    @NotBlank(message = "구(Ward) 정보는 필수입니다.")
    private String ward;       // 예: 아라카와구

    private String town;       // 예: 히가시닛포리

    private String address;    // 전체 주소 문자열

    @NotNull(message = "위도(lat)는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도(lng)는 필수입니다.")
    private Double lng;
}