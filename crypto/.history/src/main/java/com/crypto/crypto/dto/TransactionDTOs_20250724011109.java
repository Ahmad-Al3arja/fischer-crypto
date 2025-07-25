package com.crypto.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTOs {
    
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
} 