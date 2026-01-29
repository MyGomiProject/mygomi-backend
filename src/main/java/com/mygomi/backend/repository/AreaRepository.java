package com.mygomi.backend.repository;

import com.mygomi.backend.domain.area.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByPrefectureAndWardAndTownAndChome(String prefecture, String ward, String town, String chome);
    Optional<Area> findByPrefectureAndWardAndTownAndChomeIsNull(String prefecture, String ward, String town);
}