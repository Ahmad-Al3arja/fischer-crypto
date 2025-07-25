// Enhanced UserService.java
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
import java.util.regex.Pattern;

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
    private ReferralService referralService;
    
    // Enhanced phone number patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[1-9]\\d{7,14}$"); // 8-15 digits, no leading zero
    private static final Pattern PHONE_WITH_PLUS_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$"); // With + prefix
    
    public AuthDTOs.LoginResponse register(AuthDTOs.RegisterRequest request) {
        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Enhanced phone number processing
        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        
        // Validate phone number format
        if (!isValidPhoneNumber(normalizedPhone)) {
            throw new RuntimeException("Invalid phone number format. Please enter a valid international phone number (8-15 digits)");
        }
        
        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new RuntimeException("This phone number is already registered");
        }
        
        // Validate username
        if (!isValidUsername(request.getUsername())) {
            throw new RuntimeException("Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
        
        // Check if username already exists
        if (userRepository.existsByDisplayUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }
        
        // Validate and process referral code
        referralService.validateReferralCode(request.getReferralCode());
        User referrer = userRepository.findByDisplayUsername(request.getReferralCode())
                .orElseThrow(() -> new RuntimeException("Invalid referral code: " + request.getReferralCode()));
        
        // Validate password strength
        if (!isValidPassword(request.getPassword())) {
            throw new RuntimeException("Password must be at least 6 characters long and contain at least one letter and one number");
        }
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setDisplayUsername(request.getUsername().toLowerCase().trim());
        user.setPhoneNumber(normalizedPhone);
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
        // Normalize phone number for login
        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        
        // First check if user exists
        User user = userRepository.findByPhoneNumber(normalizedPhone).orElse(null);
        if (user == null) {
            // Try with original phone number format
            user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                    .orElseThrow(() -> new RuntimeException("Invalid phone number or password"));
        }
        
        // Check if account is suspended
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Your account has been suspended. Please contact admin for assistance.");
        }
        
        // Check if account is inactive
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Your account is inactive. Please contact support.");
        }
        
        try {
            // Authenticate credentials using the actual phone number stored in database
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), request.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            // Update last login time (if you want to track this)
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return new AuthDTOs.LoginResponse(jwt, user.getId(), user.getDisplayUsername(), user.getRole().name());
        } catch (Exception e) {
            throw new RuntimeException("Invalid phone number or password");
        }
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("Phone number cannot be empty");
        }
        
        // Remove all spaces, dashes, and parentheses
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Handle different formats
        if (cleaned.startsWith("+")) {
            // Remove + and return
            return cleaned.substring(1);
        } else if (cleaned.startsWith("00")) {
            // Remove 00 prefix (international format)
            return cleaned.substring(2);
        } else if (cleaned.startsWith("0")) {
            // Remove leading zero for local numbers and assume it needs country code
            throw new RuntimeException("Please include country code in your phone number (e.g., 1234567890 or +1234567890)");
        }
        
        return cleaned;
    }
    
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Check if it matches the pattern: 8-15 digits, no leading zero
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
    
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        
        // Check length
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return false;
        }
        
        // Check pattern: letters, numbers, underscores only
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        if (!pattern.matcher(trimmed).matches()) {
            return false;
        }
        
        // Must start with letter or number (not underscore)
        if (trimmed.startsWith("_")) {
            return false;
        }
        
        // Cannot be all numbers
        if (trimmed.matches("^\\d+$")) {
            return false;
        }
        
        return true;
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Must contain at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        
        return hasLetter && hasNumber;
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName();
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("User session expired. Please login again."));
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public UserDTOs.DashboardResponse getDashboard(User user) {
        UserDTOs.DashboardResponse response = new UserDTOs.DashboardResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getDisplayUsername());
        response.setPhoneNumber(formatPhoneNumberForDisplay(user.getPhoneNumber()));
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
        response.setActivationPending(user.getStatus() == UserStatus.INACTIVE);
        if (user.getStatus() == UserStatus.SUSPENDED) {
            response.setActivationMessage("Account suspended - contact admin");
        } else if (user.getStatus() == UserStatus.INACTIVE) {
            response.setActivationMessage("Account inactive - make a deposit to activate");
        }
        
        return response;
    }
    
    public UserDTOs.ProfileResponse getProfile(User user) {
        UserDTOs.ProfileResponse response = new UserDTOs.ProfileResponse();
        response.setFullName(user.getFullName());
        response.setUsername(user.getDisplayUsername());
        response.setPhoneNumber(formatPhoneNumberForDisplay(user.getPhoneNumber()));
        response.setPlanName(user.getCurrentPlan() != null ? user.getCurrentPlan().getName() : "No Plan");
        response.setSubscriptionDate(user.getSubscriptionDate());
        response.setTotalBalance(user.getTotalBalance());
        response.setReferralEarnings(user.getReferralEarnings());
        
        // Count referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setNumberOfReferrals(directReferrals);
        response.setSecondLevelReferrals(secondLevelReferrals);
        
        // Generate referral link and info
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        response.setReferralCode(user.getDisplayUsername());
        
        // Get referral usage info
        UserDTOs.ReferralStatsResponse referralStats = referralService.getReferralStats(user);
        response.setReferralUsageCount(referralStats.getReferralUsageCount());
        response.setReferralUsageLimit(referralStats.getReferralUsageLimit());
        response.setRemainingReferrals(referralStats.getRemainingReferrals());
        
        return response;
    }
    
    private String formatPhoneNumberForDisplay(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }
        
        // Add + prefix for display if not present
        if (!phoneNumber.startsWith("+")) {
            return "+" + phoneNumber;
        }
        
        return phoneNumber;
    }
    
    // Admin methods (existing methods with enhancements)
    
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Cannot suspend admin users");
        }
        
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);
    }
    
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    
    public void updateUserBalance(Long userId, BigDecimal amount, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BigDecimal oldBalance = user.getTotalBalance();
        user.setTotalBalance(user.getTotalBalance().add(amount));
        
        if (user.getTotalBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Cannot set negative balance");
        }
        
        userRepository.save(user);
        
        // TODO: Log the balance update in audit log with reason
        System.out.println("Admin updated user " + user.getId() + " balance: " + 
                oldBalance + " -> " + user.getTotalBalance() + ". Reason: " + reason);
    }
    
    public AdminDTOs.UserListResponse getAllUsers(Long planId) {
        List<User> users;
        if (planId != null) {
            users = userRepository.findByCurrentPlanId(planId);
        } else {
            users = userRepository.findByRoleOrderByCreatedAtDesc(Role.USER);
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
    
    public AdminDTOs.UserSearchResponse searchUsers(String query) {
        List<User> users = userRepository.findByDisplayUsernameContainingOrFullNameContainingOrPhoneNumberContaining(
                query, query, query);
        
        List<AdminDTOs.UserSummary> userSummaries = users.stream()
                .map(this::convertToUserSummary)
                .collect(Collectors.toList());
        
        return new AdminDTOs.UserSearchResponse(userSummaries, query);
    }
    
    private AdminDTOs.UserSummary convertToUserSummary(User user) {
        AdminDTOs.UserSummary summary = new AdminDTOs.UserSummary();
        summary.setId(user.getId());
        summary.setFullName(user.getFullName());
        summary.setUsername(user.getDisplayUsername());
        summary.setPhoneNumber(formatPhoneNumberForDisplay(user.getPhoneNumber()));
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
        details.setPhoneNumber(formatPhoneNumberForDisplay(user.getPhoneNumber()));
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
        
        // Add referrer information
        if (user.getReferrer() != null) {
            details.setReferrerUsername(user.getReferrer().getDisplayUsername());
            details.setReferrerPhone(formatPhoneNumberForDisplay(user.getReferrer().getPhoneNumber()));
        }
        
        return details;
    }
}