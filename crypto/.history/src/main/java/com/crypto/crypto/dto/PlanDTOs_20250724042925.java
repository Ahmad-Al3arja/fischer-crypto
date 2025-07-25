package com.crypto.crypto.dto;

import java.math.BigDecimal;
import java.util.List;

public class PlanDTOs {
    
    public static class PlanResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private BigDecimal monthlyProfit;
        private BigDecimal dailyProfitMin;
        private BigDecimal dailyProfitMax;
        private Integer planLevel;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getMonthlyProfit() { return monthlyProfit; }
        public void setMonthlyProfit(BigDecimal monthlyProfit) { this.monthlyProfit = monthlyProfit; }
        
        public BigDecimal getDailyProfitMin() { return dailyProfitMin; }
        public void setDailyProfitMin(BigDecimal dailyProfitMin) { this.dailyProfitMin = dailyProfitMin; }
        
        public BigDecimal getDailyProfitMax() { return dailyProfitMax; }
        public void setDailyProfitMax(BigDecimal dailyProfitMax) { this.dailyProfitMax = dailyProfitMax; }
        
        public Integer getPlanLevel() { return planLevel; }
        public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
    }
    
    public static class PlansListResponse {
        private List<PlanResponse> plans;
        
        public PlansListResponse(List<PlanResponse> plans) {
            this.plans = plans;
        }
        
        public List<PlanResponse> getPlans() { return plans; }
        public void setPlans(List<PlanResponse> plans) { this.plans = plans; }
    }
    
    public static class CreatePlanRequest {
        private String name;
        private BigDecimal price;
        private BigDecimal monthlyProfit;
        private BigDecimal dailyProfitMin;
        private BigDecimal dailyProfitMax;
        private Integer planLevel;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getMonthlyProfit() { return monthlyProfit; }
        public void setMonthlyProfit(BigDecimal monthlyProfit) { this.monthlyProfit = monthlyProfit; }
        
        public BigDecimal getDailyProfitMin() { return dailyProfitMin; }
        public void setDailyProfitMin(BigDecimal dailyProfitMin) { this.dailyProfitMin = dailyProfitMin; }
        
        public BigDecimal getDailyProfitMax() { return dailyProfitMax; }
        public void setDailyProfitMax(BigDecimal dailyProfitMax) { this.dailyProfitMax = dailyProfitMax; }
        
        public Integer getPlanLevel() { return planLevel; }
        public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
    }
    
    public static class UpdatePlanRequest {
        private String name;
        private BigDecimal price;
        private BigDecimal monthlyProfit;
        private BigDecimal dailyProfitMin;
        private BigDecimal dailyProfitMax;
        private Integer planLevel;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getMonthlyProfit() { return monthlyProfit; }
        public void setMonthlyProfit(BigDecimal monthlyProfit) { this.monthlyProfit = monthlyProfit; }
        
        public BigDecimal getDailyProfitMin() { return dailyProfitMin; }
        public void setDailyProfitMin(BigDecimal dailyProfitMin) { this.dailyProfitMin = dailyProfitMin; }
        
        public BigDecimal getDailyProfitMax() { return dailyProfitMax; }
        public void setDailyProfitMax(BigDecimal dailyProfitMax) { this.dailyProfitMax = dailyProfitMax; }
        
        public Integer getPlanLevel() { return planLevel; }
        public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
    }
} 