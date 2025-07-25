package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Withdrawal;
import com.crypto.crypto.entity.WithdrawalStatus;
import com.crypto.crypto.repository.WithdrawalRepository;
import com.crypto.crypto.repository.UserRepository;
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
    private UserRepository userRepository;
    
    public void createWithdrawal(User user, TransactionDTOs.WithdrawalRequest request) {
        // Validate withdrawal amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than 0");
        }
        
        // Check if user has sufficient balance
        BigDecimal availableBalance = user.getWithdrawableBalance();
        if (availableBalance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance for withdrawal");
        }
        
        // Deduct amount from user balance
        user.setTotalBalance(user.getTotalBalance().subtract(request.getAmount()));
        userRepository.save(user);
        
        // Create withdrawal request
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setWalletAddress(request.getWalletAddress());
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        
        withdrawalRepository.save(withdrawal);
    }
    
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal is not pending");
        }
        
        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
        
        // Here you would typically integrate with a payment processor
        // to actually send the USDT to the user's wallet
    }
    
    public void rejectWithdrawal(Long withdrawalId, String rejectionNote) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal is not pending");
        }
        
        // Refund the amount to user's balance
        User user = withdrawal.getUser();
        user.setTotalBalance(user.getTotalBalance().add(withdrawal.getAmount()));
        userRepository.save(user);
        
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setRejectionNote(rejectionNote);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
    }
    
    public TransactionDTOs.WithdrawalHistoryResponse getWithdrawalHistory(User user) {
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<TransactionDTOs.WithdrawalResponse> withdrawalResponses = withdrawals.stream()
                .map(this::convertToWithdrawalResponse)
                .collect(Collectors.toList());
        
        return new TransactionDTOs.WithdrawalHistoryResponse(withdrawalResponses, user.getWithdrawableBalance());
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