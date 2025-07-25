// Enhanced TransactionController.java
package com.crypto.crypto.controller;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.WalletService;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('USER')")
public class TransactionController {
    
    @Autowired
    private DepositService depositService;
    
    @Autowired
    private WithdrawalService withdrawalService;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserService userService;
    
    @Value("${app.platform.usdt-wallet}")
    private String platformUsdtWallet;
    
    // ============ DEPOSIT OPERATIONS ============
    
    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@Valid @RequestBody TransactionDTOs.DepositRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Additional validation
            if (request.getAmount().compareTo(BigDecimal.valueOf(1)) < 0) {
                throw new RuntimeException("Minimum deposit amount is $1");
            }
            
            if (request.getAmount().compareTo(BigDecimal.valueOf(100000)) > 0) {
                throw new RuntimeException("Maximum deposit amount is $100,000");
            }
            
            TransactionDTOs.DepositResponse response = depositService.createDeposit(currentUser, request);
            
            // Add platform wallet address to response
            response.setUsdtWalletAddress(platformUsdtWallet);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/deposit-info")
    public ResponseEntity<?> getDepositInfo() {
        try {
            TransactionDTOs.DepositInfoResponse response = new TransactionDTOs.DepositInfoResponse();
            response.setUsdtWalletAddress(platformUsdtWallet);
            response.setMinAmount(BigDecimal.valueOf(1));
            response.setMaxAmount(BigDecimal.valueOf(100000));
            response.setProcessingTime("1-24 hours");
            response.setInstructions("Send USDT (TRC20) to the above address and wait for confirmation. " +
                    "Make sure to send only USDT TRC20 tokens to avoid loss of funds.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/deposit-history")
    public ResponseEntity<?> getDepositHistory() {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.DepositHistoryResponse response = depositService.getDepositHistory(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ WITHDRAWAL OPERATIONS ============
    
    @PostMapping("/withdraw")
    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody TransactionDTOs.WithdrawalRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Enhanced validation
            if (request.getAmount().compareTo(BigDecimal.valueOf(10)) < 0) {
                throw new RuntimeException("Minimum withdrawal amount is $10");
            }
            
            if (request.getAmount().compareTo(BigDecimal.valueOf(50000)) > 0) {
                throw new RuntimeException("Maximum withdrawal amount is $50,000");
            }
            
            // Check daily withdrawal limit (this would need implementation in service)
            BigDecimal dailyWithdrawn = withdrawalService.getTodayWithdrawnAmount(currentUser);
            BigDecimal dailyLimit = BigDecimal.valueOf(10000); // $10,000 daily limit
            
            if (dailyWithdrawn.add(request.getAmount()).compareTo(dailyLimit) > 0) {
                throw new RuntimeException("Daily withdrawal limit exceeded. You can withdraw $" + 
                        dailyLimit.subtract(dailyWithdrawn) + " more today.");
            }
            
            TransactionDTOs.WithdrawalResponse response = withdrawalService.createWithdrawal(currentUser, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/withdrawal-history")
    public ResponseEntity<?> getWithdrawalHistory() {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.WithdrawalHistoryResponse response = withdrawalService.getWithdrawalHistory(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/withdrawal-info")
    public ResponseEntity<?> getWithdrawalInfo() {
        try {
            User currentUser = userService.getCurrentUser();
            
            TransactionDTOs.WithdrawalInfoResponse response = new TransactionDTOs.WithdrawalInfoResponse();
            response.setMinAmount(BigDecimal.valueOf(10));
            response.setMaxAmount(BigDecimal.valueOf(50000));
            response.setDailyLimit(BigDecimal.valueOf(10000));
            response.setFeePercentage(BigDecimal.valueOf(2.0));
            response.setProcessingTime("1-48 hours");
            response.setAvailableBalance(currentUser.getWithdrawableBalance());
            
            // Get today's withdrawn amount
            BigDecimal todayWithdrawn = withdrawalService.getTodayWithdrawnAmount(currentUser);
            response.setTodayWithdrawn(todayWithdrawn);
            response.setRemainingDailyLimit(response.getDailyLimit().subtract(todayWithdrawn));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ WALLET OPERATIONS ============
    
    @PostMapping("/wallet/save")
    public ResponseEntity<?> saveWalletAddress(@Valid @RequestBody TransactionDTOs.WalletRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Enhanced validation
            if (!isValidTRC20Address(request.getUsdtAddress())) {
                throw new RuntimeException("Invalid USDT TRC20 wallet address format. " +
                        "Address must be 34 characters long and start with 'T'.");
            }
            
            walletService.saveWalletAddress(currentUser, request.getUsdtAddress());
            return ResponseEntity.ok(new SuccessResponse("Wallet address saved successfully. " +
                    "Note: This address is now locked and cannot be changed without admin approval."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/wallet/change-request")
    public ResponseEntity<?> requestWalletChange(@Valid @RequestBody TransactionDTOs.WalletChangeRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Validate new address
            if (!isValidTRC20Address(request.getNewUsdtAddress())) {
                throw new RuntimeException("Invalid USDT TRC20 wallet address format. " +
                        "Address must be 34 characters long and start with 'T'.");
            }
            
            // Validate reason
            if (request.getReason() == null || request.getReason().trim().length() < 10) {
                throw new RuntimeException("Please provide a detailed reason for changing your wallet address (minimum 10 characters).");
            }
            
            walletService.requestWalletChange(currentUser, request.getNewUsdtAddress(), request.getReason());
            return ResponseEntity.ok(new SuccessResponse("Wallet change request submitted successfully. " +
                    "An admin will review your request within 24-48 hours."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/wallet")
    public ResponseEntity<?> getWalletInfo() {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.WalletResponse response = walletService.getWalletInfo(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ BALANCE OPERATIONS ============
    
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            User currentUser = userService.getCurrentUser();
            BalanceResponse response = new BalanceResponse();
            response.setTotalBalance(currentUser.getTotalBalance());
            response.setFrozenBalance(currentUser.getFrozenBalance());
            response.setWithdrawableBalance(currentUser.getWithdrawableBalance());
            response.setReferralEarnings(currentUser.getReferralEarnings());
            
            // Add additional balance information
            response.setProfitBalance(currentUser.getTotalBalance().subtract(currentUser.getFrozenBalance()));
            response.setLastUpdated(currentUser.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/balance/history")
    public ResponseEntity<?> getBalanceHistory(
            @RequestParam(required = false, defaultValue = "30") int days) {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.BalanceHistoryResponse response = 
                    withdrawalService.getBalanceHistory(currentUser, days);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ TRANSACTION SUMMARY ============
    
    @GetMapping("/summary")
    public ResponseEntity<?> getTransactionSummary() {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.TransactionSummaryResponse response = 
                    withdrawalService.getTransactionSummary(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ HELPER METHODS ============
    
    private boolean isValidTRC20Address(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        
        address = address.trim();
        
        // Enhanced TRC20 address validation
        return address.length() == 34 && 
               address.startsWith("T") &&
               address.matches("^T[A-Za-z0-9]{33}$") &&
               !address.contains(" ") &&
               // Additional validation: should have mixed case
               !address.toLowerCase().equals(address) && 
               !address.toUpperCase().equals(address);
    }
    
    // ============ RESPONSE CLASSES ============
    
    private static class ErrorResponse {
        private String message;
        private String status = "error";
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
    
    private static class SuccessResponse {
        private String message;
        private String status = "success";
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
    
    private static class BalanceResponse {
        private BigDecimal totalBalance;
        private BigDecimal frozenBalance;
        private BigDecimal withdrawableBalance;
        private BigDecimal referralEarnings;
        private BigDecimal profitBalance;
        private java.time.LocalDateTime lastUpdated;
        
        // Getters and Setters
        public BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public BigDecimal getFrozenBalance() { return frozenBalance; }
        public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
        
        public BigDecimal getWithdrawableBalance() { return withdrawableBalance; }
        public void setWithdrawableBalance(BigDecimal withdrawableBalance) { this.withdrawableBalance = withdrawableBalance; }
        
        public BigDecimal getReferralEarnings() { return referralEarnings; }
        public void setReferralEarnings(BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
        
        public BigDecimal getProfitBalance() { return profitBalance; }
        public void setProfitBalance(BigDecimal profitBalance) { this.profitBalance = profitBalance; }
        
        public java.time.LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(java.time.LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}