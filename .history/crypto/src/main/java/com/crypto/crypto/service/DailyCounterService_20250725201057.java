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
import java.time.temporal.ChronoUnit;
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
    
    public UserDTOs.DailyCounterResponse getUserDailyCounter(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUser(user);
        
        UserDTOs.DailyCounterResponse response = new UserDTOs.DailyCounterResponse();
        
        if (counterOpt.isPresent()) {
            DailyCounter counter = counterOpt.get();
            
            // Calculate accurate remaining seconds
            long remainingSeconds = 0;
            boolean needsReset = false;
            
            if (counter.getIsActive() && counter.getEndTime() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(counter.getEndTime())) {
                    remainingSeconds = ChronoUnit.SECONDS.between(now, counter.getEndTime());
                } else {
                    // Timer has expired, mark as completed
                    counter.setIsActive(false);
                    counter.setIsCompleted(true);
                    dailyCounterRepository.save(counter);
                    needsReset = true;
                }
            }
            
            response.setActive(counter.getIsActive());
            response.setCompleted(counter.getIsCompleted());
            response.setCurrentDayProfit(counter.getCurrentDayProfit());
            response.setCurrentDay(counter.getCurrentDay());
            response.setRemainingSeconds(Math.max(0, remainingSeconds));
            response.setNeedsReset(needsReset || (counter.getIsCompleted() && !counter.getIsActive()));
        } else {
            // No counter exists
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
        LocalDateTime now = LocalDateTime.now();
        
        if (existingOpt.isPresent()) {
            counter = existingOpt.get();
            
            // Check if there's already an active timer
            if (counter.getIsActive() && counter.getEndTime() != null && now.isBefore(counter.getEndTime())) {
                throw new CustomExceptions.DailyCounterAlreadyActiveException("Daily counter is already active for this user");
            }
            
            // Reset for new activation
            if (counter.getIsCompleted() || (counter.getEndTime() != null && now.isAfter(counter.getEndTime()))) {
                // Move to next day if completed
                if (counter.getPlanDay() >= 30) {
                    counter.setPlanDay(1);
                    counter.setCurrentDay(1);
                } else {
                    counter.setPlanDay(counter.getPlanDay() + 1);
                    counter.setCurrentDay(counter.getCurrentDay() + 1);
                }
            } else {
                // Keep same day if reactivating
                if (counter.getCurrentDay() == 0) {
                    counter.setCurrentDay(1);
                    counter.setPlanDay(1);
                }
            }
        } else {
            // Create new counter
            counter = new DailyCounter();
            counter.setUser(user);
            counter.setPlanDay(1);
            counter.setCurrentDay(1);
        }
        
        // Set timer for 24 hours from now
        counter.setStartTime(now);
        counter.setEndTime(now.plusHours(24));
        counter.setIsActive(true);
        counter.setIsCompleted(false);
        
        // Calculate progressive profit for current day
        BigDecimal dailyProfit = calculateProgressiveProfit(user.getCurrentPlan(), counter.getCurrentDay());
        counter.setCurrentDayProfit(dailyProfit);
        
        dailyCounterRepository.save(counter);
        
        System.out.println("Timer activated for user: " + user.getDisplayUsername() + 
                         " - Day: " + counter.getCurrentDay() + 
                         " - Profit: $" + dailyProfit +
                         " - End time: " + counter.getEndTime());
    }
    
    public void deactivateCounter(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUser(user);
        if (counterOpt.isPresent()) {
            DailyCounter counter = counterOpt.get();
            counter.setIsActive(false);
            dailyCounterRepository.save(counter);
        }
    }
    
    public void completeCounter(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUser(user);
        if (!counterOpt.isPresent()) {
            throw new CustomExceptions.DailyCounterNotFoundException("No counter found for user");
        }
        
        DailyCounter counter = counterOpt.get();
        
        // Check if timer has actually completed (24 hours passed)
        LocalDateTime now = LocalDateTime.now();
        if (counter.getEndTime() != null && now.isBefore(counter.getEndTime())) {
            long remainingSeconds = ChronoUnit.SECONDS.between(now, counter.getEndTime());
            throw new CustomExceptions.DailyCounterNotActiveException(
                "Timer hasn't completed yet. Remaining time: " + formatTime(remainingSeconds));
        }
        
        if (!counter.getIsActive() && !counter.getIsCompleted()) {
            throw new CustomExceptions.DailyCounterNotActiveException("Counter is not active");
        }
        
        // Add profit to user balance
        user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
        
        // Mark counter as completed
        counter.setIsActive(false);
        counter.setIsCompleted(true);
        
        dailyCounterRepository.save(counter);
        userRepository.save(user);
        
        // Check for auto-upgrade
        upgradeService.checkAndProcessAutoUpgrade(user);
        
        System.out.println("Counter completed for user: " + user.getDisplayUsername() + 
                         " - Profit added: $" + counter.getCurrentDayProfit());
    }
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processDailyProfits() {
        LocalDateTime now = LocalDateTime.now();
        List<DailyCounter> activeCounters = dailyCounterRepository.findByIsActiveTrue();
        
        for (DailyCounter counter : activeCounters) {
            if (counter.getEndTime() != null && now.isAfter(counter.getEndTime())) {
                // Auto-complete expired timer
                User user = counter.getUser();
                
                try {
                    // Add profits to user balance
                    user.setTotalBalance(user.getTotalBalance().add(counter.getCurrentDayProfit()));
                    userRepository.save(user);
                    
                    // Mark counter as completed
                    counter.setIsCompleted(true);
                    counter.setIsActive(false);
                    dailyCounterRepository.save(counter);
                    
                    // Check for auto-upgrade
                    upgradeService.checkAndProcessAutoUpgrade(user);
                    
                    System.out.println("Auto-completed expired timer for user: " + user.getDisplayUsername() + 
                                     " - Profit: $" + counter.getCurrentDayProfit());
                } catch (Exception e) {
                    System.err.println("Error auto-completing timer for user " + user.getDisplayUsername() + ": " + e.getMessage());
                }
            }
        }
    }
    
    private BigDecimal calculateProgressiveProfit(Plan plan, int dayNumber) {
        BigDecimal minProfit = plan.getDailyProfitMin();
        BigDecimal maxProfit = plan.getDailyProfitMax();
        
        // Ensure dayNumber is within 1-30 range
        int adjustedDay = Math.min(Math.max(dayNumber, 1), 30);
        
        // Calculate progression factor (0.0 to 1.0)
        double progressionFactor = (adjustedDay - 1) / 29.0; // Day 1 = 0.0, Day 30 = 1.0
        
        // Linear interpolation between min and max
        BigDecimal range = maxProfit.subtract(minProfit);
        BigDecimal progressiveIncrease = range.multiply(BigDecimal.valueOf(progressionFactor));
        
        // Add some randomness (±3%) to make it more realistic
        Random random = new Random();
        double randomFactor = 0.97 + (random.nextDouble() * 0.06); // 0.97 to 1.03
        
        BigDecimal finalProfit = minProfit.add(progressiveIncrease)
            .multiply(BigDecimal.valueOf(randomFactor))
            .setScale(2, RoundingMode.HALF_UP);
        
        // Ensure we don't exceed the max profit even with randomness
        if (finalProfit.compareTo(maxProfit) > 0) {
            finalProfit = maxProfit;
        }
        
        return finalProfit;
    }
    
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
    
    public BigDecimal getCurrentDayProfit(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUser(user);
        if (counterOpt.isPresent()) {
            return counterOpt.get().getCurrentDayProfit();
        }
        return BigDecimal.ZERO;
    }
    
    public UserDTOs.CounterStatus getCounterStatus(User user) {
        Optional<DailyCounter> counterOpt = dailyCounterRepository.findByUser(user);
        
        UserDTOs.CounterStatus status = new UserDTOs.CounterStatus();
        
        if (counterOpt.isPresent()) {
            DailyCounter counter = counterOpt.get();
            LocalDateTime now = LocalDateTime.now();
            
            long remainingSeconds = 0;
            if (counter.getIsActive() && counter.getEndTime() != null) {
                if (now.isBefore(counter.getEndTime())) {
                    remainingSeconds = ChronoUnit.SECONDS.between(now, counter.getEndTime());
                } else {
                    // Timer expired, auto-complete
                    counter.setIsActive(false);
                    counter.setIsCompleted(true);
                    dailyCounterRepository.save(counter);
                }
            }
            
            status.setActive(counter.getIsActive());
            status.setCompleted(counter.getIsCompleted());
            status.setRemainingSeconds(Math.max(0, remainingSeconds));
            status.setNeedsReset(counter.getIsCompleted() && !counter.getIsActive());
        } else {
            status.setActive(false);
            status.setCompleted(false);
            status.setRemainingSeconds(0);
            status.setNeedsReset(false);
        }
        
        return status;
    }
}