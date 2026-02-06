package com.mygomi.backend.domain.address;

import com.mygomi.backend.domain.common.BaseTimeEntity;
import com.mygomi.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_addresses")
public class UserAddress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
    public UserAddress(User user, Area area, String prefecture, String ward, String town, String chome, String banchiText, Boolean isPrimary, Double lat, Double lng) {
        this.user = user;
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

    // 주소 정보 수정 (좌표 포함)
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