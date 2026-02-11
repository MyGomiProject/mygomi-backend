package com.mygomi.backend.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportStatus {
    PENDING("접수됨"),
    IN_REVIEW("검토 중"),
    RESOLVED("처리 완료"),
    DISMISSED("기각");

    private final String description;
}
