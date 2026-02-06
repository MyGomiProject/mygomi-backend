package com.mygomi.backend.domain.item;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import com.mygomi.backend.domain.enums.WasteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "items")
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_ko", nullable = false)
    private String nameKo; // 예: "세척되지 않는 플라스틱 용기"

    @Column(name = "name_ja")
    private String nameJa;

    @Column(name = "name_en")
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false)
    private WasteType wasteType; // BURNABLE, PLASTIC 등

    private String description; // 설명

    @Column(name = "example_keywords")
    private String exampleKeywords; // 검색용 키워드

    private String prefecture; // "도쿄도"
    private String ward;       // "시나가와구"
}