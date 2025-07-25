package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.UserRepository;
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
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
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
        
        // Find referrer
        User referrer = userRepository.findByUsername(request.getReferralCode())
                .orElseThrow(() -> new RuntimeException("Invalid referral code"));
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setReferrer(referrer);
        user.setGrandReferrer(referrer.getReferrer());
        user.setStatus(UserStatus.INACTIVE);
        user.setRole(Role.USER);
        
        userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(user.getPhoneNumber());
        
        return new AuthDTOs.LoginResponse(jwt, user.getId(), user.getUsername(), user.getRole().name());
    }
    
    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new AuthDTOs.LoginResponse(jwt, user.getId(), user.getUsername(), user.getRole().name());
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public UserDTOs.DashboardResponse getDashboard(User user) {
        UserDTOs.DashboardResponse response = new UserDTOs.DashboardResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setCurrentPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        response.setTotalBalance(user.getTotalBalance());
        
        // Calculate total profits (simplified - you might want to track this separately)
        BigDecimal totalProfits = user.getTotalBalance().subtract(user.getFrozenBalance());
        response.setTotalProfits(totalProfits);
        
        // Set daily profit (this would come from the current day's counter)
        response.setDailyProfit(BigDecimal.ZERO); // This would be calculated from DailyCounter
        
        // Set counter status
        UserDTOs.CounterStatus counterStatus = new UserDTOs.CounterStatus();
        counterStatus.setActive(false);
        counterStatus.setCompleted(false);
        counterStatus.setRemainingSeconds(0);
        counterStatus.setNeedsReset(false);
        response.setCounterStatus(counterStatus);
        
        // Set activation status
        response.setActivationPending(user.getStatus() == UserStatus.INACTIVE);
        if (user.getStatus() == UserStatus.INACTIVE) {
            response.setActivationMessage("Account pending admin activation");
        }
        
        return response;
    }
    
    public UserDTOs.ProfileResponse getProfile(User user) {
        UserDTOs.ProfileResponse response = new UserDTOs.ProfileResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        response.setSubscriptionDate(user.getSubscriptionDate());
        response.setTotalBalance(user.getTotalBalance());
        response.setReferralEarnings(user.getReferralEarnings());
        
        // Count referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setNumberOfReferrals(directReferrals);
        response.setSecondLevelReferrals(secondLevelReferrals);
        
        // Generate referral link
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getUsername());
        
        return response;
    }
    
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
} 