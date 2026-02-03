package com.mygomi.backend.domain.collection;

import com.mygomi.backend.domain.address.Area;
import com.mygomi.backend.domain.common.BaseTimeEntity;
import com.mygomi.backend.domain.enums.RuleType;
import com.mygomi.backend.domain.enums.WasteType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "collection_rules")
public class CollectionRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @Enumerated(EnumType.STRING) // DB의 문자열("BURNABLE")을 Enum으로 매핑
    @Column(name = "waste_type")
    private WasteType wasteType;

    @Enumerated(EnumType.STRING) // DB의 문자열("WEEKDAY")을 Enum으로 매핑
    @Column(name = "rule_type")
    private RuleType ruleType;

    private String weekdays; // "MON,THU"

    @Column(name = "nth_weeks")
    private String nthWeeks; // "1,3"

    private String note;
}