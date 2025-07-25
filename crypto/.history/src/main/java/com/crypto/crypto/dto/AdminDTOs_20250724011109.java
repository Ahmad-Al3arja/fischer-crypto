package com.crypto.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDTOs {
    
    public static class UserListResponse {
        private List<UserSummary> users;
        
        public UserListResponse(List<UserSummary> users) {
            this.users = users;
        }
        
        public List<UserSummary> getUsers() { return users; }
        public void setUsers(List<UserSummary> users) { this.users = users; }
    }
    
    public static class UserSummary {
        private Long id;
        private String fullName;
        private String username;
        private String phoneNumber;
        private String planName;
        private BigDecimal totalBalance;
        private String status;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class UserDetailsResponse {
        private Long id;
        private String fullName;
        private String username;
        private String phoneNumber;
        private String planName;
        private BigDecimal totalBalance;
        private BigDecimal frozenBalance;
        private BigDecimal referralEarnings;
        private String status;
        private int directReferrals;
        private int secondLevelReferrals;
        private LocalDateTime subscriptionDate;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
        
        public BigDecimal getReferralEarnings() { return referralEarnings; }
        public void setReferralEarnings(BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getDirectReferrals() { return directReferrals; }
        public void setDirectReferrals(int directReferrals) { this.directReferrals = directReferrals; }
        
        public int getSecondLevelReferrals() { return secondLevelReferrals; }
        public void setSecondLevelReferrals(int secondLevelReferrals) { this.secondLevelReferrals = secondLevelReferrals; }
        
        public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
        public void setSubscriptionDate(LocalDateTime subscriptionDate) { this.subscriptionDate = subscriptionDate; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    public static class CreatePromoCodeRequest {
        @NotBlank(message = "Promo code is required")
        private String code;
        
        @NotNull(message = "Bonus value is required")
        @DecimalMin(value = "0.01", message = "Bonus value must be greater than 0")
        private BigDecimal bonusValue;
        
        @NotNull(message = "Usage limit is required")
        private Integer usageLimit;
        
        private LocalDateTime expiresAt;
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public BigDecimal getBonusValue() { return bonusValue; }
        public void setBonusValue(BigDecimal bonusValue) { this.bonusValue = bonusValue; }
        
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }
    
    public static class UpdateBalanceRequest {
        @NotNull(message = "Amount is required")
        private BigDecimal amount;
        
        @NotBlank(message = "Operation type is required")
        private String operation; // ADD or SUBTRACT
        
        private String reason;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
} 