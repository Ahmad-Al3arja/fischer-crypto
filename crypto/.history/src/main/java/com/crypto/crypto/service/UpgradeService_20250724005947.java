package com.crypto.crypto.service;

import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class UpgradeService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private DailyCounterService dailyCounterService;
    
    public void checkAndProcessUpgrade(User user) {
        if (user.getCurrentPlan() == null) {
            return; // No plan to upgrade from
        }
        
        Plan currentPlan = user.getCurrentPlan();
        Plan nextPlan = planRepository.findNextPlan(currentPlan.getPlanLevel()).orElse(null);
        
        if (nextPlan == null) {
            return; // Already at highest plan
        }
        
        BigDecimal availableBalance = user.getWithdrawableBalance();
        
        if (availableBalance.compareTo(nextPlan.getPrice()) >= 0) {
            // Auto-upgrade to next plan
            performUpgrade(user, nextPlan);
        }
    }
    
    private void performUpgrade(User user, Plan nextPlan) {
        // Freeze current capital + upgrade amount
        BigDecimal upgradeAmount = nextPlan.getPrice();
        user.setFrozenBalance(user.getFrozenBalance().add(upgradeAmount));
        user.setTotalBalance(user.getTotalBalance().subtract(upgradeAmount));
        
        // Update user plan
        user.setCurrentPlan(nextPlan);
        user.setSubscriptionDate(java.time.LocalDateTime.now());
        
        userRepository.save(user);
        
        // Reset daily counter for new plan
        dailyCounterService.disableCounter(user);
    }
    
    public void recalculateUpgradeProgress(User user) {
        if (user.getCurrentPlan() == null) {
            return;
        }
        
        Plan currentPlan = user.getCurrentPlan();
        Plan nextPlan = planRepository.findNextPlan(currentPlan.getPlanLevel()).orElse(null);
        
        if (nextPlan == null) {
            return;
        }
        
        BigDecimal availableBalance = user.getWithdrawableBalance();
        
        if (availableBalance.compareTo(nextPlan.getPrice()) >= 0) {
            performUpgrade(user, nextPlan);
        }
    }
} 