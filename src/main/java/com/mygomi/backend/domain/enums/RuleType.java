package com.mygomi.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RuleType {
    /**
     * 매주 특정 요일에 수거 (예: 매주 월, 목)
     * - weekdays: "MON, THU"
     * - nth_weeks: null
     */
    WEEKDAY("매주"),

    /**
     * 특정 주차의 요일에만 수거 (예: 1, 3주째 금요일)
     * - weekdays: "FRI"
     * - nth_weeks: "1,3" (콤마로 구분된 숫자)
     */
    NTH_WEEKDAY("특정 주 (격주 등)"),

    /**
     * 규칙적인 요일이 아님 (예: 연말연시, 비정기)
     * - 별도 로직이나 note 필드 참조 필요
     */
    CUSTOM("비정기/기타");

    private final String description;
}