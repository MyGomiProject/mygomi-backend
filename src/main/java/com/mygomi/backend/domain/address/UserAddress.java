package com.mygomi.backend.domain.address;

import com.mygomi.backend.domain.address.Area;
import com.mygomi.backend.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_addresses")
public class UserAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 여기가 핵심! DB에 있는 Area 데이터와 연결합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    private String prefecture;
    private String ward;
    private String town;
    private String chome;

    @Column(name = "banchi_text")
    private String banchiText;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    private Double lat;
    private Double lng;

    @Builder
    public UserAddress(Long userId, Area area, String prefecture, String ward, String town, String chome, String banchiText, Boolean isPrimary, Double lat, Double lng) {
        this.userId = userId;
        this.area = area;
        this.prefecture = prefecture;
        this.ward = ward;
        this.town = town;
        this.chome = chome;
        this.banchiText = banchiText;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.lat = lat;
        this.lng = lng;
    }

    public void updateAddress(Area area, String prefecture, String ward, String town, String chome, String banchiText, Double lat, Double lng) {
        this.area = area;
        this.prefecture = prefecture;
        this.ward = ward;
        this.town = town;
        this.chome = chome;
        this.banchiText = banchiText;
        this.lat = lat;
        this.lng = lng;
    }

    public void updatePrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
}