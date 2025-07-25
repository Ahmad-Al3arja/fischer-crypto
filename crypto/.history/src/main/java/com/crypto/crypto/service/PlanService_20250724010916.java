package com.crypto.crypto.service;

import com.crypto.crypto.dto.PlanDTOs;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {
    
    @Autowired
    private PlanRepository planRepository;
    
    public PlanDTOs.PlansListResponse getAllPlans() {
        List<Plan> plans = planRepository.findAll();
        
        List<PlanDTOs.PlanResponse> planResponses = plans.stream()
                .map(this::convertToPlanResponse)
                .collect(Collectors.toList());
        
        return new PlanDTOs.PlansListResponse(planResponses);
    }
    
    public Plan findNextPlan(Integer currentLevel) {
        return planRepository.findNextPlan(currentLevel).orElse(null);
    }
    
    public Plan findById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }
    
    private PlanDTOs.PlanResponse convertToPlanResponse(Plan plan) {
        PlanDTOs.PlanResponse response = new PlanDTOs.PlanResponse();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setPrice(plan.getPrice());
        response.setMonthlyProfit(plan.getMonthlyProfit());
        response.setDailyProfitMin(plan.getDailyProfitMin());
        response.setDailyProfitMax(plan.getDailyProfitMax());
        response.setPlanLevel(plan.getPlanLevel());
        return response;
    }
} 