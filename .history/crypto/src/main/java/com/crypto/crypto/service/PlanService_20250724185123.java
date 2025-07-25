package com.crypto.crypto.service;

import com.crypto.crypto.dto.PlanDTOs;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

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

        // Additional business validation
        validatePlanData(request.getPrice(), request.getDailyProfitMin(), request.getDailyProfitMax(), request.getPlanLevel());

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

        // Validate updated data
        BigDecimal price = request.getPrice() != null ? request.getPrice() : plan.getPrice();
        BigDecimal dailyMin = request.getDailyProfitMin() != null ? request.getDailyProfitMin() : plan.getDailyProfitMin();
        BigDecimal dailyMax = request.getDailyProfitMax() != null ? request.getDailyProfitMax() : plan.getDailyProfitMax();
        Integer level = request.getPlanLevel() != null ? request.getPlanLevel() : plan.getPlanLevel();
        
        validatePlanData(price, dailyMin, dailyMax, level);

        // Update fields
        if (request.getName() != null) plan.setName(request.getName());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        if (request.getMonthlyProfit() != null) plan.setMonthlyProfit(request.getMonthlyProfit());
        if (request.getDailyProfitMin() != null) plan.setDailyProfitMin(request.getDailyProfitMin());
        if (request.getDailyProfitMax() != null) plan.setDailyProfitMax(request.getDailyProfitMax());
        if (request.getPlanLevel() != null) plan.setPlanLevel(request.getPlanLevel());
        if (request.getDescription() != null) plan.setDescription(request.getDescription());
        if (request.getIsActive() != null) plan.setIsActive(request.getIsActive());

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

    public PlanDTOs.PlanStatsResponse getPlanStats(Long planId) {
        Plan plan = findById(planId);
        
        PlanDTOs.PlanStatsResponse response = new PlanDTOs.PlanStatsResponse();
        response.setPlanId(plan.getId());
        response.setPlanName(plan.getName());
        response.setCreatedAt(plan.getCreatedAt());

        // Get users on this plan
        List<User> usersOnPlan = userRepository.findByCurrentPlanId(planId);
        response.setTotalUsers(usersOnPlan.size());

        // Calculate statistics
        BigDecimal totalInvestment = usersOnPlan.stream()
                .map(User::getFrozenBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalInvestment(totalInvestment);

        BigDecimal totalProfitsPaid = usersOnPlan.stream()
                .map(user -> user.getTotalBalance().subtract(user.getFrozenBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalProfitsPaid(totalProfitsPaid);

        BigDecimal averageBalance = usersOnPlan.isEmpty() ? BigDecimal.ZERO :
                usersOnPlan.stream()
                        .map(User::getTotalBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(usersOnPlan.size()), 2, BigDecimal.ROUND_HALF_UP);
        response.setAverageUserBalance(averageBalance);

        // Get recent users (last 10)
        List<PlanDTOs.PlanStatsResponse.UserSummary> recentUsers = usersOnPlan.stream()
                .sorted((u1, u2) -> u2.getSubscriptionDate().compareTo(u1.getSubscriptionDate()))
                .limit(10)
                .map(this::convertToUserSummary)
                .collect(Collectors.toList());
        response.setRecentUsers(recentUsers);

        return response;
    }

    public AdminDTOs.PlansStatsResponse getAllPlansStats() {
        List<Plan> plans = planRepository.findAll();
        
        List<AdminDTOs.PlansStatsResponse.PlanStatsItem> planStats = plans.stream()
                .map(this::convertToPlanStatsItem)
                .collect(Collectors.toList());
        
        return new AdminDTOs.PlansStatsResponse(planStats);
    }

    private void validatePlanData(BigDecimal price, BigDecimal dailyMin, BigDecimal dailyMax, Integer level) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Plan price must be greater than 0");
        }
        
        if (dailyMin.compareTo(dailyMax) > 0) {
            throw new RuntimeException("Daily profit minimum cannot be greater than maximum");
        }
        
        if (level <= 0) {
            throw new RuntimeException("Plan level must be greater than 0");
        }
        
        if (level > 20) {
            throw new RuntimeException("Plan level cannot exceed 20");
        }

        // Business rule: Higher level plans should have higher prices
        List<Plan> existingPlans = planRepository.findAll();
        for (Plan existingPlan : existingPlans) {
            if (level > existingPlan.getPlanLevel() && price.compareTo(existingPlan.getPrice()) <= 0) {
                throw new RuntimeException("Higher level plans must have higher prices than lower level plans");
            }
            if (level < existingPlan.getPlanLevel() && price.compareTo(existingPlan.getPrice()) >= 0) {
                throw new RuntimeException("Lower level plans must have lower prices than higher level plans");
            }
        }
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
        response.setCreatedAt(plan.getCreatedAt());
        response.setUpdatedAt(plan.getUpdatedAt());
        response.setIsActive(plan.getIsActive());

        // Count users on this plan
        long userCount = planRepository.countUsersByPlanId(plan.getId());
        response.setTotalUsers((int) userCount);

        return response;
    }

    private PlanDTOs.PlanStatsResponse.UserSummary convertToUserSummary(User user) {
        PlanDTOs.PlanStatsResponse.UserSummary summary = new PlanDTOs.PlanStatsResponse.UserSummary();
        summary.setUserId(user.getId());
        summary.setUsername(user.getDisplayUsername());
        summary.setTotalBalance(user.getTotalBalance());
        summary.setJoinedAt(user.getSubscriptionDate());
        return summary;
    }

    private AdminDTOs.PlansStatsResponse.PlanStatsItem convertToPlanStatsItem(Plan plan) {
        AdminDTOs.PlansStatsResponse.PlanStatsItem item = new AdminDTOs.PlansStatsResponse.PlanStatsItem();
        item.setPlanId(plan.getId());
        item.setPlanName(plan.getName());
        item.setPrice(plan.getPrice());
        item.setPlanLevel(plan.getPlanLevel());
        item.setIsActive(plan.getIsActive());
        item.setCreatedAt(plan.getCreatedAt());

        // Get users on this plan
        List<User> usersOnPlan = userRepository.findByCurrentPlanId(plan.getId());
        item.setTotalUsers(usersOnPlan.size());

        // Calculate total investment (frozen balance)
        BigDecimal totalInvestment = usersOnPlan.stream()
                .map(User::getFrozenBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        item.setTotalInvestment(totalInvestment);

        // Calculate average user balance
        if (!usersOnPlan.isEmpty()) {
            BigDecimal totalBalance = usersOnPlan.stream()
                    .map(User::getTotalBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setAverageUserBalance(totalBalance.divide(BigDecimal.valueOf(usersOnPlan.size()), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            item.setAverageUserBalance(BigDecimal.ZERO);
        }

        return item;
    }
}