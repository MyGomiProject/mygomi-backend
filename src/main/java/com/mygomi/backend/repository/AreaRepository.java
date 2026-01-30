package com.mygomi.backend.repository;

import com.mygomi.backend.domain.address.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    // 정확히 일치하는 지역 찾기 (임시 로직)
    Optional<Area> findByPrefectureAndWardAndTownAndChome(String prefecture, String ward, String town, String chome);
}