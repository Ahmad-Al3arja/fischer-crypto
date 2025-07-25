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
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private DepositRepository depositRepository;
    
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
        
        // Count direct referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        response.setDirectReferrals(directReferrals);
        
        // Count second-level referrals
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setSecondLevelReferrals(secondLevelReferrals);
        
        // Total referrals
        response.setTotalReferrals(directReferrals + secondLevelReferrals);
        
        // Total referral earnings
        response.setTotalReferralEarnings(user.getReferralEarnings() != null ? user.getReferralEarnings() : BigDecimal.ZERO);
        
        // Referral info
        response.setReferralCode(user.getDisplayUsername());
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        
        // Get recent referrals with their details
        List<UserDTOs.ReferralDetail> recentReferrals = getRecentReferralDetails(user);
        response.setRecentReferrals(recentReferrals);
        
        return response;
    }
    
    private List<UserDTOs.ReferralDetail> getRecentReferralDetails(User user) {
        List<UserDTOs.ReferralDetail> details = new ArrayList<>();
        
        // Get direct referrals
        List<User> directReferrals = userRepository.findByReferrer(user);
        for (User referredUser : directReferrals) {
            UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
            detail.setUsername(referredUser.getDisplayUsername());
            detail.setPhoneNumber(referredUser.getPhoneNumber());
            detail.setPlanName(referredUser.getCurrentPlan() != null ? referredUser.getCurrentPlan().getName() : "لا توجد خطة");
            detail.setLevel("مباشر");
            detail.setJoinedAt(referredUser.getCreatedAt());
            
            // Get commission earned from this referral
            BigDecimal commission = referralEarningRepository.findByReferrer(user)
                    .stream()
                    .filter(earning -> earning.getReferredUser().equals(referredUser))
                    .map(ReferralEarning::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.setCommissionEarned(commission);
            
            // Get their investment amount (first approved deposit)
            Optional<Deposit> firstDeposit = depositRepository.findFirstApprovedDeposit(referredUser);
            detail.setInvestmentAmount(firstDeposit.map(Deposit::getAmount).orElse(BigDecimal.ZERO));
            
            details.add(detail);
        }
        
        // Get second-level referrals
        List<User> secondLevelReferrals = userRepository.findByGrandReferrer(user);
        for (User grandReferredUser : secondLevelReferrals) {
            UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
            detail.setUsername(grandReferredUser.getDisplayUsername());
            detail.setPhoneNumber(grandReferredUser.getPhoneNumber());
            detail.setPlanName(grandReferredUser.getCurrentPlan() != null ? grandReferredUser.getCurrentPlan().getName() : "لا توجد خطة");
            detail.setLevel("المستوى الثاني");
            detail.setJoinedAt(grandReferredUser.getCreatedAt());
            
            // Get commission earned from this grand referral
            BigDecimal commission = referralEarningRepository.findByReferrer(user)
                    .stream()
                    .filter(earning -> earning.getReferredUser().equals(grandReferredUser))
                    .map(ReferralEarning::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            detail.setCommissionEarned(commission);
            
            // Get their investment amount
            Optional<Deposit> firstDeposit = depositRepository.findFirstApprovedDeposit(grandReferredUser);
            detail.setInvestmentAmount(firstDeposit.map(Deposit::getAmount).orElse(BigDecimal.ZERO));
            
            details.add(detail);
        }
        
        // Sort by join date (most recent first)
        details.sort((a, b) -> b.getJoinedAt().compareTo(a.getJoinedAt()));
        
        // Return only the most recent 20 referrals
        return details.stream().limit(20).collect(Collectors.toList());
    }
}