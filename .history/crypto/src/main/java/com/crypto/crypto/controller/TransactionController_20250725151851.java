package com.crypto.crypto.controller;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.WalletService;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class TransactionController {
    @Autowired
    private DepositService depositService;
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private UserService userService;
    //Deposit endpoints
    @PostMapping("/deposits")
    public ResponseEntity<?> createDeposit(@Valid @RequestBody TransactionDTOs.CreateDepositRequest request) {
        try {
            TransactionDTOs.DepositResponse response = depositService.createDeposit(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/deposits")
    public ResponseEntity<?> getUserDeposits() {
        try {
            TransactionDTOs.DepositListResponse response = depositService.getUserDeposits();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/deposits/{depositId}")
    public ResponseEntity<?> getDepositById(@PathVariable Long depositId) {
        try {
            TransactionDTOs.DepositResponse response = depositService.getDepositById(depositId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/deposits/history")
    public ResponseEntity<?> getDepositHistory() {
        try {
            User user = userService.getCurrentUser();
            TransactionDTOs.DepositHistoryResponse response = depositService.getDepositHistory(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    //Withdrawal endpoints
    @PostMapping("/withdrawals")
    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody TransactionDTOs.CreateWithdrawalRequest request) {
        try {
            // Sanitize and validate amount
            if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid withdrawal amount"));
            }
            // Round amount to 2 decimal places
            request.setAmount(request.getAmount().setScale(2, java.math.RoundingMode.HALF_UP));
            // Check minimum withdrawal
            if (request.getAmount().compareTo(new java.math.BigDecimal("10.00")) < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Minimum withdrawal amount is $10"));
            }
            // Check maximum withdrawal
            if (request.getAmount().compareTo(new java.math.BigDecimal("50000.00")) > 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Maximum withdrawal amount is $50,000"));
            }
            TransactionDTOs.WithdrawalResponse response = withdrawalService.createWithdrawal(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/withdrawals")
    public ResponseEntity<?> getUserWithdrawals() {
        try {
            TransactionDTOs.WithdrawalListResponse response = withdrawalService.getUserWithdrawals();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/withdrawals/{withdrawalId}")
    public ResponseEntity<?> getWithdrawalById(@PathVariable Long withdrawalId) {
        try {
            TransactionDTOs.WithdrawalResponse response = withdrawalService.getWithdrawalById(withdrawalId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/withdrawals/history")
    public ResponseEntity<?> getWithdrawalHistory() {
        try {
            User user = userService.getCurrentUser();
            TransactionDTOs.WithdrawalHistoryResponse response = withdrawalService.getWithdrawalHistory(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    //Wallet endpoints
    @GetMapping("/wallet")
    public ResponseEntity<?> getUserWallet() {
        try {
            TransactionDTOs.WalletResponse response = walletService.getUserWallet();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @PostMapping("/wallet/address")
    public ResponseEntity<?> setWalletAddress(@Valid @RequestBody TransactionDTOs.SetWalletAddressRequest request) {
        try {
            walletService.setWalletAddress(request);
            TransactionDTOs.WalletResponse response = walletService.getUserWallet();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @PostMapping("/wallet/change-request")
    public ResponseEntity<?> requestWalletChange(@Valid @RequestBody TransactionDTOs.WalletChangeRequest request) {
        try {
            TransactionDTOs.WalletChangeResponse response = walletService.requestWalletChange(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/wallet/change-requests")
    public ResponseEntity<?> getUserWalletChangeRequests() {
        try {
            TransactionDTOs.WalletChangeListResponse response = walletService.getUserWalletChangeRequests();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    private static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
} 