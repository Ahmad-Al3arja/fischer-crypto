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
    
    public void processReferralCommissions(User user, Deposit deposit) {
        // Process direct referrer commission (12%)
        if (user.getReferrer() != null) {
            BigDecimal directCommission = deposit.getAmount().multiply(BigDecimal.valueOf(0.12));
            
            ReferralEarning directEarning = new ReferralEarning();
            directEarning.setReferrer(user.getReferrer());
            directEarning.setReferredUser(user);
            directEarning.setDeposit(deposit);
            directEarning.setAmount(directCommission);
            directEarning.setCommissionType(CommissionType.DIRECT);
            
            referralEarningRepository.save(directEarning);
            
            // Update referrer's balance
            User referrer = user.getReferrer();
            referrer.setReferralEarnings(referrer.getReferralEarnings().add(directCommission));
            referrer.setTotalBalance(referrer.getTotalBalance().add(directCommission));
            userRepository.save(referrer);
        }
        
        // Process grand referrer commission (6%)
        if (user.getGrandReferrer() != null) {
            BigDecimal grandCommission = deposit.getAmount().multiply(BigDecimal.valueOf(0.06));
            
            ReferralEarning grandEarning = new ReferralEarning();
            grandEarning.setReferrer(user.getGrandReferrer());
            grandEarning.setReferredUser(user);
            grandEarning.setDeposit(deposit);
            grandEarning.setAmount(grandCommission);
            grandEarning.setCommissionType(CommissionType.GRAND);
            
            referralEarningRepository.save(grandEarning);
            
            // Update grand referrer's balance
            User grandReferrer = user.getGrandReferrer();
            grandReferrer.setReferralEarnings(grandReferrer.getReferralEarnings().add(grandCommission));
            grandReferrer.setTotalBalance(grandReferrer.getTotalBalance().add(grandCommission));
            userRepository.save(grandReferrer);
        }
    }
} 