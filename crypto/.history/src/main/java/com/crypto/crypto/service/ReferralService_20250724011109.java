package com.crypto.crypto.service;

import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.ReferralEarningRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class ReferralService {
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final BigDecimal DIRECT_COMMISSION_RATE = new BigDecimal("0.12"); // 12%
    private static final BigDecimal GRAND_COMMISSION_RATE = new BigDecimal("0.06"); // 6%
    
    public void processReferralCommissions(User referredUser, Deposit deposit) {
        BigDecimal depositAmount = deposit.getAmount();
        
        // Process direct referrer commission (12%)
        if (referredUser.getReferrer() != null) {
            BigDecimal directCommission = depositAmount.multiply(DIRECT_COMMISSION_RATE);
            processCommission(referredUser.getReferrer(), referredUser, deposit, 
                    directCommission, CommissionType.DIRECT);
        }
        
        // Process grand referrer commission (6%)
        if (referredUser.getGrandReferrer() != null) {
            BigDecimal grandCommission = depositAmount.multiply(GRAND_COMMISSION_RATE);
            processCommission(referredUser.getGrandReferrer(), referredUser, deposit, 
                    grandCommission, CommissionType.GRAND);
        }
    }
    
    private void processCommission(User referrer, User referredUser, Deposit deposit, 
                                 BigDecimal commissionAmount, CommissionType commissionType) {
        // Create referral earning record
        ReferralEarning earning = new ReferralEarning();
        earning.setReferrer(referrer);
        earning.setReferredUser(referredUser);
        earning.setDeposit(deposit);
        earning.setAmount(commissionAmount);
        earning.setCommissionType(commissionType);
        
        referralEarningRepository.save(earning);
        
        // Add commission to referrer's balance and referral earnings
        referrer.setTotalBalance(referrer.getTotalBalance().add(commissionAmount));
        referrer.setReferralEarnings(referrer.getReferralEarnings().add(commissionAmount));
        
        userRepository.save(referrer);
    }
} 