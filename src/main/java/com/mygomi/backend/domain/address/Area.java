package com.mygomi.backend.domain.address;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "areas")
@Getter
@NoArgsConstructor
public class Area {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;
    private String prefecture;
    private String ward;
    private String town;
    private String chome;

    @Column(name = "banchi_text")
    private String banchiText;
}