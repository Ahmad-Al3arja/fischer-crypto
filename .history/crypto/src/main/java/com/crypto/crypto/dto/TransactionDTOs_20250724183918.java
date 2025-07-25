// Enhanced TransactionDTOs.java
package com.crypto.crypto.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTOs {
    
    // ============ DEPOSIT DTOs ============
    
    public static class DepositRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Minimum deposit amount is $1")
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
        private BigDecimal totalAmount;
        private BigDecimal bonusAmount;
        private String status;
        private LocalDateTime estimatedProcessingTime;
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getBonusAmount() { return bonusAmount; }
        public void setBonusAmount(BigDecimal bonusAmount) { this.bonusAmount = bonusAmount; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getEstimatedProcessingTime() { return estimatedProcessingTime; }
        public void setEstimatedProcessingTime(LocalDateTime estimatedProcessingTime) { 
            this.estimatedProcessingTime = estimatedProcessingTime; 
        }
    }
    
    public static class DepositInfoResponse {
        private String usdtWalletAddress;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private String processingTime;
        private String instructions;
        private List<String> supportedNetworks;
        private String warningMessage;
        
        public DepositInfoResponse() {
            this.supportedNetworks = List.of("TRC20");
            this.warningMessage = "Only send USDT TRC20 tokens to this address. " +
                    "Sending other tokens or using wrong network will result in permanent loss.";
        }
        
        // Getters and Setters
        public String getUsdtWalletAddress() { return usdtWalletAddress; }
        public void setUsdtWalletAddress(String usdtWalletAddress) { this.usdtWalletAddress = usdtWalletAddress; }
        
        public BigDecimal getMinAmount() { return minAmount; }
        public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
        
        public BigDecimal getMaxAmount() { return maxAmount; }
        public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
        
        public String getProcessingTime() { return processingTime; }
        public void setProcessingTime(String processingTime) { this.processingTime = processingTime; }
        
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        
        public List<String> getSupportedNetworks() { return supportedNetworks; }
        public void setSupportedNetworks(List<String> supportedNetworks) { this.supportedNetworks = supportedNetworks; }
        
        public String getWarningMessage() { return warningMessage; }
        public void setWarningMessage(String warningMessage) { this.warningMessage = warningMessage; }
    }
    
    // ============ WITHDRAWAL DTOs ============
    
    public static class WithdrawalRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "10.00", message = "Minimum withdrawal amount is $10")
        private BigDecimal amount;
        
        private String walletAddress; // Optional if user has saved wallet
        
        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getWalletAddress() { return walletAddress; }
        public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }
    }
    
    public static class WithdrawalInfoResponse {
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private BigDecimal dailyLimit;
        private BigDecimal feePercentage;
        private String processingTime;
        private BigDecimal availableBalance;
        private BigDecimal todayWithdrawn;
        private BigDecimal remainingDailyLimit;
        
        // Getters and Setters
        public BigDecimal getMinAmount() { return minAmount; }
        public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
        
        public BigDecimal getMaxAmount() { return maxAmount; }
        public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
        
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
        
        public BigDecimal getFeePercentage() { return feePercentage; }
        public void setFeePercentage(BigDecimal feePercentage) { this.feePercentage = feePercentage; }
        
        public String getProcessingTime() { return processingTime; }
        public void setProcessingTime(String processingTime) { this.processingTime = processingTime; }
        
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
        
        public BigDecimal getTodayWithdrawn() { return todayWithdrawn; }
        public void setTodayWithdrawn(BigDecimal todayWithdrawn) { this.todayWithdrawn = todayWithdrawn; }
        
        public BigDecimal getRemainingDailyLimit() { return remainingDailyLimit; }
        public void setRemainingDailyLimit(BigDecimal remainingDailyLimit) { this.remainingDailyLimit = remainingDailyLimit; }
    }
    
    // ============ WALLET DTOs ============
    
    public static class WalletRequest {
        @NotBlank(message = "USDT address is required")
        @Size(min = 34, max = 34, message = "USDT TRC20 address must be exactly 34 characters")
        private String usdtAddress;
        
        // Getters and Setters
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
    }
    
    public static class WalletChangeRequest {
        @NotBlank(message = "New USDT address is required")
        @Size(min = 34, max = 34, message = "USDT TRC20 address must be exactly 34 characters")
        private String newUsdtAddress;
        
        @NotBlank(message = "Reason is required")
        @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
        private String reason;
        
        // Getters and Setters
        public String getNewUsdtAddress() { return newUsdtAddress; }
        public void setNewUsdtAddress(String newUsdtAddress) { this.newUsdtAddress = newUsdtAddress; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class WalletResponse {
        private String usdtAddress;
        private boolean isLocked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean hasPendingChangeRequest;
        private String pendingNewAddress;
        private LocalDateTime pendingRequestDate;
        private String changeRequestStatus;
        
        // Getters and Setters
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
        
        public boolean isLocked() { return isLocked; }
        public void setLocked(boolean locked) { isLocked = locked; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public boolean isHasPendingChangeRequest() { return hasPendingChangeRequest; }
        public void setHasPendingChangeRequest(boolean hasPendingChangeRequest) { 
            this.hasPendingChangeRequest = hasPendingChangeRequest; 
        }
        
        public String getPendingNewAddress() { return pendingNewAddress; }
        public void setPendingNewAddress(String pendingNewAddress) { this.pendingNewAddress = pendingNewAddress; }
        
        public LocalDateTime getPendingRequestDate() { return pendingRequestDate; }
        public void setPendingRequestDate(LocalDateTime pendingRequestDate) { this.pendingRequestDate = pendingRequestDate; }
        
        public String getChangeRequestStatus() { return changeRequestStatus; }
        public void setChangeRequestStatus(String changeRequestStatus) { this.changeRequestStatus = changeRequestStatus; }
    }
    
    // ============ ENHANCED EXISTING DTOs ============
    
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
        private String estimatedProcessingTime;
        private String trackingNumber;
        
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
        
        public String getEstimatedProcessingTime() { return estimatedProcessingTime; }
        public void setEstimatedProcessingTime(String estimatedProcessingTime) { 
            this.estimatedProcessingTime = estimatedProcessingTime; 
        }
        
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }
    
    public static class DepositHistoryResponse {
        private List<DepositHistoryItem> deposits;
        private Integer totalDeposits;
        private BigDecimal totalAmount;
        private BigDecimal totalBonusAmount;
        
        public DepositHistoryResponse(List<DepositHistoryItem> deposits) {
            this.deposits = deposits;
            this.totalDeposits = deposits.size();
            this.totalAmount = deposits.stream()
                    .map(DepositHistoryItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.totalBonusAmount = deposits.stream()
                    .map(DepositHistoryItem::getBonusAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // Getters and Setters
        public List<DepositHistoryItem> getDeposits() { return deposits; }
        public void setDeposits(List<DepositHistoryItem> deposits) { this.deposits = deposits; }
        
        public Integer getTotalDeposits() { return totalDeposits; }
        public void setTotalDeposits(Integer totalDeposits) { this.totalDeposits = totalDeposits; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public BigDecimal getTotalBonusAmount() { return totalBonusAmount; }
        public void setTotalBonusAmount(BigDecimal totalBonusAmount) { this.totalBonusAmount = totalBonusAmount; }
    }
    
    public static class DepositHistoryItem {
        private Long id;
        private BigDecimal amount;
        private BigDecimal bonusAmount;
        private String planName;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime approvedAt;
        private String promoCode;
        
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
        
        public String getPromoCode() { return promoCode; }
        public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    }
    
    public static class WithdrawalHistoryResponse {
        private List<WithdrawalResponse> withdrawals;
        private BigDecimal availableBalance;
        private Integer totalWithdrawals;
        private BigDecimal totalWithdrawn;
        private BigDecimal totalFees;
        
        public WithdrawalHistoryResponse(List<WithdrawalResponse> withdrawals, BigDecimal availableBalance) {
            this.withdrawals = withdrawals;
            this.availableBalance = availableBalance;
            this.totalWithdrawals = withdrawals.size();
            this.totalWithdrawn = withdrawals.stream()
                    .filter(w -> "APPROVED".equals(w.getStatus()))
                    .map(WithdrawalResponse::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            this.totalFees = withdrawals.stream()
                    .filter(w -> "APPROVED".equals(w.getStatus()))
                    .map(WithdrawalResponse::getFee)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // Getters and Setters
        public List<WithdrawalResponse> getWithdrawals() { return withdrawals; }
        public void setWithdrawals(List<WithdrawalResponse> withdrawals) { this.withdrawals = withdrawals; }
        
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
        
        public Integer getTotalWithdrawals() { return totalWithdrawals; }
        public void setTotalWithdrawals(Integer totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }
        
        public BigDecimal getTotalWithdrawn() { return totalWithdrawn; }
        public void setTotalWithdrawn(BigDecimal totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }
        
        public BigDecimal getTotalFees() { return totalFees; }
        public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }
    }
    
    // ============ NEW ANALYTICS DTOs ============
    
    public static class BalanceHistoryResponse {
        private List<BalanceHistoryItem> balanceHistory;
        private BigDecimal currentBalance;
        private BigDecimal highestBalance;
        private BigDecimal lowestBalance;
        private String period;
        
        public BalanceHistoryResponse(List<BalanceHistoryItem> balanceHistory, String period) {
            this.balanceHistory = balanceHistory;
            this.period = period;
            this.currentBalance = balanceHistory.isEmpty() ? BigDecimal.ZERO : 
                    balanceHistory.get(balanceHistory.size() - 1).getBalance();
            this.highestBalance = balanceHistory.stream()
                    .map(BalanceHistoryItem::getBalance)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            this.lowestBalance = balanceHistory.stream()
                    .map(BalanceHistoryItem::getBalance)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }
        
        // Getters and Setters
        public List<BalanceHistoryItem> getBalanceHistory() { return balanceHistory; }
        public void setBalanceHistory(List<BalanceHistoryItem> balanceHistory) { this.balanceHistory = balanceHistory; }
        
        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
        
        public BigDecimal getHighestBalance() { return highestBalance; }
        public void setHighestBalance(BigDecimal highestBalance) { this.highestBalance = highestBalance; }
        
        public BigDecimal getLowestBalance() { return lowestBalance; }
        public void setLowestBalance(BigDecimal lowestBalance) { this.lowestBalance = lowestBalance; }
        
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        
        public static class BalanceHistoryItem {
            private LocalDateTime date;
            private BigDecimal balance;
            private String transactionType;
            private BigDecimal change;
            
            // Getters and Setters
            public LocalDateTime getDate() { return date; }
            public void setDate(LocalDateTime date) { this.date = date; }
            
            public BigDecimal getBalance() { return balance; }
            public void setBalance(BigDecimal balance) { this.balance = balance; }
            
            public String getTransactionType() { return transactionType; }
            public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
            
            public BigDecimal getChange() { return change; }
            public void setChange(BigDecimal change) { this.change = change; }
        }
    }
    
    public static class TransactionSummaryResponse {
        private BigDecimal totalDeposited;
        private BigDecimal totalWithdrawn;
        private BigDecimal totalProfits;
        private BigDecimal totalReferralEarnings;
        private Integer totalTransactions;
        private LocalDateTime firstTransactionDate;
        private LocalDateTime lastTransactionDate;
        private BigDecimal netGain;
        private Double roi; // Return on Investment percentage
        
        // Getters and Setters
        public BigDecimal getTotalDeposited() { return totalDeposited; }
        public void setTotalDeposited(BigDecimal totalDeposited) { this.totalDeposited = totalDeposited; }
        
        public BigDecimal getTotalWithdrawn() { return totalWithdrawn; }
        public void setTotalWithdrawn(BigDecimal totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }
        
        public BigDecimal getTotalProfits() { return totalProfits; }
        public void setTotalProfits(BigDecimal totalProfits) { this.totalProfits = totalProfits; }
        
        public BigDecimal getTotalReferralEarnings() { return totalReferralEarnings; }
        public void setTotalReferralEarnings(BigDecimal totalReferralEarnings) { 
            this.totalReferralEarnings = totalReferralEarnings; 
        }
        
        public Integer getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
        
        public LocalDateTime getFirstTransactionDate() { return firstTransactionDate; }
        public void setFirstTransactionDate(LocalDateTime firstTransactionDate) { 
            this.firstTransactionDate = firstTransactionDate; 
        }
        
        public LocalDateTime getLastTransactionDate() { return lastTransactionDate; }
        public void setLastTransactionDate(LocalDateTime lastTransactionDate) { 
            this.lastTransactionDate = lastTransactionDate; 
        }
        
        public BigDecimal getNetGain() { return netGain; }
        public void setNetGain(BigDecimal netGain) { this.netGain = netGain; }
        
        public Double getRoi() { return roi; }
        public void setRoi(Double roi) { this.roi = roi; }
    }
}