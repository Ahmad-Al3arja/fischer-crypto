package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class DepositService {
    
    @Autowired
    private DepositRepository depositRepository;
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReferralService referralService;
    
    @Value("${app.platform.usdt-wallet}")
    private String usdtWalletAddress;
    
    public TransactionDTOs.DepositResponse createDeposit(User user, TransactionDTOs.DepositRequest request) {
        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        // Validate amount matches plan price
        if (request.getAmount().compareTo(plan.getPrice()) != 0) {
            throw new RuntimeException("Amount must match plan price");
        }
        
        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setPlan(plan);
        deposit.setAmount(request.getAmount());
        
        BigDecimal bonusAmount = BigDecimal.ZERO;
        
        // Handle promo code if provided
        if (request.getPromoCode() != null && !request.getPromoCode().trim().isEmpty()) {
            PromoCode promoCode = promoCodeRepository.findByCode(request.getPromoCode())
                    .orElseThrow(() -> new RuntimeException("Invalid promo code"));
            
            if (!promoCode.isValid()) {
                throw new RuntimeException("Promo code is expired or usage limit exceeded");
            }
            
            bonusAmount = promoCode.getBonusValue();
            deposit.setPromoCode(promoCode);
            deposit.setBonusAmount(bonusAmount);
            
            // Update promo code usage
            promoCode.setUsedCount(promoCode.getUsedCount() + 1);
            promoCodeRepository.save(promoCode);
        }
        
        depositRepository.save(deposit);
        
        TransactionDTOs.DepositResponse response = new TransactionDTOs.DepositResponse();
        response.setMessage("Deposit request submitted successfully. Please transfer the amount to the provided wallet address.");
        response.setUsdtWalletAddress(usdtWalletAddress);
        response.setTotalAmount(request.getAmount().add(bonusAmount));
        response.setBonusAmount(bonusAmount);
        
        return response;
    }
    
    public void approveDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new RuntimeException("Deposit already processed");
        }
        
        User user = deposit.getUser();
        
        // Update deposit status
        deposit.setStatus(DepositStatus.APPROVED);
        deposit.setApprovedAt(LocalDateTime.now());
        depositRepository.save(deposit);
        
        // Update user plan and balance
        user.setCurrentPlan(deposit.getPlan());
        user.setSubscriptionDate(LocalDateTime.now());
        
        BigDecimal totalAmount = deposit.getAmount().add(deposit.getBonusAmount());
        user.setTotalBalance(user.getTotalBalance().add(totalAmount));
        user.setFrozenBalance(user.getFrozenBalance().add(deposit.getAmount()));
        
        userRepository.save(user);
        
        // Process referral commissions for first deposit
        if (isFirstDeposit(user)) {
            referralService.processReferralCommissions(user, deposit);
        }
    }
    
    private boolean isFirstDeposit(User user) {
        return depositRepository.findFirstApprovedDeposit(user).isEmpty();
    }
} 