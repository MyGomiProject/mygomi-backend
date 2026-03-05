package com.mygomi.backend.repository;

import com.mygomi.backend.domain.address.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {

    // 1. town을 포함(LIKE)으로 유연하게 검색하고,
    // 2. 사용자가 입력한 chome과 일치하거나, DB의 chome이 '0'(전역)인 경우를 모두 찾습니다.
    @Query("SELECT a FROM Area a WHERE a.prefecture = :prefecture AND a.ward = :ward " +
            "AND a.town LIKE %:town% " +
            "AND (a.chome = :chome OR a.chome = '0')")
    List<Area> findCandidateAreas(
            @Param("prefecture") String prefecture,
            @Param("ward") String ward,
            @Param("town") String town,
            @Param("chome") String chome
    );

    // 사용자가 chome을 입력하지 않았을 때 (null 또는 빈 문자열)
    // DB의 chome이 null이거나 '0'(전역)인 경우를 찾습니다.
    @Query("SELECT a FROM Area a WHERE a.prefecture = :prefecture AND a.ward = :ward " +
            "AND a.town LIKE %:town% " +
            "AND (a.chome IS NULL OR a.chome = '0')")
    List<Area> findCandidateAreasWithoutChome(
            @Param("prefecture") String prefecture,
            @Param("ward") String ward,
            @Param("town") String town
    );
}