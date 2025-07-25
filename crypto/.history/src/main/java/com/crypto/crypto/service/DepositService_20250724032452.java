package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.DepositRepository;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.PromoCodeRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    public TransactionDTOs.DepositResponse createDeposit(User user, TransactionDTOs.DepositRequest request) {
        // Validate plan exists
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        // Validate promo code if provided
        PromoCode promoCode = null;
        BigDecimal bonusAmount = BigDecimal.ZERO;
        
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            promoCode = promoCodeRepository.findByCode(request.getPromoCode())
                    .orElseThrow(() -> new RuntimeException("Invalid promo code"));
            
            if (!promoCode.isValid()) {
                throw new RuntimeException("Promo code is expired or usage limit reached");
            }
            
            bonusAmount = promoCode.getBonusValue();
            
            // Increment usage count
            promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
            promoCodeRepository.save(promoCode);
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
        
        // Process referral commissions if this is user's first deposit
        if (user.getCurrentPlan() == null) {
            referralService.processReferralCommissions(user, deposit);
        }
        
        // Create response
        TransactionDTOs.DepositResponse response = new TransactionDTOs.DepositResponse();
        response.setMessage("Deposit request created successfully. Awaiting admin approval.");
        response.setTotalAmount(request.getAmount().add(bonusAmount));
        response.setBonusAmount(bonusAmount);
        
        return response;
    }
    
    public TransactionDTOs.DepositHistoryResponse getDepositHistory(User user) {
        List<Deposit> deposits = depositRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<TransactionDTOs.DepositHistoryItem> depositItems = deposits.stream()
                .map(this::convertToDepositHistoryItem)
                .collect(Collectors.toList());
        
        return new TransactionDTOs.DepositHistoryResponse(depositItems);
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
    
    public void approveDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new RuntimeException("Deposit is not pending");
        }
        
        // Update deposit status
        deposit.setStatus(DepositStatus.APPROVED);
        deposit.setApprovedAt(java.time.LocalDateTime.now());
        depositRepository.save(deposit);
        
        // Update user balance and plan
        User user = deposit.getUser();
        BigDecimal totalAmount = deposit.getAmount().add(deposit.getBonusAmount());
        user.setTotalBalance(user.getTotalBalance().add(totalAmount));
        user.setCurrentPlan(deposit.getPlan());
        user.setSubscriptionDate(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
    
    public void rejectDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        deposit.setStatus(DepositStatus.REJECTED);
        depositRepository.save(deposit);
    }
    
    private TransactionDTOs.DepositHistoryItem convertToDepositHistoryItem(Deposit deposit) {
        TransactionDTOs.DepositHistoryItem item = new TransactionDTOs.DepositHistoryItem();
        item.setId(deposit.getId());
        item.setAmount(deposit.getAmount());
        item.setBonusAmount(deposit.getBonusAmount());
        item.setPlanName(deposit.getPlan() != null ? deposit.getPlan().getName() : "N/A");
        item.setStatus(deposit.getStatus().name());
        item.setCreatedAt(deposit.getCreatedAt());
        item.setApprovedAt(deposit.getApprovedAt());
        return item;
    }
    
    private AdminDTOs.DepositSummary convertToDepositSummary(Deposit deposit) {
        AdminDTOs.DepositSummary summary = new AdminDTOs.DepositSummary();
        summary.setId(deposit.getId());
        summary.setUserName(deposit.getUser().getUsername());
        summary.setUserPhone(deposit.getUser().getPhoneNumber());
        summary.setPlanName(deposit.getPlan() != null ? deposit.getPlan().getName() : "N/A");
        summary.setAmount(deposit.getAmount());
        summary.setBonusAmount(deposit.getBonusAmount());
        summary.setStatus(deposit.getStatus().name());
        summary.setCreatedAt(deposit.getCreatedAt());
        return summary;
    }
} 