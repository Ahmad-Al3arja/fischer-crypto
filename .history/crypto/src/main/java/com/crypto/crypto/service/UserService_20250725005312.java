package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.exception.CustomExceptions;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.repository.ReferralEarningRepository;
import com.crypto.crypto.repository.DepositRepository;
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
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Autowired
    private DailyCounterService dailyCounterService;
    
    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomExceptions.PasswordMismatchException();
        }
        
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new CustomExceptions.DuplicatePhoneException(request.getPhoneNumber());
        }
        
        // Check if username already exists
        if (userRepository.existsByDisplayUsername(request.getUsername())) {
            throw new CustomExceptions.DuplicateUsernameException(request.getUsername());
        }
        
        // Find referrer
        System.out.println("Looking for referral code: " + request.getReferralCode());
        User referrer = userRepository.findByDisplayUsername(request.getReferralCode())
                .orElseThrow(() -> new CustomExceptions.InvalidReferralCodeException(request.getReferralCode()));
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setDisplayUsername(request.getUsername());  // Fixed
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setReferrer(referrer);
        user.setGrandReferrer(referrer.getReferrer());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);
        
        userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(user.getPhoneNumber());
        
        return new AuthDTOs.LoginResponse(jwt, user.getId(), user.getDisplayUsername(), user.getRole().name());
    }
    
    public AuthDTOs.LoginResponse login(AuthDTOs.LoginRequest request) {
        // First check if user exists and is active
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new CustomExceptions.InvalidCredentialsException());
        
        // Check if account is suspended
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new CustomExceptions.AccountSuspendedException();
        }
        
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        return new AuthDTOs.LoginResponse(jwt, user.getId(), user.getDisplayUsername(), user.getRole().name());
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public UserDTOs.DashboardResponse getDashboard(User user) {
        UserDTOs.DashboardResponse response = new UserDTOs.DashboardResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getDisplayUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setCurrentPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        response.setTotalBalance(user.getTotalBalance());
        
        // Calculate total profits (total balance minus frozen balance)
        BigDecimal totalProfits = user.getTotalBalance().subtract(user.getFrozenBalance());
        response.setTotalProfits(totalProfits);
        
        // Get daily profit from counter service
        BigDecimal dailyProfit = dailyCounterService.getCurrentDayProfit(user);
        response.setDailyProfit(dailyProfit);
        
        // Get counter status from counter service
        UserDTOs.CounterStatus counterStatus = dailyCounterService.getCounterStatus(user);
        response.setCounterStatus(counterStatus);
        
        // Set activation status
        response.setActivationPending(false);
        if (user.getStatus() == UserStatus.SUSPENDED) {
            response.setActivationMessage("Account suspended");
        }
        
        return response;
    }
    
    public UserDTOs.ProfileResponse getProfile(User user) {
        UserDTOs.ProfileResponse response = new UserDTOs.ProfileResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getDisplayUsername());
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
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        
        return response;
    }
    
    // Admin methods
    
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }
    
    public void updateUserBalance(Long userId, BigDecimal amount, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setTotalBalance(user.getTotalBalance().add(amount));
        userRepository.save(user);
        
        // TODO: Log the balance update in audit log
    }
    
    public AdminDTOs.UserListResponse getAllUsers(Long planId) {
        List<User> users;
        if (planId != null) {
            users = userRepository.findByCurrentPlanId(planId);
        } else {
            users = userRepository.findAll();
        }
        
        List<AdminDTOs.UserSummary> userSummaries = users.stream()
                .map(this::convertToUserSummary)
                .collect(Collectors.toList());
        
        return new AdminDTOs.UserListResponse(userSummaries);
    }
    
    public AdminDTOs.UserDetailsResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToUserDetails(user);
    }
    
    private AdminDTOs.UserSummary convertToUserSummary(User user) {
        AdminDTOs.UserSummary summary = new AdminDTOs.UserSummary();
        summary.setId(user.getId());
        summary.setFullName(user.getFullName());
        summary.setUsername(user.getDisplayUsername());
        summary.setPhoneNumber(user.getPhoneNumber());
        summary.setPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        summary.setTotalBalance(user.getTotalBalance());
        summary.setStatus(user.getStatus().name());
        summary.setCreatedAt(user.getCreatedAt());
        return summary;
    }
    
    private AdminDTOs.UserDetailsResponse convertToUserDetails(User user) {
        AdminDTOs.UserDetailsResponse details = new AdminDTOs.UserDetailsResponse();
        details.setId(user.getId());
        details.setFullName(user.getFullName());
        details.setUsername(user.getDisplayUsername());
        details.setPhoneNumber(user.getPhoneNumber());
        details.setPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        details.setTotalBalance(user.getTotalBalance());
        details.setFrozenBalance(user.getFrozenBalance());
        details.setReferralEarnings(user.getReferralEarnings());
        details.setStatus(user.getStatus().name());
        details.setSubscriptionDate(user.getSubscriptionDate());
        details.setCreatedAt(user.getCreatedAt());
        
        // Count referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        details.setDirectReferrals(directReferrals);
        details.setSecondLevelReferrals(secondLevelReferrals);
        
        return details;
    }
    
    public UserDTOs.TeamStatsResponse getTeamStats(User user) {
        UserDTOs.TeamStatsResponse response = new UserDTOs.TeamStatsResponse();
        
        // Get direct referrals
        List<User> directReferrals = userRepository.findByReferrer(user);
        int directReferralsCount = directReferrals.size();
        
        // Get second level referrals
        List<User> secondLevelReferrals = userRepository.findByGrandReferrer(user);
        int secondLevelReferralsCount = secondLevelReferrals.size();
        
        // Calculate total referral earnings
        BigDecimal totalEarnings = BigDecimal.ZERO;
        List<UserDTOs.ReferralDetail> recentReferrals = new ArrayList<>();
        
        // Process direct referrals
        for (User referral : directReferrals) {
            UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
            detail.setUsername(referral.getDisplayUsername());
            detail.setPhoneNumber(referral.getPhoneNumber());
            detail.setPlanName(referral.getCurrentPlan() != null ? referral.getCurrentPlan().getName() : "No Plan");
            detail.setInvestmentAmount(referral.getTotalBalance());
            detail.setCommissionEarned(BigDecimal.ZERO); // Calculate based on your commission logic
            detail.setLevel("DIRECT");
            detail.setJoinedAt(referral.getCreatedAt());
            recentReferrals.add(detail);
        }
        
        // Process second level referrals
        for (User referral : secondLevelReferrals) {
            UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
            detail.setUsername(referral.getDisplayUsername());
            detail.setPhoneNumber(referral.getPhoneNumber());
            detail.setPlanName(referral.getCurrentPlan() != null ? referral.getCurrentPlan().getName() : "No Plan");
            detail.setInvestmentAmount(referral.getTotalBalance());
            detail.setCommissionEarned(BigDecimal.ZERO); // Calculate based on your commission logic
            detail.setLevel("GRAND");
            detail.setJoinedAt(referral.getCreatedAt());
            recentReferrals.add(detail);
        }
        
        // Sort by join date (most recent first) and limit to 10
        recentReferrals.sort((a, b) -> b.getJoinedAt().compareTo(a.getJoinedAt()));
        if (recentReferrals.size() > 10) {
            recentReferrals = recentReferrals.subList(0, 10);
        }
        
        response.setDirectReferrals(directReferralsCount);
        response.setSecondLevelReferrals(secondLevelReferralsCount);
        response.setTotalReferrals(directReferralsCount + secondLevelReferralsCount);
        response.setTotalReferralEarnings(totalEarnings);
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        response.setReferralCode(user.getDisplayUsername());
        response.setRecentReferrals(recentReferrals);
        
        return response;
    }
}