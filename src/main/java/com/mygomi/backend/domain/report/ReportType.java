package com.mygomi.backend.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {
    SHARE_POST("게시글 신고"),
    INFO("잘못된 정보 신고");

    private final String description;
}
