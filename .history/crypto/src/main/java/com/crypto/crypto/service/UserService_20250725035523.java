package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.dto.AdminDTOs;
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
import java.util.List;
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
    private PlanService planService;
    
    @Autowired
    private ReferralService referralService;
    
    public AuthDTOs.AuthResponse register(AuthDTOs.RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }
        
        // Check if username already exists
        if (userRepository.existsByDisplayUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Find referrer
        User referrer = userRepository.findByDisplayUsername(request.getReferralCode())
                .orElseThrow(() -> new RuntimeException("Invalid referral code"));
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setDisplayUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setReferrer(referrer);
        user.setGrandReferrer(referrer.getReferrer());
        user.setStatus(UserStatus.INACTIVE);
        user.setRole(Role.USER);
        
        userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(user.getPhoneNumber());
        
        return new AuthDTOs.AuthResponse(jwt, user.getId(), user.getDisplayUsername(), user.getRole().name());
    }
    
    public AuthDTOs.AuthResponse login(AuthDTOs.LoginRequest request) {
        // First check if user exists and is active
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Invalid phone number or password"));
        
        // Check if account is activated
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Your account is under review");
        }
        
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        return new AuthDTOs.AuthResponse(jwt, user.getId(), user.getDisplayUsername(), user.getRole().name());
    }
    
    public AuthDTOs.ProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        AuthDTOs.ProfileResponse response = new AuthDTOs.ProfileResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setUsername(user.getDisplayUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole().name());
        response.setStatus(user.getStatus().name());
        return response;
    }
    
    public void logout() {
        SecurityContextHolder.clearContext();
    }
    
    public void changePassword(AuthDTOs.ChangePasswordRequest request) {
        User user = getCurrentUser();
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Verify new passwords match
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public UserDTOs.DashboardResponse getDashboard() {
        User user = getCurrentUser();
        return getDashboard(user);
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
        BigDecimal dailyProfit = BigDecimal.ZERO; // Will be implemented later
        response.setDailyProfit(dailyProfit);
        
        // Get counter status from counter service
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
        } else if (user.getStatus() == UserStatus.SUSPENDED) {
            response.setActivationMessage("Account suspended");
        }
        
        // Set navigation links
        UserDTOs.NavigationLinks navigationLinks = new UserDTOs.NavigationLinks();
        response.setNavigationLinks(navigationLinks);
        
        return response;
    }
    
    public UserDTOs.ProfileResponse getUserProfile() {
        User user = getCurrentUser();
        return getProfile(user);
    }
    
    public UserDTOs.ProfileResponse updateProfile(UserDTOs.UpdateProfileRequest request) {
        User user = getCurrentUser();
        
        // Check if username is already taken by another user
        if (!user.getDisplayUsername().equals(request.getUsername())) {
            if (userRepository.existsByDisplayUsername(request.getUsername())) {
                throw new RuntimeException("Username already taken");
            }
            user.setDisplayUsername(request.getUsername());
        }
        
        user.setFullName(request.getFullName());
        userRepository.save(user);
        
        return getProfile(user);
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
        int directReferrals = userRepository.countByReferrer(user);
        int secondLevelReferrals = userRepository.countByGrandReferrer(user);
        response.setNumberOfReferrals(directReferrals);
        response.setSecondLevelReferrals(secondLevelReferrals);
        
        // Generate referral link
        response.setReferralLink("/register?ref=" + user.getDisplayUsername());
        
        return response;
    }
    
    public UserDTOs.TeamStatsResponse getTeamStats(User user) {
        UserDTOs.TeamStatsResponse response = new UserDTOs.TeamStatsResponse();
        
        // Get direct referrals
        List<User> directReferrals = userRepository.findByReferrer(user);
        response.setDirectReferrals(directReferrals.size());
        
        // Get second level referrals
        List<User> secondLevelReferrals = userRepository.findByGrandReferrer(user);
        response.setSecondLevelReferrals(secondLevelReferrals.size());
        
        response.setTotalReferrals(directReferrals.size() + secondLevelReferrals.size());
        response.setTotalReferralEarnings(user.getReferralEarnings());
        response.setReferralLink("/register?ref=" + user.getDisplayUsername());
        response.setReferralCode(user.getDisplayUsername());
        
        // Get recent referrals (last 10)
        List<UserDTOs.ReferralDetail> recentReferrals = directReferrals.stream()
                .limit(10)
                .map(this::convertToReferralDetail)
                .collect(Collectors.toList());
        response.setRecentReferrals(recentReferrals);
        
        return response;
    }
    
    // Admin methods
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    
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
        
        // Log the transaction
        // TODO: Add transaction logging
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
        int directReferrals = userRepository.countByReferrer(user);
        int secondLevelReferrals = userRepository.countByGrandReferrer(user);
        details.setDirectReferrals(directReferrals);
        details.setSecondLevelReferrals(secondLevelReferrals);
        
        return details;
    }
    
    private UserDTOs.ReferralDetail convertToReferralDetail(User referral) {
        UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
        detail.setUsername(referral.getDisplayUsername());
        detail.setPhoneNumber(referral.getPhoneNumber());
        detail.setPlanName(referral.getCurrentPlan() != null ? referral.getCurrentPlan().getName() : "No Plan");
        detail.setInvestmentAmount(referral.getTotalBalance());
        detail.setCommissionEarned(BigDecimal.ZERO); // TODO: Calculate actual commission
        detail.setLevel("DIRECT");
        detail.setJoinedAt(referral.getCreatedAt());
        return detail;
    }
} 