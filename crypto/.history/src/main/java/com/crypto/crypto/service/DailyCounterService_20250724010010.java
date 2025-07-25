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

@Service
@Transactional
public class DailyCounterService {
    
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public void activateCounter(User user) {
        if (user.getCurrentPlan() == null) {
            throw new RuntimeException("User must have an active plan to start daily counter");
        }
        
        DailyCounter counter = dailyCounterRepository.findByUser(user).orElse(new DailyCounter());
        counter.setUser(user);
        counter.setIsActive(true);
        counter.setIsCompleted(false);
        counter.setStartTime(LocalDateTime.now());
        counter.setEndTime(LocalDateTime.now().plusHours(24));
        counter.setPlanDay(1);
        
        // Calculate initial daily profit (starts from min)
        Plan plan = user.getCurrentPlan();
        counter.setCurrentDayProfit(plan.getDailyProfitMin());
        
        dailyCounterRepository.save(counter);
    }
    
    public void resetCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No active counter found"));
        
        if (!counter.getIsCompleted()) {
            throw new RuntimeException("Counter must be completed before reset");
        }
        
        // Move to next day
        int nextDay = Math.min(counter.getPlanDay() + 1, 30);
        counter.setPlanDay(nextDay);
        counter.setIsCompleted(false);
        counter.setStartTime(LocalDateTime.now());
        counter.setEndTime(LocalDateTime.now().plusHours(24));
        
        // Calculate progressive daily profit
        Plan plan = user.getCurrentPlan();
        BigDecimal progressRatio = BigDecimal.valueOf(nextDay - 1).divide(BigDecimal.valueOf(29), 4, RoundingMode.HALF_UP);
        BigDecimal profitRange = plan.getDailyProfitMax().subtract(plan.getDailyProfitMin());
        BigDecimal currentProfit = plan.getDailyProfitMin().add(profitRange.multiply(progressRatio));
        
        counter.setCurrentDayProfit(currentProfit);
        
        dailyCounterRepository.save(counter);
    }
    
    @Scheduled(fixedRate = 60000) // Check every minute
    public void processExpiredCounters() {
        LocalDateTime now = LocalDateTime.now();
        var expiredCounters = dailyCounterRepository.findExpiredCounters(now);
        
        for (DailyCounter counter : expiredCounters) {
            User user = counter.getUser();
            
            // Add daily profit to user balance
            user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
            userRepository.save(user);
            
            // Mark counter as completed
            counter.setIsActive(false);
            counter.setIsCompleted(true);
            dailyCounterRepository.save(counter);
        }
    }
    
    public void disableCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user).orElse(null);
        if (counter != null) {
            counter.setIsActive(false);
            dailyCounterRepository.save(counter);
        }
    }
} 