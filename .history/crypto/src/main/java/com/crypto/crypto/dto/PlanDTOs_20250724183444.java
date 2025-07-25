// Enhanced PlanDTOs.java
package com.crypto.crypto.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer totalUsers; // Number of users on this plan
        private Boolean isActive;
        
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
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    public static class PlansListResponse {
        private List<PlanResponse> plans;
        private Integer totalPlans;
        private String message;
        
        public PlansListResponse(List<PlanResponse> plans) {
            this.plans = plans;
            this.totalPlans = plans.size();
            this.message = "Plans retrieved successfully";
        }
        
        public List<PlanResponse> getPlans() { return plans; }
        public void setPlans(List<PlanResponse> plans) { this.plans = plans; }
        
        public Integer getTotalPlans() { return totalPlans; }
        public void setTotalPlans(Integer totalPlans) { this.totalPlans = totalPlans; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class CreatePlanRequest {
        @NotBlank(message = "Plan name is required")
        @Size(min = 2, max = 50, message = "Plan name must be between 2 and 50 characters")
        private String name;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
        private BigDecimal price;
        
        @NotNull(message = "Monthly profit is required")
        @DecimalMin(value = "0.01", message = "Monthly profit must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Monthly profit format is invalid")
        private BigDecimal monthlyProfit;
        
        @NotNull(message = "Daily profit minimum is required")
        @DecimalMin(value = "0.01", message = "Daily profit minimum must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Daily profit minimum format is invalid")
        private BigDecimal dailyProfitMin;
        
        @NotNull(message = "Daily profit maximum is required")
        @DecimalMin(value = "0.01", message = "Daily profit maximum must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Daily profit maximum format is invalid")
        private BigDecimal dailyProfitMax;
        
        @NotNull(message = "Plan level is required")
        @Min(value = 1, message = "Plan level must be at least 1")
        @Max(value = 20, message = "Plan level cannot exceed 20")
        private Integer planLevel;
        
        private String description;
        private Boolean isActive = true;
        
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
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    public static class UpdatePlanRequest {
        @Size(min = 2, max = 50, message = "Plan name must be between 2 and 50 characters")
        private String name;
        
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
        private BigDecimal price;
        
        @DecimalMin(value = "0.01", message = "Monthly profit must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Monthly profit format is invalid")
        private BigDecimal monthlyProfit;
        
        @DecimalMin(value = "0.01", message = "Daily profit minimum must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Daily profit minimum format is invalid")
        private BigDecimal dailyProfitMin;
        
        @DecimalMin(value = "0.01", message = "Daily profit maximum must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Daily profit maximum format is invalid")
        private BigDecimal dailyProfitMax;
        
        @Min(value = 1, message = "Plan level must be at least 1")
        @Max(value = 20, message = "Plan level cannot exceed 20")
        private Integer planLevel;
        
        private String description;
        private Boolean isActive;
        
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
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    public static class PlanStatsResponse {
        private Long planId;
        private String planName;
        private Integer totalUsers;
        private BigDecimal totalInvestment;
        private BigDecimal totalProfitsPaid;
        private BigDecimal averageUserBalance;
        private LocalDateTime createdAt;
        private List<UserSummary> recentUsers;
        
        // Getters and Setters
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public BigDecimal getTotalInvestment() { return totalInvestment; }
        public void setTotalInvestment(BigDecimal totalInvestment) { this.totalInvestment = totalInvestment; }
        
        public BigDecimal getTotalProfitsPaid() { return totalProfitsPaid; }
        public void setTotalProfitsPaid(BigDecimal totalProfitsPaid) { this.totalProfitsPaid = totalProfitsPaid; }
        
        public BigDecimal getAverageUserBalance() { return averageUserBalance; }
        public void setAverageUserBalance(BigDecimal averageUserBalance) { this.averageUserBalance = averageUserBalance; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public List<UserSummary> getRecentUsers() { return recentUsers; }
        public void setRecentUsers(List<UserSummary> recentUsers) { this.recentUsers = recentUsers; }
        
        public static class UserSummary {
            private Long userId;
            private String username;
            private BigDecimal totalBalance;
            private LocalDateTime joinedAt;
            
            // Getters and Setters
            public Long getUserId() { return userId; }
            public void setUserId(Long userId) { this.userId = userId; }
            
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            
            public BigDecimal getTotalBalance() { return totalBalance; }
            public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
            
            public LocalDateTime getJoinedAt() { return joinedAt; }
            public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
        }
    }
}