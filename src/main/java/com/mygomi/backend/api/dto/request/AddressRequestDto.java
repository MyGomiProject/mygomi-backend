package com.mygomi.backend.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "주소 등록/수정 요청 DTO")
public class AddressRequestDto {

    @Schema(description = "도/부/현", example = "도쿄도")
    private String prefecture;

    @Schema(description = "구/시", example = "신주쿠구")
    private String ward;

    @Schema(description = "동/정", example = "오쿠보")
    private String town;

    @Schema(description = "쵸메 (숫자만 있어도 됨)", example = "1")
    private String chome;

    @Schema(description = "번지/호", example = "1-1")
    private String banchi;

    @Schema(description = "위도 (자동계산 됨)", example = "35.701")
    private Double lat;

    @Schema(description = "경도 (자동계산 됨)", example = "139.700")
    private Double lng;

    @Schema(description = "대표 주소 여부", example = "true")
    private Boolean isPrimary;
}