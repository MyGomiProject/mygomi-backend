package com.mygomi.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WasteType {
    // 색상은 프론트엔드 달력에 예쁘게 나올 추천 색상 코드입니다.
    BURNABLE("타는 쓰레기", "#FF5733"),       // 붉은색
    NON_BURNABLE("타지 않는 쓰레기", "#3357FF"), // 파란색
    CAN_BOTTLE("캔/병", "#33FF57"),            // 초록색
    PAPER("종이류", "#8D6E63"),                // 갈색
    PLASTIC("플라스틱/비닐", "#FFFF33");        // 노란색

    private final String label; // Service에서 getLabel()로 호출함
    private final String color; // Service에서 getColor()로 호출함
}