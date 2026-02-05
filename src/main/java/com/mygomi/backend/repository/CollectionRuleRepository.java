package com.mygomi.backend.repository;

import com.mygomi.backend.domain.collection.CollectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRuleRepository extends JpaRepository<CollectionRule, Long> {
    // 특정 지역(Area)의 ID로 모든 수거 규칙을 조회
    List<CollectionRule> findByAreaId(Long areaId);
}