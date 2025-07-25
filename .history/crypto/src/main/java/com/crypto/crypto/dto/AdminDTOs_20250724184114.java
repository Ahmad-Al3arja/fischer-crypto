// Enhanced AdminDTOs.java
package com.crypto.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDTOs {
    
    // ============ EXISTING USER DTOs (Enhanced) ============
    
    public static class UserListResponse {
        private List<UserSummary> users;
        private Integer totalUsers;
        private Integer activeUsers;
        private Integer suspendedUsers;
        private String message;
        
        public UserListResponse(List<UserSummary> users) {
            this.users = users;
            this.totalUsers = users.size();
            this.activeUsers = (int) users.stream().filter(u -> "ACTIVE".equals(u.getStatus())).count();
            this.suspendedUsers = (int) users.stream().filter(u -> "SUSPENDED".equals(u.getStatus())).count();
            this.message = "Users retrieved successfully";
        }
        
        // Getters and Setters
        public List<UserSummary> getUsers() { return users; }
        public void setUsers(List<UserSummary> users) { this.users = users; }
        
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public Integer getActiveUsers() { return activeUsers; }
        public void setActiveUsers(Integer activeUsers) { this.activeUsers = activeUsers; }
        
        public Integer getSuspendedUsers() { return suspendedUsers; }
        public void setSuspendedUsers(Integer suspendedUsers) { this.suspendedUsers = suspendedUsers; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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
        private Integer directReferrals;
        private BigDecimal referralEarnings;
        private LocalDateTime lastLoginAt;
        
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
        
        public Integer getDirectReferrals() { return directReferrals; }
        public void setDirectReferrals(Integer directReferrals) { this.directReferrals = directReferrals; }
        
        public BigDecimal getReferralEarnings() { return referralEarnings; }
        public void setReferralEarnings(BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
        
        public LocalDateTime getLastLoginAt() { return lastLoginAt; }
        public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
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
        private LocalDateTime subscriptionDate;
        private LocalDateTime createdAt;
        private int directReferrals;
        private int secondLevelReferrals;
        private String referrerUsername;
        private String referrerPhone;
        private WalletInfo walletInfo;
        private List<RecentTransaction> recentTransactions;
        
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
        
        public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
        public void setSubscriptionDate(LocalDateTime subscriptionDate) { this.subscriptionDate = subscriptionDate; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public int getDirectReferrals() { return directReferrals; }
        public void setDirectReferrals(int directReferrals) { this.directReferrals = directReferrals; }
        
        public int getSecondLevelReferrals() { return secondLevelReferrals; }
        public void setSecondLevelReferrals(int secondLevelReferrals) { this.secondLevelReferrals = secondLevelReferrals; }
        
        public String getReferrerUsername() { return referrerUsername; }
        public void setReferrerUsername(String referrerUsername) { this.referrerUsername = referrerUsername; }
        
        public String getReferrerPhone() { return referrerPhone; }
        public void setReferrerPhone(String referrerPhone) { this.referrerPhone = referrerPhone; }
        
        public WalletInfo getWalletInfo() { return walletInfo; }
        public void setWalletInfo(WalletInfo walletInfo) { this.walletInfo = walletInfo; }
        
        public List<RecentTransaction> getRecentTransactions() { return recentTransactions; }
        public void setRecentTransactions(List<RecentTransaction> recentTransactions) { 
            this.recentTransactions = recentTransactions; 
        }
        
        public static class WalletInfo {
            private String usdtAddress;
            private boolean isLocked;
            private LocalDateTime createdAt;
            
            // Getters and Setters
            public String getUsdtAddress() { return usdtAddress; }
            public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
            
            public boolean isLocked() { return isLocked; }
            public void setLocked(boolean locked) { isLocked = locked; }
            
            public LocalDateTime getCreatedAt() { return createdAt; }
            public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        }
        
        public static class RecentTransaction {
            private String type;
            private BigDecimal amount;
            private String status;
            private LocalDateTime date;
            
            // Getters and Setters
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            
            public BigDecimal getAmount() { return amount; }
            public void setAmount(BigDecimal amount) { this.amount = amount; }
            
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
            
            public LocalDateTime getDate() { return date; }
            public void setDate(LocalDateTime date) { this.date = date; }
        }
    }
    
    public static class UserSearchResponse {
        private List<UserSummary> users;
        private String query;
        private Integer totalResults;
        
        public UserSearchResponse(List<UserSummary> users, String query) {
            this.users = users;
            this.query = query;
            this.totalResults = users.size();
        }
        
        // Getters and Setters
        public List<UserSummary> getUsers() { return users; }
        public void setUsers(List<UserSummary> users) { this.users = users; }
        
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        
        public Integer getTotalResults() { return totalResults; }
        public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }
    }
    
    // ============ WALLET CHANGE REQUEST DTOs ============
    
    public static class WalletChangeRequestListResponse {
        private List<WalletChangeRequestSummary> requests;
        private Integer totalPendingRequests;
        
        public WalletChangeRequestListResponse(List<WalletChangeRequestSummary> requests) {
            this.requests = requests;
            this.totalPendingRequests = requests.size();
        }
        
        // Getters and Setters
        public List<WalletChangeRequestSummary> getRequests() { return requests; }
        public void setRequests(List<WalletChangeRequestSummary> requests) { this.requests = requests; }
        
        public Integer getTotalPendingRequests() { return totalPendingRequests; }
        public void setTotalPendingRequests(Integer totalPendingRequests) { 
            this.totalPendingRequests = totalPendingRequests; 
        }
    }
    
    public static class WalletChangeRequestSummary {
        private Long id;
        private String username;
        private String phoneNumber;
        private String currentAddress;
        private String newAddress;
        private String reason;
        private String status;
        private LocalDateTime requestedAt;
        private LocalDateTime processedAt;
        private String processedByUsername;
        private String rejectionReason;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getCurrentAddress() { return currentAddress; }
        public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
        
        public String getNewAddress() { return newAddress; }
        public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getRequestedAt() { return requestedAt; }
        public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public String getProcessedByUsername() { return processedByUsername; }
        public void setProcessedByUsername(String processedByUsername) { this.processedByUsername = processedByUsername; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
    
    // ============ REFERRAL MANAGEMENT DTOs ============
    
    public static class ReferralUsageListResponse {
        private List<ReferralUsageSummary> referralUsages;
        private Integer totalUsers;
        private Double averageUsagePercentage;
        
        public ReferralUsageListResponse(List<ReferralUsageSummary> referralUsages) {
            this.referralUsages = referralUsages;
            this.totalUsers = referralUsages.size();
            this.averageUsagePercentage = referralUsages.stream()
                    .mapToDouble(ReferralUsageSummary::getUsagePercentage)
                    .average()
                    .orElse(0.0);
        }
        
        // Getters and Setters
        public List<ReferralUsageSummary> getReferralUsages() { return referralUsages; }
        public void setReferralUsages(List<ReferralUsageSummary> referralUsages) { 
            this.referralUsages = referralUsages; 
        }
        
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public Double getAverageUsagePercentage() { return averageUsagePercentage; }
        public void setAverageUsagePercentage(Double averageUsagePercentage) { 
            this.averageUsagePercentage = averageUsagePercentage; 
        }
    }
    
    public static class ReferralUsageSummary {
        private Long userId;
        private String username;
        private String fullName;
        private Integer usageCount;
        private Integer usageLimit;
        private Boolean isActive;
        private Integer remainingReferrals;
        private Double usagePercentage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
        
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Integer getRemainingReferrals() { return remainingReferrals; }
        public void setRemainingReferrals(Integer remainingReferrals) { this.remainingReferrals = remainingReferrals; }
        
        public Double getUsagePercentage() { return usagePercentage; }
        public void setUsagePercentage(Double usagePercentage) { this.usagePercentage = usagePercentage; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    // ============ PLAN MANAGEMENT DTOs ============
    
    public static class PlansStatsResponse {
        private List<PlanStatsItem> planStats;
        private Integer totalPlans;
        private Integer activePlans;
        private BigDecimal totalInvestment;
        private Integer totalUsers;
        
        public PlansStatsResponse(List<PlanStatsItem> planStats) {
            this.planStats = planStats;
            this.totalPlans = planStats.size();
            this.activePlans = (int) planStats.stream().filter(PlanStatsItem::getIsActive).count();
            this.totalInvestment = planStats.stream()
                    .map(PlanStatsItem::getTotalInvestment)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.totalUsers = planStats.stream()
                    .mapToInt(PlanStatsItem::getTotalUsers)
                    .sum();
        }
        
        // Getters and Setters
        public List<PlanStatsItem> getPlanStats() { return planStats; }
        public void setPlanStats(List<PlanStatsItem> planStats) { this.planStats = planStats; }
        
        public Integer getTotalPlans() { return totalPlans; }
        public void setTotalPlans(Integer totalPlans) { this.totalPlans = totalPlans; }
        
        public Integer getActivePlans() { return activePlans; }
        public void setActivePlans(Integer activePlans) { this.activePlans = activePlans; }
        
        public BigDecimal getTotalInvestment() { return totalInvestment; }
        public void setTotalInvestment(BigDecimal totalInvestment) { this.totalInvestment = totalInvestment; }
        
        public Integer getTotalUsers() { return totalUsers; }
        public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
        
        public static class PlanStatsItem {
            private Long planId;
            private String planName;
            private BigDecimal price;
            private Integer planLevel;
            private Boolean isActive;
            private Integer totalUsers;
            private BigDecimal totalInvestment;
            private BigDecimal averageUserBalance;
            private LocalDateTime createdAt;
            
            // Getters and Setters
            public Long getPlanId() { return planId; }
            public void setPlanId(Long planId) { this.planId = planId; }
            
            public String getPlanName() { return planName; }
            public void setPlanName(String planName) { this.planName = planName; }
            
            public BigDecimal getPrice() { return price; }
            public void setPrice(BigDecimal price) { this.price = price; }
            
            public Integer getPlanLevel() { return planLevel; }
            public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
            
            public Boolean getIsActive() { return isActive; }
            public void setIsActive(Boolean isActive) { this.isActive = isActive; }
            
            public Integer getTotalUsers() { return totalUsers; }
            public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
            
            public BigDecimal getTotalInvestment() { return totalInvestment; }
            public void setTotalInvestment(BigDecimal totalInvestment) { this.totalInvestment = totalInvestment; }
            
            public BigDecimal getAverageUserBalance() { return averageUserBalance; }
            public void setAverageUserBalance(BigDecimal averageUserBalance) { 
                this.averageUserBalance = averageUserBalance; 
            }
            
            public LocalDateTime getCreatedAt() { return createdAt; }
            public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        }
    }
    
    // ============ ADMIN DASHBOARD & ANALYTICS DTOs ============
    
    public static class DashboardResponse {
        private DashboardStats stats;
        private List<RecentActivity> recentActivities;
        private List<PendingAction> pendingActions;
        private SystemHealth systemHealth;
        
        // Getters and Setters
        public DashboardStats getStats() { return stats; }
        public void setStats(DashboardStats stats) { this.stats = stats; }
        
        public List<RecentActivity> getRecentActivities() { return recentActivities; }
        public void setRecentActivities(List<RecentActivity> recentActivities) { 
            this.recentActivities = recentActivities; 
        }
        
        public List<PendingAction> getPendingActions() { return pendingActions; }
        public void setPendingActions(List<PendingAction> pendingActions) { this.pendingActions = pendingActions; }
        
        public SystemHealth getSystemHealth() { return systemHealth; }
        public void setSystemHealth(SystemHealth systemHealth) { this.systemHealth = systemHealth; }
        
        public static class DashboardStats {
            private Integer totalUsers;
            private Integer activeUsers;
            private BigDecimal totalBalance;
            private BigDecimal totalDeposits;
            private BigDecimal totalWithdrawals;
            private Integer pendingDeposits;
            private Integer pendingWithdrawals;
            private Integer pendingWalletChanges;
            
            // Getters and Setters
            public Integer getTotalUsers() { return totalUsers; }
            public void setTotalUsers(Integer totalUsers) { this.totalUsers = totalUsers; }
            
            public Integer getActiveUsers() { return activeUsers; }
            public void setActiveUsers(Integer activeUsers) { this.activeUsers = activeUsers; }
            
            public BigDecimal getTotalBalance() { return totalBalance; }
            public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
            
            public BigDecimal getTotalDeposits() { return totalDeposits; }
            public void setTotalDeposits(BigDecimal totalDeposits) { this.totalDeposits = totalDeposits; }
            
            public BigDecimal getTotalWithdrawals() { return totalWithdrawals; }
            public void setTotalWithdrawals(BigDecimal totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }
            
            public Integer getPendingDeposits() { return pendingDeposits; }
            public void setPendingDeposits(Integer pendingDeposits) { this.pendingDeposits = pendingDeposits; }
            
            public Integer getPendingWithdrawals() { return pendingWithdrawals; }
            public void setPendingWithdrawals(Integer pendingWithdrawals) { this.pendingWithdrawals = pendingWithdrawals; }
            
            public Integer getPendingWalletChanges() { return pendingWalletChanges; }
            public void setPendingWalletChanges(Integer pendingWalletChanges) { 
                this.pendingWalletChanges = pendingWalletChanges; 
            }
        }
        
        public static class RecentActivity {
            private String type;
            private String description;
            private String username;
            private BigDecimal amount;
            private LocalDateTime timestamp;
            
            // Getters and Setters
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            
            public String getUsername() { return username; }
            public void setUsername(String username) { this.username = username; }
            
            public BigDecimal getAmount() { return amount; }
            public void setAmount(BigDecimal amount) { this.amount = amount; }
            
            public LocalDateTime getTimestamp() { return timestamp; }
            public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        }
        
        public static class PendingAction {
            private String type;
            private String description;
            private Integer count;
            private String priority;
            private String actionUrl;
            
            // Getters and Setters
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            
            public Integer getCount() { return count; }
            public void setCount(Integer count) { this.count = count; }
            
            public String getPriority() { return priority; }
            public void setPriority(String priority) { this.priority = priority; }
            
            public String getActionUrl() { return actionUrl; }
            public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        }
        
        public static class SystemHealth {
            private String status;
            private Double cpuUsage;
            private Double memoryUsage;
            private Long databaseConnections;
            private LocalDateTime lastBackup;
            private Boolean maintenanceMode;
            
            // Getters and Setters
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
            
            public Double getCpuUsage() { return cpuUsage; }
            public void setCpuUsage(Double cpuUsage) { this.cpuUsage = cpuUsage; }
            
            public Double getMemoryUsage() { return memoryUsage; }
            public void setMemoryUsage(Double memoryUsage) { this.memoryUsage = memoryUsage; }
            
            public Long getDatabaseConnections() { return databaseConnections; }
            public void setDatabaseConnections(Long databaseConnections) { this.databaseConnections = databaseConnections; }
            
            public LocalDateTime getLastBackup() { return lastBackup; }
            public void setLastBackup(LocalDateTime lastBackup) { this.lastBackup = lastBackup; }
            
            public Boolean getMaintenanceMode() { return maintenanceMode; }
            public void setMaintenanceMode(Boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
        }
    }
    
    public static class AnalyticsResponse {
        private String period;
        private String type;
        private List<AnalyticsDataPoint> dataPoints;
        private AnalyticsSummary summary;
        
        // Getters and Setters
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public List<AnalyticsDataPoint> getDataPoints() { return dataPoints; }
        public void setDataPoints(List<AnalyticsDataPoint> dataPoints) { this.dataPoints = dataPoints; }
        
        public AnalyticsSummary getSummary() { return summary; }
        public void setSummary(AnalyticsSummary summary) { this.summary = summary; }
        
        public static class AnalyticsDataPoint {
            private LocalDateTime date;
            private BigDecimal value;
            private Integer count;
            private String label;
            
            // Getters and Setters
            public LocalDateTime getDate() { return date; }
            public void setDate(LocalDateTime date) { this.date = date; }
            
            public BigDecimal getValue() { return value; }
            public void setValue(BigDecimal value) { this.value = value; }
            
            public Integer getCount() { return count; }
            public void setCount(Integer count) { this.count = count; }
            
            public String getLabel() { return label; }
            public void setLabel(String label) { this.label = label; }
        }
        
        public static class AnalyticsSummary {
            private BigDecimal totalValue;
            private Integer totalCount;
            private BigDecimal averageValue;
            private BigDecimal growth;
            private Double growthPercentage;
            
            // Getters and Setters
            public BigDecimal getTotalValue() { return totalValue; }
            public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
            
            public Integer getTotalCount() { return totalCount; }
            public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
            
            public BigDecimal getAverageValue() { return averageValue; }
            public void setAverageValue(BigDecimal averageValue) { this.averageValue = averageValue; }
            
            public BigDecimal getGrowth() { return growth; }
            public void setGrowth(BigDecimal growth) { this.growth = growth; }
            
            public Double getGrowthPercentage() { return growthPercentage; }
            public void setGrowthPercentage(Double growthPercentage) { this.growthPercentage = growthPercentage; }
        }
    }
    
    // ============ ENHANCED EXISTING DTOs ============
    
    public static class DepositListResponse {
        private List<DepositSummary> deposits;
        private Integer totalDeposits;
        private BigDecimal totalAmount;
        private Integer pendingCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        
        public DepositListResponse(List<DepositSummary> deposits) {
            this.deposits = deposits;
            this.totalDeposits = deposits.size();
            this.totalAmount = deposits.stream()
                    .map(DepositSummary::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.pendingCount = (int) deposits.stream().filter(d -> "PENDING".equals(d.getStatus())).count();
            this.approvedCount = (int) deposits.stream().filter(d -> "APPROVED".equals(d.getStatus())).count();
            this.rejectedCount = (int) deposits.stream().filter(d -> "REJECTED".equals(d.getStatus())).count();
        }
        
        // Getters and Setters
        public List<DepositSummary> getDeposits() { return deposits; }
        public void setDeposits(List<DepositSummary> deposits) { this.deposits = deposits; }
        
        public Integer getTotalDeposits() { return totalDeposits; }
        public void setTotalDeposits(Integer totalDeposits) { this.totalDeposits = totalDeposits; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        
        public Integer getApprovedCount() { return approvedCount; }
        public void setApprovedCount(Integer approvedCount) { this.approvedCount = approvedCount; }
        
        public Integer getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(Integer rejectedCount) { this.rejectedCount = rejectedCount; }
    }
    
    public static class DepositSummary {
        private Long id;
        private String userName;
        private String userPhone;
        private String planName;
        private BigDecimal amount;
        private BigDecimal bonusAmount;
        private String status;
        private LocalDateTime createdAt;
        private String promoCode;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public BigDecimal getBonusAmount() { return bonusAmount; }
        public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getPromoCode() { return promoCode; }
        public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    }
    
    public static class WithdrawalListResponse {
        private List<WithdrawalSummary> withdrawals;
        private Integer totalWithdrawals;
        private BigDecimal totalAmount;
        private Integer pendingCount;
        private Integer approvedCount;
        private Integer rejectedCount;
        
        public WithdrawalListResponse(List<WithdrawalSummary> withdrawals) {
            this.withdrawals = withdrawals;
            this.totalWithdrawals = withdrawals.size();
            this.totalAmount = withdrawals.stream()
                    .map(WithdrawalSummary::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.pendingCount = (int) withdrawals.stream().filter(w -> "PENDING".equals(w.getStatus())).count();
            this.approvedCount = (int) withdrawals.stream().filter(w -> "APPROVED".equals(w.getStatus())).count();
            this.rejectedCount = (int) withdrawals.stream().filter(w -> "REJECTED".equals(w.getStatus())).count();
        }
        
        // Getters and Setters
        public List<WithdrawalSummary> getWithdrawals() { return withdrawals; }
        public void setWithdrawals(List<WithdrawalSummary> withdrawals) { this.withdrawals = withdrawals; }
        
        public Integer getTotalWithdrawals() { return totalWithdrawals; }
        public void setTotalWithdrawals(Integer totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        
        public Integer getApprovedCount() { return approvedCount; }
        public void setApprovedCount(Integer approvedCount) { this.approvedCount = approvedCount; }
        
        public Integer getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(Integer rejectedCount) { this.rejectedCount = rejectedCount; }
    }
    
    public static class WithdrawalSummary {
        private Long id;
        private String userName;
        private String userPhone;
        private BigDecimal amount;
        private String walletAddress;
        private String status;
        private LocalDateTime createdAt;
        private String rejectionNote;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getRejectionNote() { return rejectionNote; }
        public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }
    }
    
    // ============ EXISTING PROMO CODE & SETTINGS DTOs (Keep as is) ============
    
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
    
    public static class PromoCodeResponse {
        private Long id;
        private String code;
        private BigDecimal bonusValue;
        private Integer usageLimit;
        private Integer usedCount;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public BigDecimal getBonusValue() { return bonusValue; }
        public void setBonusValue(BigDecimal bonusValue) { this.bonusValue = bonusValue; }
        
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        
        public Integer getUsedCount() { return usedCount; }
        public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }
    
    public static class PromoCodeListResponse {
        private List<PromoCodeResponse> promoCodes;
        
        public PromoCodeListResponse(List<PromoCodeResponse> promoCodes) {
            this.promoCodes = promoCodes;
        }
        
        public List<PromoCodeResponse> getPromoCodes() { return promoCodes; }
        public void setPromoCodes(List<PromoCodeResponse> promoCodes) { this.promoCodes = promoCodes; }
    }
    
    public static class AdminSettingsResponse {
        private boolean maintenanceMode;
        private String aboutContent;
        private String usdtWalletAddress;
        
        // Getters and Setters
        public boolean isMaintenanceMode() { return maintenanceMode; }
        public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
        
        public String getAboutContent() { return aboutContent; }
        public void setAboutContent(String aboutContent) { this.aboutContent = aboutContent; }
        
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
    }
}