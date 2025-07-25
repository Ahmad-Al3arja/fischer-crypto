package com.crypto.crypto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class UserDTOs {
    
    public static class UpdateProfileRequest {
        private String fullName;
        private String username;
        
        // Getters and Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    public static class ReferralListResponse {
        private List<ReferralDetail> referrals;
        
        public ReferralListResponse(List<ReferralDetail> referrals) {
            this.referrals = referrals;
        }
        
        // Getters and Setters
        public List<ReferralDetail> getReferrals() { return referrals; }
        public void setReferrals(List<ReferralDetail> referrals) { this.referrals = referrals; }
    }
    
    public static class ReferralEarningsResponse {
        private BigDecimal totalEarnings;
        private List<ReferralDetail> earnings;
        
        public ReferralEarningsResponse(BigDecimal totalEarnings, List<ReferralDetail> earnings) {
            this.totalEarnings = totalEarnings;
            this.earnings = earnings;
        }
        
        // Getters and Setters
        public BigDecimal getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(BigDecimal totalEarnings) { this.totalEarnings = totalEarnings; }
        
        public List<ReferralDetail> getEarnings() { return earnings; }
        public void setEarnings(List<ReferralDetail> earnings) { this.earnings = earnings; }
    }
    
    public static class DailyCounterResponse {
        private boolean isActive;
        private boolean isCompleted;
        private long remainingSeconds;
        private boolean needsReset;
        private BigDecimal currentDayProfit;
        private int currentDay;
        
        // Getters and Setters
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
        
        public long getRemainingSeconds() { return remainingSeconds; }
        public void setRemainingSeconds(long remainingSeconds) { this.remainingSeconds = remainingSeconds; }
        
        public boolean isNeedsReset() { return needsReset; }
        public void setNeedsReset(boolean needsReset) { this.needsReset = needsReset; }
        
        public BigDecimal getCurrentDayProfit() { return currentDayProfit; }
        public void setCurrentDayProfit(BigDecimal currentDayProfit) { this.currentDayProfit = currentDayProfit; }
        
        public int getCurrentDay() { return currentDay; }
        public void setCurrentDay(int currentDay) { this.currentDay = currentDay; }
    }
    
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
        private NavigationLinks navigationLinks;
        
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
        
        public NavigationLinks getNavigationLinks() { return navigationLinks; }
        public void setNavigationLinks(NavigationLinks navigationLinks) { this.navigationLinks = navigationLinks; }
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
    
    public static class NavigationLinks {
        private String profile = "/api/user/profile";
        private String deposit = "/api/transactions/deposit";
        private String withdraw = "/api/transactions/withdraw";
        private String withdrawalHistory = "/api/transactions/withdrawal-history";
        private String plans = "/api/plans";
        private String logout = "/api/auth/logout";
        
        // Getters and Setters
        public String getProfile() { return profile; }
        public void setProfile(String profile) { this.profile = profile; }
        
        public String getDeposit() { return deposit; }
        public void setDeposit(String deposit) { this.deposit = deposit; }
        
        public String getWithdraw() { return withdraw; }
        public void setWithdraw(String withdraw) { this.withdraw = withdraw; }
        
        public String getWithdrawalHistory() { return withdrawalHistory; }
        public void setWithdrawalHistory(String withdrawalHistory) { this.withdrawalHistory = withdrawalHistory; }
        
        public String getPlans() { return plans; }
        public void setPlans(String plans) { this.plans = plans; }
        
        public String getLogout() { return logout; }
        public void setLogout(String logout) { this.logout = logout; }
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
    
    public static class ReferralStatsResponse {
        private int totalDirectReferrals;
        private int totalSecondLevelReferrals;
        private BigDecimal totalReferralEarnings;
        private String referralLink;
        
        // Getters and Setters
        public int getTotalDirectReferrals() { return totalDirectReferrals; }
        public void setTotalDirectReferrals(int totalDirectReferrals) { this.totalDirectReferrals = totalDirectReferrals; }
        
        public int getTotalSecondLevelReferrals() { return totalSecondLevelReferrals; }
        public void setTotalSecondLevelReferrals(int totalSecondLevelReferrals) { this.totalSecondLevelReferrals = totalSecondLevelReferrals; }
        
        public BigDecimal getTotalReferralEarnings() { return totalReferralEarnings; }
        public void setTotalReferralEarnings(BigDecimal totalReferralEarnings) { this.totalReferralEarnings = totalReferralEarnings; }
        
        public String getReferralLink() { return referralLink; }
        public void setReferralLink(String referralLink) { this.referralLink = referralLink; }
    }
    
    public static class TeamStatsResponse {
        private int directReferrals;
        private int secondLevelReferrals;
        private int totalReferrals;
        private BigDecimal totalReferralEarnings;
        private String referralLink;
        private String referralCode;
        private List<ReferralDetail> recentReferrals;
        
        // Getters and Setters
        public int getDirectReferrals() { return directReferrals; }
        public void setDirectReferrals(int directReferrals) { this.directReferrals = directReferrals; }
        
        public int getSecondLevelReferrals() { return secondLevelReferrals; }
        public void setSecondLevelReferrals(int secondLevelReferrals) { this.secondLevelReferrals = secondLevelReferrals; }
        
        public int getTotalReferrals() { return totalReferrals; }
        public void setTotalReferrals(int totalReferrals) { this.totalReferrals = totalReferrals; }
        
        public BigDecimal getTotalReferralEarnings() { return totalReferralEarnings; }
        public void setTotalReferralEarnings(BigDecimal totalReferralEarnings) { this.totalReferralEarnings = totalReferralEarnings; }
        
        public String getReferralLink() { return referralLink; }
        public void setReferralLink(String referralLink) { this.referralLink = referralLink; }
        
        public String getReferralCode() { return referralCode; }
        public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
        
        public List<ReferralDetail> getRecentReferrals() { return recentReferrals; }
        public void setRecentReferrals(List<ReferralDetail> recentReferrals) { this.recentReferrals = recentReferrals; }
    }
    
    public static class ReferralDetail {
        private String username;
        private String phoneNumber;
        private String planName;
        private BigDecimal investmentAmount;
        private BigDecimal commissionEarned;
        private String level; // "DIRECT" or "GRAND"
        private LocalDateTime joinedAt;
        
        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public BigDecimal getInvestmentAmount() { return investmentAmount; }
        public void setInvestmentAmount(BigDecimal investmentAmount) { this.investmentAmount = investmentAmount; }
        
        public BigDecimal getCommissionEarned() { return commissionEarned; }
        public void setCommissionEarned(BigDecimal commissionEarned) { this.commissionEarned = commissionEarned; }
        
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    }
} 