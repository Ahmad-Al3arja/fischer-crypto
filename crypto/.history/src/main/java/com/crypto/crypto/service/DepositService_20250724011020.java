package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.DepositRepository;
import com.crypto.crypto.repository.PromoCodeRepository;
import com.crypto.crypto.repository.UserRepository;
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
    private UserRepository userRepository;
    
    @Autowired
    private PlanService planService;
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    @Autowired
    private ReferralService referralService;
    
    @Value("${app.platform.usdt-wallet}")
    private String platformUsdtWallet;
    
    public TransactionDTOs.DepositResponse createDeposit(User user, TransactionDTOs.DepositRequest request) {
        // Validate plan exists
        Plan plan = planService.findById(request.getPlanId());
        
        // Validate amount matches plan price
        if (request.getAmount().compareTo(plan.getPrice()) != 0) {
            throw new RuntimeException("Deposit amount must match plan price");
        }
        
        // Create deposit
        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setPlan(plan);
        deposit.setAmount(request.getAmount());
        deposit.setStatus(DepositStatus.PENDING);
        
        // Process promo code if provided
        BigDecimal bonusAmount = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            try {
                PromoCode promoCode = promoCodeRepository.findByCode(request.getPromoCode())
                        .orElseThrow(() -> new RuntimeException("Invalid promo code"));
                
                if (!promoCode.isValid()) {
                    throw new RuntimeException("Promo code is expired or usage limit exceeded");
                }
                
                bonusAmount = promoCode.getBonusValue();
                deposit.setPromoCode(promoCode);
                deposit.setBonusAmount(bonusAmount);
                
                // Increment usage count
                promoCode.setUsedCount(promoCode.getUsedCount() + 1);
                promoCodeRepository.save(promoCode);
                
            } catch (Exception e) {
                throw new RuntimeException("Error processing promo code: " + e.getMessage());
            }
        }
        
        depositRepository.save(deposit);
        
        // Create response
        TransactionDTOs.DepositResponse response = new TransactionDTOs.DepositResponse();
        response.setMessage("Deposit request created successfully. Please send " + 
                request.getAmount().add(bonusAmount) + " USDT to the provided wallet address.");
        response.setUsdtWalletAddress(platformUsdtWallet);
        response.setTotalAmount(request.getAmount().add(bonusAmount));
        response.setBonusAmount(bonusAmount);
        
        return response;
    }
    
    public void approveDeposit(Long depositId) {
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(() -> new RuntimeException("Deposit not found"));
        
        if (deposit.getStatus() != DepositStatus.PENDING) {
            throw new RuntimeException("Deposit is not pending");
        }
        
        User user = deposit.getUser();
        
        // Add amount to user balance
        BigDecimal totalAmount = deposit.getAmount().add(deposit.getBonusAmount());
        user.setTotalBalance(user.getTotalBalance().add(totalAmount));
        
        // Set current plan
        user.setCurrentPlan(deposit.getPlan());
        user.setSubscriptionDate(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Update deposit status
        deposit.setStatus(DepositStatus.APPROVED);
        deposit.setApprovedAt(LocalDateTime.now());
        depositRepository.save(deposit);
        
        // Process referral commissions
        referralService.processReferralCommissions(user, deposit);
    }
} 