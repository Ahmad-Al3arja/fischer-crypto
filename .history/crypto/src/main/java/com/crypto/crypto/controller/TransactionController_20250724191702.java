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
    
    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@Valid @RequestBody TransactionDTOs.DepositRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
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
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/withdraw")
    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody TransactionDTOs.WithdrawalRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Validate minimum withdrawal amount
            if (request.getAmount().compareTo(java.math.BigDecimal.valueOf(10)) < 0) {
                throw new RuntimeException("Minimum withdrawal amount is $10");
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
    
    @PostMapping("/wallet/save")
    public ResponseEntity<?> saveWalletAddress(@Valid @RequestBody TransactionDTOs.WalletRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            
            // Validate USDT TRC20 address format (basic validation)
            if (!isValidTRC20Address(request.getUsdtAddress())) {
                throw new RuntimeException("Invalid USDT TRC20 wallet address format");
            }
            
            walletService.saveWalletAddress(currentUser, request.getUsdtAddress());
            return ResponseEntity.ok(new SuccessResponse("Wallet address saved successfully"));
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
    
    @PostMapping("/wallet/change-request")
    public ResponseEntity<?> createWalletChangeRequest(@Valid @RequestBody TransactionDTOs.WalletChangeRequest request) {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.WalletChangeResponse response = walletService.createWalletChangeRequest(currentUser, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/wallet/change-history")
    public ResponseEntity<?> getWalletChangeHistory() {
        try {
            User currentUser = userService.getCurrentUser();
            TransactionDTOs.WalletChangeHistoryResponse response = walletService.getWalletChangeHistory(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/balance")
    public ResponseEntity<?> getBalance() {
        try {
            User currentUser = userService.getCurrentUser();
            BalanceResponse response = new BalanceResponse();
            response.setTotalBalance(currentUser.getTotalBalance());
            response.setFrozenBalance(currentUser.getFrozenBalance());
            response.setWithdrawableBalance(currentUser.getWithdrawableBalance());
            response.setReferralEarnings(currentUser.getReferralEarnings());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private boolean isValidTRC20Address(String address) {
        // Basic TRC20 address validation (starts with T and is 34 characters)
        return address != null && 
               address.length() == 34 && 
               address.startsWith("T") &&
               address.matches("^T[A-Za-z0-9]{33}$");
    }
    
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private static class BalanceResponse {
        private java.math.BigDecimal totalBalance;
        private java.math.BigDecimal frozenBalance;
        private java.math.BigDecimal withdrawableBalance;
        private java.math.BigDecimal referralEarnings;
        
        // Getters and Setters
        public java.math.BigDecimal getTotalBalance() { return totalBalance; }
        public void setTotalBalance(java.math.BigDecimal totalBalance) { this.totalBalance = totalBalance; }
        
        public java.math.BigDecimal getFrozenBalance() { return frozenBalance; }
        public void setFrozenBalance(java.math.BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
        
        public java.math.BigDecimal getWithdrawableBalance() { return withdrawableBalance; }
        public void setWithdrawableBalance(java.math.BigDecimal withdrawableBalance) { this.withdrawableBalance = withdrawableBalance; }
        
        public java.math.BigDecimal getReferralEarnings() { return referralEarnings; }
        public void setReferralEarnings(java.math.BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
    }
}