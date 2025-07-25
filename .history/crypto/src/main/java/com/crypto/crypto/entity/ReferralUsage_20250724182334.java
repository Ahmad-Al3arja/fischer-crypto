// crypto/src/main/java/com/crypto/crypto/entity/ReferralUsage.java
package com.crypto.crypto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "referral_usage")
public class ReferralUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 100; // Default limit
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getReferrer() { return referrer; }
    public void setReferrer(User referrer) { this.referrer = referrer; }
    
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public boolean canAcceptReferrals() {
        return isActive && usageCount < usageLimit;
    }
    
    public void incrementUsage() {
        this.usageCount++;
        if (this.usageCount >= this.usageLimit) {
            this.isActive = false;
        }
    }
}

// crypto/src/main/java/com/crypto/crypto/repository/ReferralUsageRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralUsageRepository extends JpaRepository<ReferralUsage, Long> {
    Optional<ReferralUsage> findByReferrer(User referrer);
}

// crypto/src/main/java/com/crypto/crypto/service/EnhancedReferralService.java
package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.ReferralEarningRepository;
import com.crypto.crypto.repository.ReferralUsageRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class EnhancedReferralService {
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private ReferralUsageRepository referralUsageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final BigDecimal DIRECT_COMMISSION_RATE = new BigDecimal("0.12"); // 12%
    private static final BigDecimal GRAND_COMMISSION_RATE = new BigDecimal("0.06"); // 6%
    
    public boolean validateReferralCode(String referralCode) {
        User referrer = userRepository.findByDisplayUsername(referralCode).orElse(null);
        if (referrer == null) {
            return false;
        }
        
        ReferralUsage usage = getReferralUsage(referrer);
        return usage.canAcceptReferrals();
    }
    
    public void processReferralCommissions(User referredUser, Deposit deposit) {
        BigDecimal depositAmount = deposit.getAmount();
        
        // Process direct referrer commission (12%)
        if (referredUser.getReferrer() != null) {
            // Increment referral usage
            ReferralUsage directUsage = getReferralUsage(referredUser.getReferrer());
            directUsage.incrementUsage();
            referralUsageRepository.save(directUsage);
            
            BigDecimal directCommission = depositAmount.multiply(DIRECT_COMMISSION_RATE);
            processCommission(referredUser.getReferrer(), referredUser, deposit, 
                    directCommission, CommissionType.DIRECT);
        }
        
        // Process grand referrer commission (6%)
        if (referredUser.getGrandReferrer() != null) {
            BigDecimal grandCommission = depositAmount.multiply(GRAND_COMMISSION_RATE);
            processCommission(referredUser.getGrandReferrer(), referredUser, deposit, 
                    grandCommission, CommissionType.GRAND);
        }
    }
    
    private void processCommission(User referrer, User referredUser, Deposit deposit, 
                                 BigDecimal commissionAmount, CommissionType commissionType) {
        // Create referral earning record
        ReferralEarning earning = new ReferralEarning();
        earning.setUser(referredUser);
        earning.setReferrer(referrer);
        earning.setReferredUser(referredUser);
        earning.setDeposit(deposit);
        earning.setAmount(commissionAmount);
        earning.setCommissionType(commissionType);
        
        referralEarningRepository.save(earning);
        
        // Add commission to referrer's balance and referral earnings
        referrer.setTotalBalance(referrer.getTotalBalance().add(commissionAmount));
        referrer.setReferralEarnings(referrer.getReferralEarnings().add(commissionAmount));
        
        userRepository.save(referrer);
    }
    
    private ReferralUsage getReferralUsage(User referrer) {
        return referralUsageRepository.findByReferrer(referrer)
                .orElseGet(() -> {
                    ReferralUsage usage = new ReferralUsage();
                    usage.setReferrer(referrer);
                    usage.setUsageLimit(100); // Default limit
                    usage.setUsageCount(0);
                    usage.setIsActive(true);
                    return referralUsageRepository.save(usage);
                });
    }
    
    public UserDTOs.ReferralStatsResponse getReferralStats(User user) {
        UserDTOs.ReferralStatsResponse response = new UserDTOs.ReferralStatsResponse();
        
        // Count direct referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        response.setTotalDirectReferrals(directReferrals);
        
        // Count second-level referrals
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setTotalSecondLevelReferrals(secondLevelReferrals);
        
        // Get total referral earnings
        response.setTotalReferralEarnings(user.getReferralEarnings());
        
        // Generate referral link
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        
        return response;
    }
    
    public void updateReferralLimit(Long userId, Integer newLimit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReferralUsage usage = getReferralUsage(user);
        usage.setUsageLimit(newLimit);
        
        // Reactivate if within limit
        if (usage.getUsageCount() < newLimit) {
            usage.setIsActive(true);
        }
        
        referralUsageRepository.save(usage);
    }
    
    public void resetReferralUsage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReferralUsage usage = getReferralUsage(user);
        usage.setUsageCount(0);
        usage.setIsActive(true);
        
        referralUsageRepository.save(usage);
    }
}

// Updated UserService with enhanced referral validation
// crypto/src/main/java/com/crypto/crypto/service/EnhancedUserService.java
package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
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

@Service
@Transactional
public class EnhancedUserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private EnhancedReferralService enhancedReferralService;
    
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
        if (userRepository.existsByDisplayUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        
        // Validate referral code and check usage limit
        if (!enhancedReferralService.validateReferralCode(request.getReferralCode())) {
            throw new RuntimeException("Invalid referral code or referral limit exceeded");
        }
        
        // Find referrer
        User referrer = userRepository.findByDisplayUsername(request.getReferralCode())
                .orElseThrow(() -> new RuntimeException("Invalid referral code: " + request.getReferralCode()));
        
        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setDisplayUsername(request.getUsername());
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
    
    // ... other methods remain the same
}