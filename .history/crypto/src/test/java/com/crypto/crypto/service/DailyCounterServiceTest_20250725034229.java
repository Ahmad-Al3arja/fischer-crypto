package com.crypto.crypto.service;

import com.crypto.crypto.entity.DailyCounter;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.exception.CustomExceptions;
import com.crypto.crypto.repository.DailyCounterRepository;
import com.crypto.crypto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyCounterServiceTest {
    
    @Mock
    private DailyCounterRepository dailyCounterRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UpgradeService upgradeService;
    
    @InjectMocks
    private DailyCounterService dailyCounterService;
    
    private User testUser;
    private Plan testPlan;
    private DailyCounter testCounter;
    
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setDisplayUsername("testuser");
        testUser.setPhoneNumber("+1234567890");
        
        testPlan = new Plan();
        testPlan.setId(1L);
        testPlan.setName("Level 1");
        testPlan.setPrice(new BigDecimal("100"));
        testPlan.setDailyProfitMin(new BigDecimal("5"));
        testPlan.setDailyProfitMax(new BigDecimal("15"));
        testPlan.setPlanLevel(1);
        
        testUser.setCurrentPlan(testPlan);
        
        testCounter = new DailyCounter();
        testCounter.setId(1L);
        testCounter.setUser(testUser);
        testCounter.setPlanDay(1);
        testCounter.setIsActive(false);
        testCounter.setIsCompleted(false);
        testCounter.setStartTime(LocalDateTime.now().minusHours(25)); // Set start time to 25 hours ago
        testCounter.setEndTime(LocalDateTime.now().minusHours(1)); // Set end time to 1 hour ago
    }
    
    @Test
    void testActivateCounter_Success() {
        // Given
        when(dailyCounterRepository.findByUser(testUser)).thenReturn(Optional.of(testCounter));
        when(dailyCounterRepository.save(any(DailyCounter.class))).thenReturn(testCounter);
        
        // When
        dailyCounterService.activateCounter(testUser);
        
        // Then
        verify(dailyCounterRepository).save(any(DailyCounter.class));
        assertTrue(testCounter.getIsActive());
        assertFalse(testCounter.getIsCompleted());
        assertEquals(1, testCounter.getPlanDay());
    }
    
    @Test
    void testActivateCounter_UserWithoutPlan_ThrowsException() {
        // Given
        testUser.setCurrentPlan(null);
        
        // When & Then
        assertThrows(CustomExceptions.DailyCounterNotActiveException.class, () -> {
            dailyCounterService.activateCounter(testUser);
        });
    }
    
    @Test
    void testActivateCounter_AlreadyActive_ThrowsException() {
        // Given
        testCounter.setIsActive(true);
        when(dailyCounterRepository.findByUser(testUser)).thenReturn(Optional.of(testCounter));
        
        // When & Then
        assertThrows(CustomExceptions.DailyCounterAlreadyActiveException.class, () -> {
            dailyCounterService.activateCounter(testUser);
        });
    }
    
    @Test
    void testCompleteCounter_Success() {
        // Given
        testCounter.setIsActive(true);
        testCounter.setCurrentDayProfit(new BigDecimal("10"));
        testUser.setTotalBalance(new BigDecimal("100"));
        
        when(dailyCounterRepository.findByUser(testUser)).thenReturn(Optional.of(testCounter));
        when(dailyCounterRepository.save(any(DailyCounter.class))).thenReturn(testCounter);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // When
        dailyCounterService.completeCounter(testUser);
        
        // Then
        verify(userRepository).save(testUser);
        verify(dailyCounterRepository).save(testCounter);
        verify(upgradeService).checkAndProcessAutoUpgrade(testUser);
        
        assertFalse(testCounter.getIsActive());
        assertTrue(testCounter.getIsCompleted());
        assertEquals(new BigDecimal("110"), testUser.getTotalBalance());
    }
    
    @Test
    void testCompleteCounter_NotActive_ThrowsException() {
        // Given
        testCounter.setIsActive(false);
        when(dailyCounterRepository.findByUser(testUser)).thenReturn(Optional.of(testCounter));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dailyCounterService.completeCounter(testUser);
        });
    }
    
    @Test
    void testDeactivateCounter_Success() {
        // Given
        testCounter.setIsActive(true);
        when(dailyCounterRepository.findByUser(testUser)).thenReturn(Optional.of(testCounter));
        when(dailyCounterRepository.save(any(DailyCounter.class))).thenReturn(testCounter);
        
        // When
        dailyCounterService.deactivateCounter(testUser);
        
        // Then
        verify(dailyCounterRepository).save(testCounter);
        assertFalse(testCounter.getIsActive());
    }
    
    @Test
    void testDeactivateCounter_NoCounter_ThrowsException() {
        // Given
        when(dailyCounterRepository.findByUserAndIsActiveTrue(testUser)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(CustomExceptions.DailyCounterNotFoundException.class, () -> {
            dailyCounterService.deactivateCounter(testUser);
        });
    }
} 