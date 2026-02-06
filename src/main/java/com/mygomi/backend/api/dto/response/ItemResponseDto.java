package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.item.Item;
import lombok.Getter;

@Getter
public class ItemResponseDto {
    private final Long id;
    private final String name;           // "세척되지 않는 플라스틱 용기"
    private final String wasteType;      // "BURNABLE" (코드)
    private final String wasteTypeDesc;  // "타는 쓰레기" (한글 설명)
    private final String ward;           // "시나가와구"

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.name = item.getNameKo();
        this.wasteType = item.getWasteType().name();
        this.wasteTypeDesc = item.getWasteType().getDescription();
        this.ward = item.getWard();
    }
}