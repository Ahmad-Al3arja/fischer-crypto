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
} 