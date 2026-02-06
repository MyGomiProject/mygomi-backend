package com.mygomi.backend.domain.share;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShareStatus {
    OPEN("나눔 가능"),
    RESERVED("예약됨"),
    COMPLETED("나눔 완료"),
    DELETED("삭제됨");

    private final String description;
}
