package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Wallet;
import com.crypto.crypto.entity.Withdrawal;
import com.crypto.crypto.entity.WithdrawalStatus;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.repository.WalletRepository;
import com.crypto.crypto.repository.WithdrawalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WithdrawalService {
    
    @Autowired
    private WithdrawalRepository withdrawalRepository;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UpgradeService upgradeService;
    
    public void createWithdrawal(User user, TransactionDTOs.WithdrawalRequest request) {
        BigDecimal withdrawableBalance = user.getWithdrawableBalance();
        
        if (request.getAmount().compareTo(withdrawableBalance) > 0) {
            throw new RuntimeException("Insufficient withdrawable balance");
        }
        
        String walletAddress = request.getWalletAddress();
        
        // If no wallet address provided, use saved wallet
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            Wallet wallet = walletRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("No wallet address found. Please provide a wallet address."));
            walletAddress = wallet.getUsdtAddress();
        }
        
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setWalletAddress(walletAddress);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        
        withdrawalRepository.save(withdrawal);
        
        // Temporarily freeze the withdrawal amount
        user.setTotalBalance(user.getTotalBalance().subtract(request.getAmount()));
        userRepository.save(user);
        
        // Recalculate upgrade progress after withdrawal
        upgradeService.recalculateUpgradeProgress(user);
    }
    
    public TransactionDTOs.WithdrawalHistoryResponse getWithdrawalHistory(User user) {
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<TransactionDTOs.WithdrawalResponse> withdrawalResponses = withdrawals.stream()
                .map(this::convertToWithdrawalResponse)
                .collect(Collectors.toList());
        
        return new TransactionDTOs.WithdrawalHistoryResponse(
                withdrawalResponses, 
                user.getWithdrawableBalance()
        );
    }
    
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal already processed");
        }
        
        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }
    
    public void rejectWithdrawal(Long withdrawalId, String rejectionNote) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal already processed");
        }
        
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setRejectionNote(rejectionNote);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
        
        // Return money to user balance
        User user = withdrawal.getUser();
        user.setTotalBalance(user.getTotalBalance().add(withdrawal.getAmount()));
        userRepository.save(user);
    }
    
    private TransactionDTOs.WithdrawalResponse convertToWithdrawalResponse(Withdrawal withdrawal) {
        TransactionDTOs.WithdrawalResponse response = new TransactionDTOs.WithdrawalResponse();
        response.setId(withdrawal.getId());
        response.setAmount(withdrawal.getAmount());
        response.setWalletAddress(withdrawal.getWalletAddress());
        response.setStatus(withdrawal.getStatus().name());
        response.setCreatedAt(withdrawal.getCreatedAt());
        response.setProcessedAt(withdrawal.getProcessedAt());
        response.setRejectionNote(withdrawal.getRejectionNote());
        return response;
    }
} 