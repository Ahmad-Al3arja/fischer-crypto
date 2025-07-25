package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.exception.CustomExceptions;
import com.crypto.crypto.repository.DailyCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
@Service
@Transactional
public class DailyCounterService {
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    @Autowired
    private UpgradeService upgradeService;
    
    public UserDTOs.DailyCounterResponse getUserDailyCounter() {
        User user = getCurrentUser();
        DailyCounter counter = dailyCounterRepository.findByUserAndIsActiveTrue(user)
                .orElse(null);
        
        UserDTOs.DailyCounterResponse response = new UserDTOs.DailyCounterResponse();
        
        if (counter != null) {
            response.setActive(counter.getIsActive());
            response.setCompleted(counter.getIsCompleted());
            response.setCurrentDayProfit(counter.getCurrentDayProfit());
            response.setCurrentDay(counter.getCurrentDay());
            
            // Calculate remaining seconds
            if (counter.getEndTime() != null) {
                long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), counter.getEndTime()).getSeconds();
                response.setRemainingSeconds(Math.max(0, remainingSeconds));
            }
            
            response.setNeedsReset(counter.getIsCompleted() && counter.getEndTime().isBefore(LocalDateTime.now()));
        } else {
            response.setActive(false);
            response.setCompleted(false);
            response.setCurrentDayProfit(BigDecimal.ZERO);
            response.setCurrentDay(0);
            response.setRemainingSeconds(0);
            response.setNeedsReset(false);
        }
        
        return response;
    }
    
    private User getCurrentUser() {
        // This should be implemented to get the current authenticated user
        // For now, we'll throw an exception - this needs to be implemented properly
        throw new RuntimeException("getCurrentUser() method needs to be implemented");
    }
    
    public void activateCounter(User user) {
        // Check if user has an active plan
        if (user.getCurrentPlan() == null) {
            throw new CustomExceptions.DailyCounterNotActiveException("User must have an active plan to start daily counter");
        }
        
        // Check if counter is already active
        if (dailyCounterRepository.existsByUserAndIsActiveTrue(user)) {
            throw new CustomExceptions.DailyCounterAlreadyActiveException("Daily counter is already active for this user");
        }
        
        // Create new daily counter
        DailyCounter counter = new DailyCounter();
        counter.setUser(user);
        counter.setStartTime(LocalDateTime.now());
        counter.setEndTime(LocalDateTime.now().plusDays(1));
        counter.setPlanDay(1);
        counter.setCurrentDay(1);
        counter.setCurrentDayProfit(BigDecimal.ZERO);
        counter.setIsActive(true);
        counter.setIsCompleted(false);
        
        dailyCounterRepository.save(counter);
    }
    
    public void deactivateCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new CustomExceptions.DailyCounterNotFoundException("No active counter found for user"));
        
        counter.setIsActive(false);
        dailyCounterRepository.save(counter);
    }
    
    public BigDecimal getCurrentDayProfit(User user) {
        DailyCounter counter = dailyCounterRepository.findByUserAndIsActiveTrue(user)
                .orElse(null);
        
        if (counter != null) {
            return counter.getCurrentDayProfit();
        }
        
        return BigDecimal.ZERO;
    }
    
    public UserDTOs.CounterStatus getCounterStatus(User user) {
        DailyCounter counter = dailyCounterRepository.findByUserAndIsActiveTrue(user)
                .orElse(null);
        
        UserDTOs.CounterStatus status = new UserDTOs.CounterStatus();
        
        if (counter != null) {
            status.setActive(counter.getIsActive());
            status.setCompleted(counter.getIsCompleted());
            
            if (counter.getEndTime() != null) {
                long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), counter.getEndTime()).getSeconds();
                status.setRemainingSeconds(Math.max(0, remainingSeconds));
            }
            
            status.setNeedsReset(counter.getIsCompleted() && counter.getEndTime().isBefore(LocalDateTime.now()));
        } else {
            status.setActive(false);
            status.setCompleted(false);
            status.setRemainingSeconds(0);
            status.setNeedsReset(false);
        }
        
        return status;
    }
    
    @Scheduled(cron = "0 0 * * * *") // Run every hour
    public void processDailyProfits() {
        List<DailyCounter> activeCounters = dailyCounterRepository.findByIsActiveTrue();
        
        for (DailyCounter counter : activeCounters) {
            if (counter.getEndTime().isBefore(LocalDateTime.now())) {
                // Counter period completed
                counter.setIsCompleted(true);
                counter.setIsActive(false);
                dailyCounterRepository.save(counter);
                
                // Add profits to user balance
                User user = counter.getUser();
                user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
                // TODO: Save user
            } else {
                // Calculate and add daily profit
                calculateAndAddDailyProfit(counter);
            }
        }
    }
    
    private void calculateAndAddDailyProfit(DailyCounter counter) {
        User user = counter.getUser();
        Plan plan = user.getCurrentPlan();
        
        if (plan == null) {
            return;
        }
        
        // Calculate random daily profit within the plan's range
        Random random = new Random();
        BigDecimal minProfit = plan.getDailyProfitMin();
        BigDecimal maxProfit = plan.getDailyProfitMax();
        
        BigDecimal range = maxProfit.subtract(minProfit);
        BigDecimal randomValue = new BigDecimal(random.nextDouble()).multiply(range);
        BigDecimal dailyProfit = minProfit.add(randomValue).setScale(2, RoundingMode.HALF_UP);
        
        // Update counter
        counter.setCurrentDayProfit(counter.getCurrentDayProfit().add(dailyProfit));
        counter.setCurrentDay(counter.getCurrentDay() + 1);
        dailyCounterRepository.save(counter);
    }

    public void completeCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new CustomExceptions.DailyCounterNotFoundException("No counter found for user"));
        if (!counter.getIsActive()) {
            throw new CustomExceptions.DailyCounterNotActiveException("Counter is not active");
        }
        // Add profit to user balance
        user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
        // Mark counter as completed and inactive
        counter.setIsActive(false);
        counter.setIsCompleted(true);
        dailyCounterRepository.save(counter);
        // Save user and process upgrade
        // Assuming userRepository is available
        // userRepository.save(user);
        upgradeService.checkAndProcessAutoUpgrade(user);
    }
}