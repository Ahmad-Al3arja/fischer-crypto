package com.crypto.crypto.controller;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins="*")
public class TransactionController {
    @Autowired
    private DepositService depositService;
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private WalletService walletService;
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
    //Withdrawal endpoints
    @PostMapping("/withdrawals")
    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody TransactionDTOs.CreateWithdrawalRequest request) {
        try {
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