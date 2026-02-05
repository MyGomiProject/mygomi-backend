package com.mygomi.backend.repository;

import com.mygomi.backend.domain.address.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    // 🔍 변경: Optional -> List (후보군 다 가져오기 위해)
    List<Area> findByPrefectureAndWardAndTownAndChome(String prefecture, String ward, String town, String chome);

    // 쵸메가 없는 경우도 처리
    List<Area> findByPrefectureAndWardAndTownAndChomeIsNull(String prefecture, String ward, String town);
}