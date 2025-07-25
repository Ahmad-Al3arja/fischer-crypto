package com.crypto.crypto.service;

import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.repository.DailyCounterRepository;
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
    private PlanService planService;
    
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    public void checkAndProcessAutoUpgrade(User user) {
        if (user.getCurrentPlan() == null) {
            return;
        }
        
        Plan nextPlan = planService.findNextPlan(user.getCurrentPlan().getPlanLevel());
        if (nextPlan == null) {
            return; // Already at highest plan
        }
        
        BigDecimal availableBalance = user.getTotalBalance().subtract(user.getFrozenBalance());
        
        // Check if user has enough balance for next plan
        if (availableBalance.compareTo(nextPlan.getPrice()) >= 0) {
            upgradeUserToPlan(user, nextPlan);
        }
    }
    
    public void upgradeUserToPlan(User user, Plan newPlan) {
        BigDecimal upgradeAmount = newPlan.getPrice();
        if (user.getTotalBalance().compareTo(upgradeAmount) < 0) {
            throw new RuntimeException("Insufficient balance for upgrade");
        }
        user.setFrozenBalance(user.getFrozenBalance().add(upgradeAmount));
        user.setCurrentPlan(newPlan);
        userRepository.save(user);
        dailyCounterRepository.findByUser(user).ifPresent(counter -> {
            counter.setIsActive(false);
            counter.setIsCompleted(false);
            counter.setPlanDay(1);
            counter.setCurrentDay(1);
            dailyCounterRepository.save(counter);
        });
        System.out.println("User " + user.getDisplayUsername() + " auto-upgraded to " + newPlan.getName() + " - Frozen amount: $" + upgradeAmount);
    }
    
    public void recalculateUpgradeProgress(User user) {
        if (user.getCurrentPlan() == null) {
            return;
        }
        
        // Recalculate if user can still maintain current plan level
        BigDecimal totalInvestment = user.getFrozenBalance();
        BigDecimal currentPlanPrice = user.getCurrentPlan().getPrice();
        
        if (totalInvestment.compareTo(currentPlanPrice) < 0) {
            // Downgrade to appropriate plan based on available balance
            // This is a simplified version - you might want to implement more complex logic
            downgradeToPreviousPlan(user);
        }
    }
    
    private void downgradeToPreviousPlan(User user) {
        // Find the highest plan the user can afford with frozen balance
        BigDecimal frozenBalance = user.getFrozenBalance();
        
        // This would need implementation based on your business logic
        // For now, we'll keep the current plan but adjust daily profits
    }
} 