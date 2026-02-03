package com.mygomi.backend.repository;

import com.mygomi.backend.domain.collection.CollectionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CollectionRuleRepository extends JpaRepository<CollectionRule, Long> {
    List<CollectionRule> findByAreaId(Long areaId);
}