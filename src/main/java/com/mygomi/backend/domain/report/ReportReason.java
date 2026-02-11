package com.mygomi.backend.domain.report;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    FAKE_POST("허위 게시글"),
    OFFENSIVE("불쾌한 게시글 / 혐오 표현"),
    SPAM("스팸 / 광고성 게시글"),
    PRIVACY("개인정보 노출"),
    COPYRIGHT("저작권 침해"),
    INAPPROPRIATE("부적절한 거래 유도"),
    OTHER("기타");

    private final String description;
}
