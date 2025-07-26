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
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private WithdrawalRateLimiter withdrawalRateLimiter;
    
    public TransactionDTOs.WithdrawalResponse createWithdrawal(TransactionDTOs.CreateWithdrawalRequest request) {
        User user = userService.getCurrentUser();
        return createWithdrawal(user, convertToWithdrawalRequest(request));
    }
    
    public TransactionDTOs.WithdrawalResponse createWithdrawal(User user, TransactionDTOs.WithdrawalRequest request) {
        // Rate limiting: max 3 withdrawals per 24 hours
        if (!withdrawalRateLimiter.canWithdraw(user.getId())) {
            throw new RuntimeException("Withdrawal rate limit exceeded. Max 3 withdrawals per 24 hours.");
        }
        // Validate withdrawal amount
        BigDecimal availableBalance = user.getWithdrawableBalance();
        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new RuntimeException("Insufficient withdrawable balance. Available: " + availableBalance);
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
            if (wallet == null || wallet.getUsdtAddress() == null || !wallet.getAddressSet()) {
                throw new RuntimeException("No wallet address found. Please save your wallet address first.");
            }
            walletAddress = wallet.getUsdtAddress();
        }
        
        // Validate USDT address format
        if (!isValidTRC20Address(walletAddress)) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format");
        }
        
        // Calculate fee (12% of withdrawal amount)
        BigDecimal fee = request.getAmount().multiply(BigDecimal.valueOf(0.12));
        BigDecimal netAmount = request.getAmount().subtract(fee);
        
        // Create withdrawal - save only the net amount (after 12% fee)
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(netAmount); // Save the net amount (after fee deduction)
        withdrawal.setFee(BigDecimal.ZERO); // Set fee to 0 since we're saving net amount
        withdrawal.setNetAmount(netAmount); // Net amount is the same as amount
        withdrawal.setWalletAddress(walletAddress);
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        
        withdrawalRepository.save(withdrawal);
        
        // Record withdrawal for rate limiting
        withdrawalRateLimiter.recordWithdrawal(user.getId());
        
        // Deduct from user balance immediately (pending withdrawal)
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
    
    public void approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Withdrawal is not pending");
        }
        
        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        withdrawal.setProcessedAt(java.time.LocalDateTime.now());
        withdrawalRepository.save(withdrawal);
        
        // Balance already deducted during creation
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
    
    private boolean isValidTRC20Address(String address) {
        // Enhanced TRC20 address validation
        return address != null && 
               address.length() == 34 && 
               address.startsWith("T") &&
               address.matches("^T[A-Za-z0-9]{33}$");
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
        summary.setUserName(withdrawal.getUser().getDisplayUsername());
        summary.setUserPhone(withdrawal.getUser().getPhoneNumber());
        summary.setAmount(withdrawal.getAmount());
        summary.setWalletAddress(withdrawal.getWalletAddress());
        summary.setStatus(withdrawal.getStatus().name());
        summary.setCreatedAt(withdrawal.getCreatedAt());
        summary.setRejectionNote(withdrawal.getRejectionNote());
        return summary;
    }
    
    private TransactionDTOs.WithdrawalRequest convertToWithdrawalRequest(TransactionDTOs.CreateWithdrawalRequest request) {
        TransactionDTOs.WithdrawalRequest withdrawalRequest = new TransactionDTOs.WithdrawalRequest();
        withdrawalRequest.setAmount(request.getAmount());
        withdrawalRequest.setWalletAddress(request.getWalletAddress());
        return withdrawalRequest;
    }
    
    public TransactionDTOs.WithdrawalListResponse getUserWithdrawals() {
        User user = userService.getCurrentUser();
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
        List<TransactionDTOs.WithdrawalResponse> withdrawalResponses = withdrawals.stream()
                .map(this::convertToWithdrawalResponse)
                .collect(Collectors.toList());
        return new TransactionDTOs.WithdrawalListResponse(withdrawalResponses);
    }
    
    public TransactionDTOs.WithdrawalResponse getWithdrawalById(Long withdrawalId) {
        User user = userService.getCurrentUser();
        Withdrawal withdrawal = withdrawalRepository.findByIdAndUser(withdrawalId, user)
                .orElseThrow(() -> new RuntimeException("Withdrawal not found"));
        return convertToWithdrawalResponse(withdrawal);
    }

    public long getPendingWithdrawalsCount() {
        return withdrawalRepository.countByStatus(WithdrawalStatus.PENDING);
    }

    public BigDecimal getTotalWithdrawn() {
        return withdrawalRepository.findByStatus(WithdrawalStatus.APPROVED)
                .stream()
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 