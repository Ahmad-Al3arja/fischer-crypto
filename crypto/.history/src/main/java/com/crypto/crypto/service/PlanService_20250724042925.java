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
    
    public PlanDTOs.PlanResponse getPlanById(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return convertToPlanResponse(plan);
    }
    
    public Plan findNextPlan(Integer currentLevel) {
        return planRepository.findNextPlan(currentLevel).orElse(null);
    }
    
    public Plan findById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
    }
    
    public PlanDTOs.PlanResponse createPlan(PlanDTOs.CreatePlanRequest request) {
        // Validate plan level doesn't already exist
        if (planRepository.findByPlanLevel(request.getPlanLevel()).isPresent()) {
            throw new RuntimeException("Plan level " + request.getPlanLevel() + " already exists");
        }
        
        Plan plan = new Plan();
        plan.setName(request.getName());
        plan.setPrice(request.getPrice());
        plan.setMonthlyProfit(request.getMonthlyProfit());
        plan.setDailyProfitMin(request.getDailyProfitMin());
        plan.setDailyProfitMax(request.getDailyProfitMax());
        plan.setPlanLevel(request.getPlanLevel());
        
        planRepository.save(plan);
        return convertToPlanResponse(plan);
    }
    
    public PlanDTOs.PlanResponse updatePlan(Long planId, PlanDTOs.UpdatePlanRequest request) {
        Plan plan = findById(planId);
        
        // Check if new plan level conflicts with existing plan (excluding current plan)
        if (request.getPlanLevel() != null && !request.getPlanLevel().equals(plan.getPlanLevel())) {
            planRepository.findByPlanLevel(request.getPlanLevel()).ifPresent(existingPlan -> {
                if (!existingPlan.getId().equals(planId)) {
                    throw new RuntimeException("Plan level " + request.getPlanLevel() + " already exists");
                }
            });
        }
        
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        if (request.getMonthlyProfit() != null) plan.setMonthlyProfit(request.getMonthlyProfit());
        if (request.getDailyProfitMin() != null) plan.setDailyProfitMin(request.getDailyProfitMin());
        if (request.getDailyProfitMax() != null) plan.setDailyProfitMax(request.getDailyProfitMax());
        if (request.getPlanLevel() != null) plan.setPlanLevel(request.getPlanLevel());
        
        planRepository.save(plan);
        return convertToPlanResponse(plan);
    }
    
    public void deletePlan(Long planId) {
        Plan plan = findById(planId);
        
        // Check if any users are currently on this plan
        long userCount = planRepository.countUsersByPlanId(planId);
        if (userCount > 0) {
            throw new RuntimeException("Cannot delete plan: " + userCount + " users are currently subscribed to this plan");
        }
        
        planRepository.delete(plan);
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