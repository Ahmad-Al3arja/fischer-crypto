package com.crypto.crypto.service;

import com.crypto.crypto.entity.DailyCounter;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.repository.DailyCounterRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DailyCounterService {
    
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UpgradeService upgradeService;
    
    public void activateCounter(User user) {
        if (user.getCurrentPlan() == null) {
            throw new RuntimeException("User must have an active plan to start counter");
        }
        
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElse(new DailyCounter());
        
        if (counter.getIsActive()) {
            throw new RuntimeException("Counter is already active");
        }
        
        LocalDateTime now = LocalDateTime.now();
        counter.setUser(user);
        counter.setStartTime(now);
        counter.setEndTime(now.plusHours(24));
        counter.setIsActive(true);
        counter.setIsCompleted(false);
        
        // Calculate current day profit
        BigDecimal dailyProfit = calculateDailyProfit(user.getCurrentPlan(), counter.getPlanDay());
        counter.setCurrentDayProfit(dailyProfit);
        
        dailyCounterRepository.save(counter);
    }
    
    public void completeCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No active counter found"));
        
        if (!counter.getIsActive() || counter.getIsCompleted()) {
            throw new RuntimeException("Counter is not active or already completed");
        }
        
        // Add profit to user balance
        user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
        userRepository.save(user);
        
        // Update counter
        counter.setIsActive(false);
        counter.setIsCompleted(true);
        
        // Increment plan day (max 30)
        if (counter.getPlanDay() < 30) {
            counter.setPlanDay(counter.getPlanDay() + 1);
        }
        
        dailyCounterRepository.save(counter);
        
        // Check for auto-upgrade
        upgradeService.checkAndProcessAutoUpgrade(user);
    }
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void processExpiredCounters() {
        LocalDateTime now = LocalDateTime.now();
        List<DailyCounter> expiredCounters = dailyCounterRepository.findExpiredCounters(now);
        
        for (DailyCounter counter : expiredCounters) {
            if (counter.getIsActive() && !counter.getIsCompleted()) {
                completeCounter(counter.getUser());
            }
        }
    }
    
    private BigDecimal calculateDailyProfit(Plan plan, int planDay) {
        BigDecimal minProfit = plan.getDailyProfitMin();
        BigDecimal maxProfit = plan.getDailyProfitMax();
        
        // Progressive profit calculation (linear increase over 30 days)
        BigDecimal dailyIncrease = maxProfit.subtract(minProfit).divide(BigDecimal.valueOf(29), 2, RoundingMode.HALF_UP);
        BigDecimal currentProfit = minProfit.add(dailyIncrease.multiply(BigDecimal.valueOf(planDay - 1)));
        
        return currentProfit.setScale(2, RoundingMode.HALF_UP);
    }
} 