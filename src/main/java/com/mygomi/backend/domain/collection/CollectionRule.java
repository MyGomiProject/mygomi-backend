package com.mygomi.backend.domain.collection;

import com.mygomi.backend.domain.address.Area;
import com.mygomi.backend.domain.enums.WasteType;
import com.mygomi.backend.domain.enums.RuleType; // 파일에 이미 있는 Enum 활용
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollectionRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @Enumerated(EnumType.STRING)
    private WasteType wasteType;

    @Enumerated(EnumType.STRING)
    private RuleType ruleType; // WEEKDAY(매주), NTH_WEEKDAY(격주) 등

    // "MON,THU" 처럼 여러 요일이 들어올 수 있으므로 String으로 저장
    private String dayOfWeek;

    // "1,3" 처럼 여러 주차가 들어올 수 있으므로 String으로 저장
    private String weekNumbers;
}