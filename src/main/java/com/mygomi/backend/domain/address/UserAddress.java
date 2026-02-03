package com.mygomi.backend.domain.address;

import com.mygomi.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
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
    public UserAddress(Long userId, Area area, String prefecture, String ward, String town, String chome, String banchiText, Boolean isPrimary, Double lat, Double lng) {
        this.userId = userId;
        this.area = area;
        this.prefecture = prefecture;
        this.ward = ward;
        this.town = town;
        this.chome = chome;
        this.banchiText = banchiText;
        this.isPrimary = isPrimary;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = LocalDateTime.now();
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
}