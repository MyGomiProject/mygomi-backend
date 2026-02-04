package com.mygomi.backend.domain.share;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShareCategory {
    FURNITURE("가구/인테리어"),
    ELECTRONICS("가전/디지털"),
    CLOTHING("의류/잡화"),
    BOOKS("도서/음반"),
    KITCHENWARE("주방/생활"),
    SPORTS("스포츠/레저"),
    TOYS("유아동/장난감"),
    ETC("기타");

    private final String description;
}