package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.*;
import com.crypto.crypto.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DailyCounterRepository dailyCounterRepository;
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private DailyCounterService dailyCounterService;
    
    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Find referrer by username (referral code)
        User referrer = userRepository.findByUsername(request.getReferralCode())
                .orElseThrow(() -> new RuntimeException("Invalid referral code"));
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setReferrer(referrer);
        user.setGrandReferrer(referrer.getReferrer()); // Can be null
        user.setStatus(UserStatus.INACTIVE);
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(savedUser.getPhoneNumber());
        
        return new AuthDTOs.LoginResponse(jwt, savedUser.getId(), 
                savedUser.getUsername(), savedUser.getRole().name());
    }
    
    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        // Find user by phone number
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Invalid phone number or password"));
        
        // Check if account is activated
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Your account is under review");
        }
        
        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getPhoneNumber(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(user.getPhoneNumber());
        
        return new AuthDTOs.LoginResponse(jwt, user.getId(), 
                user.getUsername(), user.getRole().name());
    }
    
    public UserDTOs.DashboardResponse getDashboard(User user) {
        UserDTOs.DashboardResponse response = new UserDTOs.DashboardResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setCurrentPlanName(user.getCurrentPlan() != null ? 
                user.getCurrentPlan().getName() : "No Plan");
        response.setTotalBalance(user.getTotalBalance());
        response.setTotalProfits(user.getReferralEarnings());
        
        // Check activation status
        if (user.getStatus() != UserStatus.ACTIVE) {
            response.setActivationPending(true);
            response.setActivationMessage("Your account is under review");
            return response;
        }
        
        // Get daily counter status
        DailyCounter counter = dailyCounterRepository.findByUser(user).orElse(null);
        UserDTOs.CounterStatus counterStatus = new UserDTOs.CounterStatus();
        
        if (counter != null) {
            counterStatus.setActive(counter.getIsActive());
            counterStatus.setCompleted(counter.getIsCompleted());
            
            if (counter.getIsActive()) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(counter.getEndTime())) {
                    long remainingSeconds = ChronoUnit.SECONDS.between(now, counter.getEndTime());
                    counterStatus.setRemainingSeconds(remainingSeconds);
                    response.setDailyProfit(counter.getCurrentDayProfit());
                } else {
                    // Time expired, needs reset
                    counterStatus.setNeedsReset(true);
                    counterStatus.setActive(false);
                }
            }
        }
        
        response.setCounterStatus(counterStatus);
        return response;
    }
    
    public UserDTOs.ProfileResponse getProfile(User user) {
        UserDTOs.ProfileResponse response = new UserDTOs.ProfileResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setPlanName(user.getCurrentPlan() != null ? 
                user.getCurrentPlan().getName() : "No Plan");
        response.setSubscriptionDate(user.getSubscriptionDate());
        response.setTotalBalance(user.getTotalBalance());
        response.setReferralEarnings(user.getReferralEarnings());
        
        // Count referrals
        long directReferrals = userRepository.countDirectReferrals(user);
        long secondLevelReferrals = userRepository.countSecondLevelReferrals(user);
        response.setNumberOfReferrals((int) directReferrals);
        response.setSecondLevelReferrals((int) secondLevelReferrals);
        
        // Generate referral link
        response.setReferralLink("https://platform.com/register?ref=" + user.getUsername());
        
        return response;
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
} 