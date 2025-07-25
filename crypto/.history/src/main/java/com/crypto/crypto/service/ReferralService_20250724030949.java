package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
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
        earning.setUser(referredUser); // The user who made the deposit
        earning.setReferrer(referrer); // The user who gets the commission
        earning.setReferredUser(referredUser); // The referred user (same as user)
        earning.setDeposit(deposit); // The deposit that triggered the commission
        earning.setAmount(commissionAmount);
        earning.setCommissionType(commissionType);
        
        referralEarningRepository.save(earning);
        
        // Add commission to referrer's balance and referral earnings
        referrer.setTotalBalance(referrer.getTotalBalance().add(commissionAmount));
        referrer.setReferralEarnings(referrer.getReferralEarnings().add(commissionAmount));
        
        userRepository.save(referrer);
    }
    
    public UserDTOs.ReferralStatsResponse getReferralStats(User user) {
        UserDTOs.ReferralStatsResponse response = new UserDTOs.ReferralStatsResponse();
        
        // Count direct referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        response.setTotalDirectReferrals(directReferrals);
        
        // Count second-level referrals
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setTotalSecondLevelReferrals(secondLevelReferrals);
        
        // Get total referral earnings
        response.setTotalReferralEarnings(user.getReferralEarnings());
        
        // Generate referral link
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getUsername());
        
        return response;
    }
} 