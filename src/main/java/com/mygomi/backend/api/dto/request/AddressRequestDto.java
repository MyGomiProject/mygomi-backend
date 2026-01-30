package com.mygomi.backend.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressRequestDto {
    private String prefecture; // 도/현
    private String ward;       // 구
    private String town;       // 동/町
    private String chome;      // 丁目
    private String banchiText; // 번지
    private Boolean isPrimary; // 대표 주소 여부
    private Double lat;        // 위도
    private Double lng;        // 경도
}