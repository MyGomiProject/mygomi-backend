package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.address.UserAddress;
import lombok.Getter;

@Getter
public class AddressResponseDto {
    private final String prefecture;
    private final String ward;
    private final String town;
    private final String chome;
    private final String banchi;
    private final String fullAddress;

    public AddressResponseDto(UserAddress entity) {
        this.prefecture = entity.getPrefecture();
        this.ward = entity.getWard();
        this.town = entity.getTown();
        this.chome = entity.getChome();
        this.banchi = entity.getBanchiText();

        // 프론트에서 보여주기 편한 전체 주소 문자열 조합
        this.fullAddress = String.format("%s %s %s %s %s",
                prefecture, ward, town,
                (chome != null ? chome + "丁目" : ""),
                (banchi != null ? banchi : "")).trim();
    }
}