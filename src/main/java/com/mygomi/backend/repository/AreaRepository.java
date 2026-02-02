package com.mygomi.backend.repository;

import com.mygomi.backend.domain.area.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {

    // 🔍 변경: Optional -> List (일단 후보군을 다 가져옴)
    List<Area> findByPrefectureAndWardAndTownAndChome(String prefecture, String ward, String town, String chome);

    // 쵸메가 없는 경우도 혹시 모르니 List로
    List<Area> findByPrefectureAndWardAndTownAndChomeIsNull(String prefecture, String ward, String town);
}