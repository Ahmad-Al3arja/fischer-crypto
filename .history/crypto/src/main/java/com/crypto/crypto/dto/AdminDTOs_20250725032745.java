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
        private LocalDateTime subscriptionDate;
        private LocalDateTime createdAt;
        private int directReferrals;
        private int secondLevelReferrals;
        
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
    }
    
    public static class DepositListResponse {
        private List<DepositSummary> deposits;
        
        public DepositListResponse(List<DepositSummary> deposits) {
            this.deposits = deposits;
        }
        
        public List<DepositSummary> getDeposits() { return deposits; }
        public void setDeposits(List<DepositSummary> deposits) { this.deposits = deposits; }
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
    }
    
    public static class WithdrawalListResponse {
        private List<WithdrawalSummary> withdrawals;
        
        public WithdrawalListResponse(List<WithdrawalSummary> withdrawals) {
            this.withdrawals = withdrawals;
        }
        
        public List<WithdrawalSummary> getWithdrawals() { return withdrawals; }
        public void setWithdrawals(List<WithdrawalSummary> withdrawals) { this.withdrawals = withdrawals; }
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
    
    public static class WalletChangeActionRequest {
        private String adminNotes;
        
        // Getters and Setters
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }
    
    public static class WalletChangeSummary {
        private Long id;
        private String userName;
        private String userPhone;
        private String currentAddress;
        private String newAddress;
        private String reason;
        private String status;
        private LocalDateTime createdAt;
        private String adminNotes;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        
        public String getCurrentAddress() { return currentAddress; }
        public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
        
        public String getNewAddress() { return newAddress; }
        public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    }
    
    public static class WalletChangeListResponse {
        private List<WalletChangeSummary> requests;
        
        public WalletChangeListResponse(List<WalletChangeSummary> requests) {
            this.requests = requests;
        }
        
        public List<WalletChangeSummary> getRequests() { return requests; }
        public void setRequests(List<WalletChangeSummary> requests) { this.requests = requests; }
    }
    
    public static class AdminSettingsResponse {
        private boolean maintenanceMode;
        private String aboutContent;
        private String usdtWalletAddress;
        private Integer defaultUsageLimit;
        
        // Getters and Setters
        public boolean isMaintenanceMode() { return maintenanceMode; }
        public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
        
        public String getAboutContent() { return aboutContent; }
        public void setAboutContent(String aboutContent) { this.aboutContent = aboutContent; }
        
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
        
        public Integer getDefaultUsageLimit() { return defaultUsageLimit; }
        public void setDefaultUsageLimit(Integer defaultUsageLimit) { this.defaultUsageLimit = defaultUsageLimit; }
    }
} 