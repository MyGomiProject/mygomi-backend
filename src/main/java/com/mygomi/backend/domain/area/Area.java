package com.mygomi.backend.domain.area;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "areas")
public class Area extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;      // 간토
    private String prefecture;  // 도쿄도
    private String ward;        // 아라카와
    private String town;        // 히가시닛포리
    private String chome;       // 6 (숫자만 들어있음)

    @Column(name = "banchi_text")
    private String banchiText;  // 22-24, 40...
}