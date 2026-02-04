package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.address.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AddressResponseDto {
    private Long id;
    private String fullAddress; // 전체 주소 문자열
    private Boolean isPrimary;
    private Long areaId; // 매칭된 수거 지역 ID

    public static AddressResponseDto from(UserAddress entity) {
        String fullAddr = String.format("%s %s %s %s %s",
                entity.getPrefecture(), entity.getWard(), entity.getTown(),
                entity.getChome(), entity.getBanchiText());

        return AddressResponseDto.builder()
                .id(entity.getId())
                .fullAddress(fullAddr)
                .isPrimary(entity.getIsPrimary())
                .areaId(entity.getArea() != null ? entity.getArea().getId() : null)
                .build();
    }
}