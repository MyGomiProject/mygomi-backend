package com.mygomi.backend.repository;

import com.mygomi.backend.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * 🔍 강력해진 검색 쿼리
     * 1. LOWER(): 대소문자 무시 (cd == CD)
     * 2. REPLACE(x, ' ', ''): DB 데이터의 띄어쓰기 무시 (깨진 병 == 깨진병)
     * 3. CONCAT('%', :keyword, '%'): 부분 일치 검색
     */
    @Query("SELECT i FROM Item i " +
            "WHERE i.ward = :ward " +
            "AND (" +
            "   LOWER(REPLACE(i.nameKo, ' ', '')) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR " +
            "   LOWER(REPLACE(i.exampleKeywords, ' ', '')) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    List<Item> searchByKeywordAndWard(@Param("keyword") String keyword, @Param("ward") String ward);
}