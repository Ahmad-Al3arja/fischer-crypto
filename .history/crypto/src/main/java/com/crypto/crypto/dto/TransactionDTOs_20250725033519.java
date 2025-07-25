package com.crypto.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTOs {
    
    public static class CreateDepositRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        @NotNull(message = "Plan ID is required")
        private Long planId;
        
        private String promoCode;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getPromoCode() { return promoCode; }
        public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    }
    
    public static class DepositListResponse {
        private List<DepositResponse> deposits;
        
        public DepositListResponse(List<DepositResponse> deposits) {
            this.deposits = deposits;
        }
        
        // Getters and Setters
        public List<DepositResponse> getDeposits() { return deposits; }
        public void setDeposits(List<DepositResponse> deposits) { this.deposits = deposits; }
    }
    
    public static class CreateWithdrawalRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        private String walletAddress; // Optional if user has saved wallet
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    }
    
    public static class WithdrawalListResponse {
        private List<WithdrawalResponse> withdrawals;
        
        public WithdrawalListResponse(List<WithdrawalResponse> withdrawals) {
            this.withdrawals = withdrawals;
        }
        
        // Getters and Setters
        public List<WithdrawalResponse> getWithdrawals() { return withdrawals; }
        public void setWithdrawals(List<WithdrawalResponse> withdrawals) { this.withdrawals = withdrawals; }
    }
    
    public static class SetWalletAddressRequest {
        @NotBlank(message = "USDT address is required")
        private String usdtAddress;
        
        // Getters and Setters
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
    }
    
    public static class DepositRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        @NotNull(message = "Plan ID is required")
        private Long planId;
        
        private String promoCode;
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Long getPlanId() { return planId; }
        public void setPlanId(Long planId) { this.planId = planId; }
        
        public String getPromoCode() { return promoCode; }
        public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    }
    
    public static class DepositResponse {
        private String message;
        private String usdtWalletAddress;
        private BigDecimal totalAmount; // including bonus
        private BigDecimal bonusAmount;
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getBonusAmount() { return bonusAmount; }
        public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }
    }
    
    public static class DepositInfoResponse {
        private String usdtWalletAddress;
        
        // Getters and Setters
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
    }
    
    public static class DepositHistoryResponse {
        private List<DepositHistoryItem> deposits;
        
        public DepositHistoryResponse(List<DepositHistoryItem> deposits) {
            this.deposits = deposits;
        }
        
        // Getters and Setters
        public List<DepositHistoryItem> getDeposits() { return deposits; }
        public void setDeposits(List<DepositHistoryItem> deposits) { this.deposits = deposits; }
    }
    
    public static class DepositHistoryItem {
        private Long id;
        private BigDecimal amount;
        private BigDecimal bonusAmount;
        private String planName;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public BigDecimal getBonusAmount() { return bonusAmount; }
        public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }
        
        public String getPlanName() { return planName; }
        public void setPlanName(String planName) { this.planName = planName; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getApprovedAt() { return approvedAt; }
        public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    }
    
    public static class WithdrawalRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        private String walletAddress; // Optional if user has saved wallet
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    }
    
    public static class WithdrawalResponse {
        private Long id;
        private BigDecimal amount;
        private BigDecimal fee;
        private BigDecimal netAmount;
        private String walletAddress;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
        private String rejectionNote;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }
        
        public BigDecimal getNetAmount() { return netAmount; }
        public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
        
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
        
        public String getRejectionNote() { return rejectionNote; }
        public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }
    }
    
    public static class WithdrawalHistoryResponse {
        private List<WithdrawalResponse> withdrawals;
        private BigDecimal availableBalance;
        
        public WithdrawalHistoryResponse(List<WithdrawalResponse> withdrawals, BigDecimal availableBalance) {
            this.withdrawals = withdrawals;
            this.availableBalance = availableBalance;
        }
        
        // Getters and Setters
        public List<WithdrawalResponse> getWithdrawals() { return withdrawals; }
        public void setWithdrawals(List<WithdrawalResponse> withdrawals) { this.withdrawals = withdrawals; }
        
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    }
    
    public static class WalletRequest {
        @NotBlank(message = "USDT address is required")
        private String usdtAddress;
        
        // Getters and Setters
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
    }
    
    public static class WalletResponse {
        private String usdtAddress;
        private boolean isLocked;
        private boolean addressSet;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and Setters
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
        
        public boolean isLocked() { return isLocked; }
        public void setLocked(boolean locked) { isLocked = locked; }
        
        public boolean isAddressSet() { return addressSet; }
        public void setAddressSet(boolean addressSet) { this.addressSet = addressSet; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    public static class WalletChangeRequest {
        @NotBlank(message = "New USDT address is required")
        private String newAddress;
        
        @NotBlank(message = "Reason for change is required")
        private String reason;
        
        // Getters and Setters
        public String getNewAddress() { return newAddress; }
        public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class WalletChangeResponse {
        private Long id;
        private String currentAddress;
        private String newAddress;
        private String reason;
        private String status;
        private String adminNotes;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getCurrentAddress() { return currentAddress; }
        public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
        
        public String getNewAddress() { return newAddress; }
        public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
    
    public static class WalletChangeHistoryResponse {
        private List<WalletChangeResponse> requests;
        
        public WalletChangeHistoryResponse(List<WalletChangeResponse> requests) {
            this.requests = requests;
        }
        
        // Getters and Setters
        public List<WalletChangeResponse> getRequests() { return requests; }
        public void setRequests(List<WalletChangeResponse> requests) { this.requests = requests; }
    }
    
    public static class WalletChangeListResponse {
        private List<WalletChangeResponse> requests;
        
        public WalletChangeListResponse(List<WalletChangeResponse> requests) {
            this.requests = requests;
        }
        
        public List<WalletChangeResponse> getRequests() { return requests; }
        public void setRequests(List<WalletChangeResponse> requests) { this.requests = requests; }
    }
} 