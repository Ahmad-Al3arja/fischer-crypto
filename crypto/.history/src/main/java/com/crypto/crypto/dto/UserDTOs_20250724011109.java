package com.crypto.crypto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserDTOs {
    
    public static class DashboardResponse {
        private String fullName;
        private String username;
        private String phoneNumber;
        private String currentPlanName;
        private BigDecimal totalBalance;
        private BigDecimal totalProfits;
        private BigDecimal dailyProfit;
        private CounterStatus counterStatus;
        private boolean activationPending;
        private String activationMessage;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getCurrentPlanName() { return currentPlanName; }
        public void setCurrentPlanName(String currentPlanName) { this.currentPlanName = currentPlanName; }
        
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public BigDecimal getTotalProfits() { return totalProfits; }
        public void setTotalProfits(BigDecimal totalProfits) { this.totalProfits = totalProfits; }
        
        public BigDecimal getDailyProfit() { return dailyProfit; }
        public void setDailyProfit(BigDecimal dailyProfit) { this.dailyProfit = dailyProfit; }
        
        public CounterStatus getCounterStatus() { return counterStatus; }
        public void setCounterStatus(CounterStatus counterStatus) { this.counterStatus = counterStatus; }
        
        public boolean isActivationPending() { return activationPending; }
        public void setActivationPending(boolean activationPending) { this.activationPending = activationPending; }
        
        public String getActivationMessage() { return activationMessage; }
        public void setActivationMessage(String activationMessage) { this.activationMessage = activationMessage; }
    }
    
    public static class CounterStatus {
        private boolean isActive;
        private boolean isCompleted;
        private long remainingSeconds;
        private boolean needsReset;
        
        // Getters and Setters
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        
        public long getRemainingSeconds() { return remainingSeconds; }
        public void setRemainingSeconds(long remainingSeconds) { this.remainingSeconds = remainingSeconds; }
        
        public boolean isNeedsReset() { return needsReset; }
        public void setNeedsReset(boolean needsReset) { this.needsReset = needsReset; }
    }
    
    public static class ProfileResponse {
        private String fullName;
        private String username;
        private String phoneNumber;
        private String planName;
        private LocalDateTime subscriptionDate;
        private int numberOfReferrals;
        private int secondLevelReferrals;
        private BigDecimal totalBalance;
        private BigDecimal referralEarnings;
        private String referralLink;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
        public void setSubscriptionDate(LocalDateTime subscriptionDate) { this.subscriptionDate = subscriptionDate; }
        
        public int getNumberOfReferrals() { return numberOfReferrals; }
        public void setNumberOfReferrals(int numberOfReferrals) { this.numberOfReferrals = numberOfReferrals; }
        
        public int getSecondLevelReferrals() { return secondLevelReferrals; }
        public void setSecondLevelReferrals(int secondLevelReferrals) { this.secondLevelReferrals = secondLevelReferrals; }
        
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public BigDecimal getReferralEarnings() { return referralEarnings; }
        public void setReferralEarnings(BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
        
        public String getReferralLink() { return referralLink; }
        public void setReferralLink(String referralLink) { this.referralLink = referralLink; }
    }
} 