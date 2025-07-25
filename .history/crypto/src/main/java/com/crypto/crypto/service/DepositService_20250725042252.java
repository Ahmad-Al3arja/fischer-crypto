
// crypto/src/main/java/com/crypto/crypto/service/DepositService.java
// src/main/java/com/crypto/crypto/service/DepositService.java
package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.DepositRepository;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.PromoCodeRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepositService {
    
    @Autowired
    private DepositRepository depositRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    @Autowired
    private ReferralService referralService;
    
    @Autowired
    private PromoCodeService promoCodeService;
    
    @Autowired
    private UserService userService;
    
    @Value("${app.platform.usdt-wallet:TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE}")
    private String platformUsdtWallet;
    
    public TransactionDTOs.DepositResponse createDeposit(TransactionDTOs.CreateDepositRequest request) {
        // Get current user
        User user = userService.getCurrentUser();
        return createDeposit(user, convertToDepositRequest(request));
    }
    
    public TransactionDTOs.DepositResponse createDeposit(User user, TransactionDTOs.DepositRequest request) {
        // Validate plan exists
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        // Validate amount meets plan minimum
        if (request.getAmount().compareTo(plan.getPrice()) < 0) {
            throw new RuntimeException("Amount must be at least " + plan.getPrice() + " for this plan");
        }
        
        // Validate promo code if provided (but don't consume it yet)
        PromoCode promoCode = null;
        BigDecimal bonusAmount = BigDecimal.ZERO;
        
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            promoCode = promoCodeService.validatePromoCode(request.getPromoCode());
            bonusAmount = promoCode.getBonusValue();
            // Don't increment usage count here - wait for approval
        }
        
        // Create deposit
        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setPlan(plan);
        deposit.setAmount(request.getAmount());
        deposit.setPromoCode(promoCode);
        deposit.setBonusAmount(bonusAmount);
        deposit.setStatus(DepositStatus.PENDING);
        
        depositRepository.save(deposit);
        
        // Create response
        TransactionDTOs.DepositResponse response = new TransactionDTOs.DepositResponse();
        response.setMessage("Deposit request created successfully. Awaiting admin approval.");
        response.setTotalAmount(request.getAmount().add(bonusAmount));
        response.setBonusAmount(bonusAmount);
        response.setUsdtWalletAddress(platformUsdtWallet); // Add platform wallet to response
        return response;
    }
    
    public TransactionDTOs.DepositListResponse getUserDeposits() {
        User user = userService.getCurrentUser();
        List<Deposit> deposits = depositRepository.findByUser(user);
        List<TransactionDTOs.DepositResponse> depositResponses = deposits.stream()
                .map(this::convertToDepositResponse)
                .collect(Collectors.toList());
        return new TransactionDTOs.DepositListResponse(depositResponses);
    }
    
    public TransactionDTOs.DepositResponse getDepositById(Long depositId) {
        User user = userService.getCurrentUser();
        Deposit deposit = depositRepository.findByIdAndUser(depositId, user)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        return convertToDepositResponse(deposit);
    }
    
    private TransactionDTOs.DepositRequest convertToDepositRequest(TransactionDTOs.CreateDepositRequest request) {
        TransactionDTOs.DepositRequest depositRequest = new TransactionDTOs.DepositRequest();
        depositRequest.setAmount(request.getAmount());
        depositRequest.setPlanId(request.getPlanId());
        depositRequest.setPromoCode(request.getPromoCode());
        return depositRequest;
    }
    
    private TransactionDTOs.DepositResponse convertToDepositResponse(Deposit deposit) {
        TransactionDTOs.DepositResponse response = new TransactionDTOs.DepositResponse();
        response.setMessage("Deposit " + deposit.getStatus().name().toLowerCase());
        response.setTotalAmount(deposit.getAmount().add(deposit.getBonusAmount()));
        response.setBonusAmount(deposit.getBonusAmount());
        return response;
    }
    
    public void approveDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new RuntimeException("Deposit is not pending");
        }
        
        User user = deposit.getUser();
        Plan newPlan = deposit.getPlan();
        
        // Check if user can upgrade to this plan
        Plan currentPlan = user.getCurrentPlan();
        if (currentPlan != null && newPlan.getPlanLevel() <= currentPlan.getPlanLevel()) {
            throw new RuntimeException("Cannot downgrade or move to same level plan");
        }
        
        // Consume promo code now that deposit is approved
        if (deposit.getPromoCode() != null) {
            promoCodeService.consumePromoCode(deposit.getPromoCode());
        }
        
        // Update user balance and plan
        user.setTotalBalance(user.getTotalBalance().add(deposit.getAmount()));
        user.setCurrentPlan(newPlan);
        user.setSubscriptionDate(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        // Update deposit status
        deposit.setStatus(DepositStatus.APPROVED);
        depositRepository.save(deposit);
        
        // Process referral earnings
        referralService.processReferralEarnings(user, deposit.getAmount());
    }
    
    public void rejectDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new RuntimeException("Deposit is not pending");
        }
        
        deposit.setStatus(DepositStatus.REJECTED);
        depositRepository.save(deposit);
    }
    
    public TransactionDTOs.DepositHistoryResponse getDepositHistory(User user) {
        List<Deposit> deposits = depositRepository.findByUser(user);
        List<TransactionDTOs.DepositHistoryItem> historyItems = deposits.stream()
                .map(this::convertToDepositHistoryItem)
                .collect(Collectors.toList());
        return new TransactionDTOs.DepositHistoryResponse(historyItems);
    }
    
    public AdminDTOs.DepositListResponse getAllDeposits(String status) {
        List<Deposit> deposits;
        if (status != null && !status.isEmpty()) {
            deposits = depositRepository.findByStatus(DepositStatus.valueOf(status.toUpperCase()));
        } else {
            deposits = depositRepository.findAll();
        }
        
        List<AdminDTOs.DepositSummary> depositSummaries = deposits.stream()
                .map(this::convertToDepositSummary)
                .collect(Collectors.toList());
        
        return new AdminDTOs.DepositListResponse(depositSummaries);
    }
    
    private TransactionDTOs.DepositHistoryItem convertToDepositHistoryItem(Deposit deposit) {
        TransactionDTOs.DepositHistoryItem item = new TransactionDTOs.DepositHistoryItem();
        item.setId(deposit.getId());
        item.setAmount(deposit.getAmount());
        item.setBonusAmount(deposit.getBonusAmount());
        item.setPlanName(deposit.getPlan().getName());
        item.setStatus(deposit.getStatus().name());
        item.setCreatedAt(deposit.getCreatedAt());
        item.setApprovedAt(deposit.getStatus() == DepositStatus.APPROVED ? deposit.getUpdatedAt() : null);
        return item;
    }
    
    private AdminDTOs.DepositSummary convertToDepositSummary(Deposit deposit) {
        AdminDTOs.DepositSummary summary = new AdminDTOs.DepositSummary();
        summary.setId(deposit.getId());
        summary.setUserName(deposit.getUser().getDisplayUsername());
        summary.setUserPhone(deposit.getUser().getPhoneNumber());
        summary.setPlanName(deposit.getPlan().getName());
        summary.setAmount(deposit.getAmount());
        summary.setBonusAmount(deposit.getBonusAmount());
        summary.setStatus(deposit.getStatus().name());
        summary.setCreatedAt(deposit.getCreatedAt());
        return summary;
    }
}