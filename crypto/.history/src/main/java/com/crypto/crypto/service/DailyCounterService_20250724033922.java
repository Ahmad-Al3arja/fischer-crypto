package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
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
import java.time.temporal.ChronoUnit;
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
        
        // Check if user has already completed today's counter
        if (counter.getIsCompleted() && 
            counter.getEndTime() != null && 
            counter.getEndTime().isAfter(LocalDateTime.now().minusDays(1))) {
            throw new RuntimeException("Counter already completed today. Wait for reset.");
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
    
    public void deactivateCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No active counter found"));
        
        counter.setIsActive(false);
        dailyCounterRepository.save(counter);
    }
    
    public void completeCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No active counter found"));
        
        if (!counter.getIsActive()) {
            throw new RuntimeException("Counter is not active");
        }
        
        if (counter.getEndTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Counter time has not expired yet");
        }
        
        if (counter.getIsCompleted()) {
            throw new RuntimeException("Counter already completed");
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
    
    public UserDTOs.CounterStatus getCounterStatus(User user) {
        UserDTOs.CounterStatus status = new UserDTOs.CounterStatus();
        
        DailyCounter counter = dailyCounterRepository.findByUser(user).orElse(null);
        
        if (counter == null) {
            status.setActive(false);
            status.setCompleted(false);
            status.setRemainingSeconds(0);
            status.setNeedsReset(false);
            return status;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        if (counter.getIsActive() && counter.getEndTime().isAfter(now)) {
            // Counter is active and not expired
            status.setActive(true);
            status.setCompleted(false);
            status.setRemainingSeconds(ChronoUnit.SECONDS.between(now, counter.getEndTime()));
            status.setNeedsReset(false);
        } else if (counter.getIsActive() && counter.getEndTime().isBefore(now)) {
            // Counter is expired, ready for completion
            status.setActive(true);
            status.setCompleted(false);
            status.setRemainingSeconds(0);
            status.setNeedsReset(true);
        } else if (counter.getIsCompleted()) {
            // Counter completed, can be reactivated after 24 hours
            boolean canReactivate = counter.getEndTime().isBefore(LocalDateTime.now().minusHours(24));
            status.setActive(false);
            status.setCompleted(true);
            status.setRemainingSeconds(0);
            status.setNeedsReset(canReactivate);
        } else {
            // Counter inactive
            status.setActive(false);
            status.setCompleted(false);
            status.setRemainingSeconds(0);
            status.setNeedsReset(false);
        }
        
        return status;
    }
    
    public BigDecimal getCurrentDayProfit(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user).orElse(null);
        if (counter == null || user.getCurrentPlan() == null) {
            return BigDecimal.ZERO;
        }
        
        return calculateDailyProfit(user.getCurrentPlan(), counter.getPlanDay());
    }
    
    // REMOVED AUTO-COMPLETION - Let users claim manually
    // @Scheduled(fixedRate = 60000) // Check every minute
    // public void processExpiredCounters() { ... }
    
    private BigDecimal calculateDailyProfit(Plan plan, int planDay) {
        BigDecimal minProfit = plan.getDailyProfitMin();
        BigDecimal maxProfit = plan.getDailyProfitMax();
        
        // Progressive profit calculation (linear increase over 30 days)
        BigDecimal dailyIncrease = maxProfit.subtract(minProfit)
                .divide(BigDecimal.valueOf(29), 4, RoundingMode.HALF_UP);
        BigDecimal currentProfit = minProfit.add(
                dailyIncrease.multiply(BigDecimal.valueOf(planDay - 1)));
        
        return currentProfit.setScale(2, RoundingMode.HALF_UP);
    }
}