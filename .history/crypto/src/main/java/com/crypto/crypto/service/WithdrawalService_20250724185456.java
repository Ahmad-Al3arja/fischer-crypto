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
import java.time.LocalDate;
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

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UpgradeService upgradeService;

    @Autowired
    private NotificationService notificationService;

    // Constants
    private static final BigDecimal WITHDRAWAL_FEE_PERCENTAGE = new BigDecimal("0.02"); // 2%
    private static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("10.00");
    private static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("50000.00");
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000.00");

    public TransactionDTOs.WithdrawalResponse createWithdrawal(User user, TransactionDTOs.WithdrawalRequest request) {
        // Validate withdrawal amount
        if (request.getAmount().compareTo(MIN_WITHDRAWAL) < 0) {
            throw new RuntimeException("Minimum withdrawal amount is $" + MIN_WITHDRAWAL);
        }

        if (request.getAmount().compareTo(MAX_WITHDRAWAL) > 0) {
            throw new RuntimeException("Maximum withdrawal amount is $" + MAX_WITHDRAWAL);
        }

        BigDecimal availableBalance = user.getWithdrawableBalance();
        if (request.getAmount().compareTo(availableBalance) > 0) {
            throw new RuntimeException("Insufficient withdrawable balance. Available: $" + availableBalance);
        }

        // Check daily withdrawal limit
        BigDecimal todayWithdrawn = getTodayWithdrawnAmount(user);
        BigDecimal totalWithdrawalToday = todayWithdrawn.add(request.getAmount());
        
        if (totalWithdrawalToday.compareTo(DAILY_LIMIT) > 0) {
            BigDecimal remaining = DAILY_LIMIT.subtract(todayWithdrawn);
            throw new RuntimeException("Daily withdrawal limit exceeded. You can withdraw $" + 
                                     remaining + " more today (Daily limit: $" + DAILY_LIMIT + ")");
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

        // Validate USDT address format
        if (!isValidTRC20Address(walletAddress)) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format");
        }

        // Calculate fee and net amount
        BigDecimal fee = request.getAmount().multiply(WITHDRAWAL_FEE_PERCENTAGE);
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

        // Deduct from user balance immediately (pending withdrawal)
        user.setTotalBalance(user.getTotalBalance().subtract(request.getAmount()));
        userRepository.save(user);

        // Recalculate upgrade progress
        upgradeService.recalculateUpgradeProgress(user);

        // Create response
        TransactionDTOs.WithdrawalResponse response = convertToWithdrawalResponse(withdrawal);
        response.setEstimatedProcessingTime("1-48 hours");
        response.setTrackingNumber("WD" + withdrawal.getId() + System.currentTimeMillis() % 10000);

        return response;
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

        // Notify user
        notificationService.notifyUserWithdrawalProcessed(
            withdrawal.getUser(), 
            withdrawal.getAmount().toString(), 
            "APPROVED"
        );

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
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        // Notify user
        notificationService.notifyUserWithdrawalProcessed(
            withdrawal.getUser(), 
            withdrawal.getAmount().toString(), 
            "REJECTED"
        );
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

    public BigDecimal getTodayWithdrawnAmount(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return withdrawalRepository.findByUserAndCreatedAtBetweenAndStatus(
                user, startOfDay, endOfDay, WithdrawalStatus.APPROVED)
                .stream()
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public TransactionDTOs.BalanceHistoryResponse getBalanceHistory(User user, int days) {
        // This would typically query a transaction log table
        // For now, we'll create a simple mock implementation
        List<TransactionDTOs.BalanceHistoryResponse.BalanceHistoryItem> history = 
                generateMockBalanceHistory(user, days);

        return new TransactionDTOs.BalanceHistoryResponse(history, days + "days");
    }

    public TransactionDTOs.TransactionSummaryResponse getTransactionSummary(User user) {
        TransactionDTOs.TransactionSummaryResponse response = 
                new TransactionDTOs.TransactionSummaryResponse();

        // Calculate totals from user's transactions
        // This would typically involve querying multiple transaction tables
        
        // Mock implementation - in real scenario, you'd query actual transaction logs
        response.setTotalDeposited(user.getFrozenBalance()); // Simplified
        response.setTotalWithdrawn(getTotalWithdrawnAmount(user));
        response.setTotalProfits(user.getTotalBalance().subtract(user.getFrozenBalance()));
        response.setTotalReferralEarnings(user.getReferralEarnings());
        
        List<Withdrawal> allWithdrawals = withdrawalRepository.findByUserOrderByCreatedAtDesc(user);
        response.setTotalTransactions(allWithdrawals.size());
        
        if (!allWithdrawals.isEmpty()) {
            response.setFirstTransactionDate(allWithdrawals.get(allWithdrawals.size() - 1).getCreatedAt());
            response.setLastTransactionDate(allWithdrawals.get(0).getCreatedAt());
        }

        // Calculate net gain and ROI
        BigDecimal netGain = response.getTotalProfits().add(response.getTotalReferralEarnings());
        response.setNetGain(netGain);
        
        if (user.getFrozenBalance().compareTo(BigDecimal.ZERO) > 0) {
            double roi = netGain.divide(user.getFrozenBalance(), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            response.setRoi(roi);
        } else {
            response.setRoi(0.0);
        }

        return response;
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

    private BigDecimal getTotalWithdrawnAmount(User user) {
        return withdrawalRepository.findByUserAndStatus(user, WithdrawalStatus.APPROVED)
                .stream()
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<TransactionDTOs.BalanceHistoryResponse.BalanceHistoryItem> generateMockBalanceHistory(User user, int days) {
        List<TransactionDTOs.BalanceHistoryResponse.BalanceHistoryItem> history = new ArrayList<>();
        
        BigDecimal currentBalance = user.getTotalBalance();
        
        for (int i = 0; i < days; i++) {
            TransactionDTOs.BalanceHistoryResponse.BalanceHistoryItem item = 
                    new TransactionDTOs.BalanceHistoryResponse.BalanceHistoryItem();
            
            LocalDateTime date = LocalDateTime.now().minusDays(days - i - 1);
            item.setDate(date);
            item.setBalance(currentBalance);
            item.setTransactionType("DAILY_PROFIT");
            item.setChange(new BigDecimal("5.00")); // Mock daily change
            
            history.add(item);
        }
        
        return history;
    }
}