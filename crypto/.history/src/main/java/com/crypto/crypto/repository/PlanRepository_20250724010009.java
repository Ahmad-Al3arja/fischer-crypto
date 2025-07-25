package com.crypto.crypto.repository;

import com.crypto.crypto.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    @Query("SELECT p FROM Plan p WHERE p.planLevel = :level")
    Optional<Plan> findByPlanLevel(Integer level);
    
    @Query("SELECT p FROM Plan p WHERE p.planLevel > :currentLevel ORDER BY p.planLevel ASC")
    Optional<Plan> findNextPlan(Integer currentLevel);
} 