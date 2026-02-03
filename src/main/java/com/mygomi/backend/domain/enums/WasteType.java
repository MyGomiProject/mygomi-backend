package com.mygomi.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WasteType {
    BURNABLE("타는 쓰레기"),
    NON_BURNABLE("타지 않는 쓰레기"),
    CAN_BOTTLE("캔/병"),
    PAPER("종이류"),
    PLASTIC("플라스틱/비닐");

    private final String description;
}