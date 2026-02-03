package com.mygomi.backend.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressRequestDto {
    @Schema(description = "도/부/현", example = "도쿄도")
    private String prefecture;

    @Schema(description = "구/시", example = "아라카와")
    private String ward;

    @Schema(description = "동/정", example = "히가시닛포리")
    private String town;

    @Schema(description = "쵸메 (숫자만 있어도 됨)", example = "6")
    private String chome;

    @Schema(description = "번지/건물명", example = "22-24")
    private String banchi;

    @Schema(description = "위도")
    private Double lat;

    @Schema(description = "경도")
    private Double lng;
}