package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.WithdrawalRepository;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WithdrawalService {
    
    @Autowired
    private WithdrawalRepository withdrawalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private UpgradeService upgradeService;
    
    public TransactionDTOs.WithdrawalResponse createWithdrawal(User user, TransactionDTOs.WithdrawalRequest request) {
        // Validate withdrawal amount
        BigDecimal availableBalance = user.getWithdrawableBalance();
        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new RuntimeException("Insufficient withdrawable balance");
        }
        
        // Validate minimum withdrawal amount
        if (request.getAmount().compareTo(BigDecimal.valueOf(10)) < 0) {
            throw new RuntimeException("Minimum withdrawal amount is $10");
        }
        
        // Get wallet address
        String walletAddress = request.getWalletAddress();
        if (walletAddress == null || walletAddress.isEmpty()) {
            // Use saved wallet address
            Wallet wallet = walletRepository.findByUser(user).orElse(null);
            if (wallet == null || wallet.getUsdtAddress() == null) {
                throw new RuntimeException("No wallet address found. Please save your wallet address first.");
            }
            walletAddress = wallet.getUsdtAddress();
        }
        
        // Calculate fee (2% of withdrawal amount)
        BigDecimal fee = request.getAmount().multiply(BigDecimal.valueOf(0.02));
        BigDecimal netAmount = request.getAmount().subtract(fee);
        
        // Create withdrawal
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setFee(fee);
        withdrawal.setNetAmount(netAmount);
        withdrawal.setWalletAddress(walletAddress);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        
        withdrawalRepository.save(withdrawal);
        
        // Deduct from user balance
        user.setTotalBalance(user.getTotalBalance().subtract(request.getAmount()));
        userRepository.save(user);
        
        // Recalculate upgrade progress
        upgradeService.recalculateUpgradeProgress(user);
        
        // Create response
        TransactionDTOs.WithdrawalResponse response = new TransactionDTOs.WithdrawalResponse();
        response.setId(withdrawal.getId());
        response.setAmount(withdrawal.getAmount());
        response.setFee(withdrawal.getFee());
        response.setNetAmount(withdrawal.getNetAmount());
        response.setWalletAddress(withdrawal.getWalletAddress());
        response.setStatus(withdrawal.getStatus().name());
        response.setCreatedAt(withdrawal.getCreatedAt());
        response.setProcessedAt(withdrawal.getProcessedAt());
        response.setRejectionNote(withdrawal.getRejectionNote());
        
        return response;
    }
    
    public TransactionDTOs.WithdrawalHistoryResponse getWithdrawalHistory(User user) {
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<TransactionDTOs.WithdrawalResponse> withdrawalResponses = withdrawals.stream()
                .map(this::convertToWithdrawalResponse)
                .collect(Collectors.toList());
        
        return new TransactionDTOs.WithdrawalHistoryResponse(withdrawalResponses, user.getWithdrawableBalance());
    }
    
    public AdminDTOs.WithdrawalListResponse getAllWithdrawals(String status) {
        List<Withdrawal> withdrawals;
        if (status != null && !status.isEmpty()) {
            withdrawals = withdrawalRepository.findByStatus(WithdrawalStatus.valueOf(status.toUpperCase()));
        } else {
            withdrawals = withdrawalRepository.findAll();
        }
        
        List<AdminDTOs.WithdrawalSummary> withdrawalSummaries = withdrawals.stream()
                .map(this::convertToWithdrawalSummary)
                .collect(Collectors.toList());
        
        return new AdminDTOs.WithdrawalListResponse(withdrawalSummaries);
    }
    
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal is not pending");
        }
        
        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        withdrawal.setProcessedAt(java.time.LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }
    
    public void rejectWithdrawal(Long withdrawalId, String rejectionNote) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal is not pending");
        }
        
        // Refund the amount to user
        User user = withdrawal.getUser();
        user.setTotalBalance(user.getTotalBalance().add(withdrawal.getAmount()));
        userRepository.save(user);
        
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setRejectionNote(rejectionNote);
        withdrawal.setProcessedAt(java.time.LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }
    
    private TransactionDTOs.WithdrawalResponse convertToWithdrawalResponse(Withdrawal withdrawal) {
        TransactionDTOs.WithdrawalResponse response = new TransactionDTOs.WithdrawalResponse();
        response.setId(withdrawal.getId());
        response.setAmount(withdrawal.getAmount());
        response.setFee(withdrawal.getFee());
        response.setNetAmount(withdrawal.getNetAmount());
        response.setWalletAddress(withdrawal.getWalletAddress());
        response.setStatus(withdrawal.getStatus().name());
        response.setCreatedAt(withdrawal.getCreatedAt());
        response.setProcessedAt(withdrawal.getProcessedAt());
        response.setRejectionNote(withdrawal.getRejectionNote());
        return response;
    }
    
    private AdminDTOs.WithdrawalSummary convertToWithdrawalSummary(Withdrawal withdrawal) {
        AdminDTOs.WithdrawalSummary summary = new AdminDTOs.WithdrawalSummary();
        summary.setId(withdrawal.getId());
        summary.setUserName(withdrawal.getUser().getUsername());
        summary.setUserPhone(withdrawal.getUser().getPhoneNumber());
        summary.setAmount(withdrawal.getAmount());
        summary.setWalletAddress(withdrawal.getWalletAddress());
        summary.setStatus(withdrawal.getStatus().name());
        summary.setCreatedAt(withdrawal.getCreatedAt());
        summary.setRejectionNote(withdrawal.getRejectionNote());
        return summary;
    }
} 