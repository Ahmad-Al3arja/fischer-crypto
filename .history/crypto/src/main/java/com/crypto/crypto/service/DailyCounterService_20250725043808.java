package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.exception.CustomExceptions;
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
import java.util.Optional;
import java.util.Random;
@Service
@Transactional
public class DailyCounterService {
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UpgradeService upgradeService;
    
    @Autowired
    private UserService userService;
    
    public UserDTOs.DailyCounterResponse getUserDailyCounter() {
        User user = userService.getCurrentUser();
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
    
    public void activateCounter(User user) {
        if (user.getCurrentPlan() == null) {
            throw new CustomExceptions.DailyCounterNotActiveException("User must have an active plan to start daily counter");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new CustomExceptions.DailyCounterNotActiveException("User account must be activated by admin first");
        }
        
        Optional<DailyCounter> existingOpt = dailyCounterRepository.findByUser(user);
        DailyCounter counter;
        
        if (existingOpt.isPresent()) {
            counter = existingOpt.get();
            if (counter.getIsActive()) {
                throw new CustomExceptions.DailyCounterAlreadyActiveException("Daily counter is already active for this user");
            }
            
            // Only increment planDay after completion
            if (counter.getIsCompleted()) {
                if (counter.getPlanDay() < 30) {
                    counter.setPlanDay(counter.getPlanDay() + 1);
                    counter.setCurrentDay(counter.getPlanDay());
                } else {
                    // Reset cycle after 30 days
                    counter.setPlanDay(1);
                    counter.setCurrentDay(1);
                }
            }
            
            // Reset counter if needed (30-day cycle)
            resetCounterIfNeeded(counter);
            
            counter.setStartTime(LocalDateTime.now());
            counter.setEndTime(LocalDateTime.now().plusHours(24));
            counter.setIsActive(true);
            counter.setIsCompleted(false);
            
            // Calculate profit for CURRENT planDay, not incremented one
            BigDecimal dailyProfit = calculateProgressiveProfit(user.getCurrentPlan(), counter.getCurrentDay());
            counter.setCurrentDayProfit(dailyProfit);
            
        } else {
            // New user
            counter = new DailyCounter();
            counter.setUser(user);
            counter.setStartTime(LocalDateTime.now());
            counter.setEndTime(LocalDateTime.now().plusHours(24));
            counter.setPlanDay(1); // Start from day 1
            counter.setCurrentDay(1);
            counter.setIsActive(true);
            counter.setIsCompleted(false);
            
            BigDecimal dailyProfit = calculateProgressiveProfit(user.getCurrentPlan(), 1);
            counter.setCurrentDayProfit(dailyProfit);
        }
        
        dailyCounterRepository.save(counter);
    }
    
    public void deactivateCounter(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUserAndIsActiveTrue(user);
        if (!counterOpt.isPresent()) {
            counterOpt = dailyCounterRepository.findByUser(user);
        }
        DailyCounter counter = counterOpt.orElseThrow(() -> new CustomExceptions.DailyCounterNotFoundException("No active counter found for user"));
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
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes instead of hourly
    public void processDailyProfits() {
        List<DailyCounter> activeCounters = dailyCounterRepository.findByIsActiveTrue();
        
        for (DailyCounter counter : activeCounters) {
            if (counter.getEndTime().isBefore(LocalDateTime.now())) {
                // Counter period completed - auto complete
                User user = counter.getUser();
                
                // Add profits to user balance
                user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
                userRepository.save(user);
                
                // Mark counter as completed
                counter.setIsCompleted(true);
                counter.setIsActive(false);
                dailyCounterRepository.save(counter);
                
                // Check for auto-upgrade
                upgradeService.checkAndProcessAutoUpgrade(user);
                
                System.out.println("Auto-completed counter for user: " + user.getDisplayUsername() + 
                                 " - Profit: " + counter.getCurrentDayProfit());
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

    private BigDecimal calculateProgressiveProfit(Plan plan, int dayNumber) {
        // Progressive calculation: starts at min, reaches max at day 30
        BigDecimal minProfit = plan.getDailyProfitMin();
        BigDecimal maxProfit = plan.getDailyProfitMax();
        
        // Calculate progression factor (0.0 to 1.0)
        double progressionFactor = Math.min((dayNumber - 1) / 29.0, 1.0); // 29 days progression
        
        // Linear interpolation between min and max
        BigDecimal range = maxProfit.subtract(minProfit);
        BigDecimal progressiveIncrease = range.multiply(BigDecimal.valueOf(progressionFactor));
        
        // Add some randomness (±5%) to make it more realistic
        Random random = new Random();
        double randomFactor = 0.95 + (random.nextDouble() * 0.1); // 0.95 to 1.05
        
        BigDecimal finalProfit = minProfit.add(progressiveIncrease)
            .multiply(BigDecimal.valueOf(randomFactor))
            .setScale(2, RoundingMode.HALF_UP);
        
        return finalProfit;
    }

    private void resetCounterIfNeeded(DailyCounter counter) {
        // Reset to day 1 if reached day 30 and completed
        if (counter.getPlanDay() >= 30 && counter.getIsCompleted()) {
            counter.setPlanDay(1);
            counter.setCurrentDay(1);
        }
    }

    public void completeCounter(User user) {
        DailyCounter counter = dailyCounterRepository.findByUser(user)
                .orElseThrow(() -> new CustomExceptions.DailyCounterNotFoundException("No counter found for user"));
        if (!counter.getIsActive()) {
            throw new CustomExceptions.DailyCounterNotActiveException("Counter is not active");
        }
        user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
        counter.setIsActive(false);
        counter.setIsCompleted(true);
        dailyCounterRepository.save(counter);
        userRepository.save(user);
        upgradeService.checkAndProcessAutoUpgrade(user);
    }
}