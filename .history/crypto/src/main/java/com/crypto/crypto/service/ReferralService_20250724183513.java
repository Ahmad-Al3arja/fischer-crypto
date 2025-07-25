// Enhanced ReferralService.java
package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.ReferralEarningRepository;
import com.crypto.crypto.repository.ReferralUsageRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReferralService {
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private ReferralUsageRepository referralUsageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final BigDecimal DIRECT_COMMISSION_RATE = new BigDecimal("0.12"); // 12%
    private static final BigDecimal GRAND_COMMISSION_RATE = new BigDecimal("0.06"); // 6%
    private static final int DEFAULT_REFERRAL_LIMIT = 100;
    
    public boolean canUseReferralCode(String referralCode) {
        User referrer = userRepository.findByDisplayUsername(referralCode).orElse(null);
        if (referrer == null) {
            return false;
        }
        
        ReferralUsage usage = referralUsageRepository.findByReferrer(referrer).orElse(null);
        if (usage == null) {
            // Create default usage record
            usage = createDefaultReferralUsage(referrer);
        }
        
        return usage.getIsActive() && usage.getUsageCount() < usage.getUsageLimit();
    }
    
    public void validateReferralCode(String referralCode) {
        User referrer = userRepository.findByDisplayUsername(referralCode)
                .orElseThrow(() -> new RuntimeException("Invalid referral code: " + referralCode));
        
        ReferralUsage usage = referralUsageRepository.findByReferrer(referrer).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(referrer);
        }
        
        if (!usage.getIsActive()) {
            throw new RuntimeException("This referral code has been deactivated by admin");
        }
        
        if (usage.getUsageCount() >= usage.getUsageLimit()) {
            throw new RuntimeException("This referral code has reached its usage limit (" + 
                    usage.getUsageLimit() + " referrals)");
        }
    }
    
    public void incrementReferralUsage(User referrer) {
        ReferralUsage usage = referralUsageRepository.findByReferrer(referrer).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(referrer);
        }
        
        usage.setUsageCount(usage.getUsageCount() + 1);
        referralUsageRepository.save(usage);
        
        // Notify if nearing limit
        if (usage.getUsageCount() >= usage.getUsageLimit() * 0.9) { // 90% of limit
            notifyReferrerNearingLimit(referrer, usage);
        }
    }
    
    public void processReferralCommissions(User referredUser, Deposit deposit) {
        BigDecimal depositAmount = deposit.getAmount();
        
        // Process direct referrer commission (12%)
        if (referredUser.getReferrer() != null) {
            BigDecimal directCommission = depositAmount.multiply(DIRECT_COMMISSION_RATE);
            processCommission(referredUser.getReferrer(), referredUser, deposit, 
                    directCommission, CommissionType.DIRECT);
            
            // Increment referral usage for direct referrer
            incrementReferralUsage(referredUser.getReferrer());
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
        
        // Get referral usage info
        ReferralUsage usage = referralUsageRepository.findByReferrer(user).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(user);
        }
        
        response.setReferralUsageCount(usage.getUsageCount());
        response.setReferralUsageLimit(usage.getUsageLimit());
        response.setReferralCodeActive(usage.getIsActive());
        response.setRemainingReferrals(usage.getUsageLimit() - usage.getUsageCount());
        
        return response;
    }
    
    // Admin methods for managing referral usage limits
    public AdminDTOs.ReferralUsageListResponse getAllReferralUsages() {
        List<ReferralUsage> usages = referralUsageRepository.findAllByOrderByUsageCountDesc();
        
        List<AdminDTOs.ReferralUsageSummary> summaries = usages.stream()
                .map(this::convertToReferralUsageSummary)
                .collect(Collectors.toList());
        
        return new AdminDTOs.ReferralUsageListResponse(summaries);
    }
    
    public void updateReferralUsageLimit(Long userId, Integer newLimit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (newLimit < 0 || newLimit > 10000) {
            throw new RuntimeException("Referral limit must be between 0 and 10000");
        }
        
        ReferralUsage usage = referralUsageRepository.findByReferrer(user).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(user);
        }
        
        usage.setUsageLimit(newLimit);
        referralUsageRepository.save(usage);
    }
    
    public void toggleReferralCodeStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReferralUsage usage = referralUsageRepository.findByReferrer(user).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(user);
        }
        
        usage.setIsActive(!usage.getIsActive());
        referralUsageRepository.save(usage);
    }
    
    public void resetReferralUsageCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ReferralUsage usage = referralUsageRepository.findByReferrer(user).orElse(null);
        if (usage == null) {
            usage = createDefaultReferralUsage(user);
        }
        
        usage.setUsageCount(0);
        referralUsageRepository.save(usage);
    }
    
    private ReferralUsage createDefaultReferralUsage(User referrer) {
        ReferralUsage usage = new ReferralUsage();
        usage.setReferrer(referrer);
        usage.setUsageCount(0);
        usage.setUsageLimit(DEFAULT_REFERRAL_LIMIT);
        usage.setIsActive(true);
        return referralUsageRepository.save(usage);
    }
    
    private void notifyReferrerNearingLimit(User referrer, ReferralUsage usage) {
        // Implementation for notifying user that they're nearing their referral limit
        // This could send an email, push notification, or in-app notification
        System.out.println("User " + referrer.getDisplayUsername() + 
                " is nearing their referral limit: " + usage.getUsageCount() + 
                "/" + usage.getUsageLimit());
    }
    
    private AdminDTOs.ReferralUsageSummary convertToReferralUsageSummary(ReferralUsage usage) {
        AdminDTOs.ReferralUsageSummary summary = new AdminDTOs.ReferralUsageSummary();
        summary.setUserId(usage.getReferrer().getId());
        summary.setUsername(usage.getReferrer().getDisplayUsername());
        summary.setFullName(usage.getReferrer().getFullName());
        summary.setUsageCount(usage.getUsageCount());
        summary.setUsageLimit(usage.getUsageLimit());
        summary.setIsActive(usage.getIsActive());
        summary.setRemainingReferrals(usage.getUsageLimit() - usage.getUsageCount());
        summary.setCreatedAt(usage.getCreatedAt());
        summary.setUpdatedAt(usage.getUpdatedAt());
        
        // Calculate percentage used
        double percentage = usage.getUsageLimit() > 0 ? 
                (double) usage.getUsageCount() / usage.getUsageLimit() * 100 : 0;
        summary.setUsagePercentage(Math.round(percentage * 100.0) / 100.0);
        
        return summary;
    }
}

// Enhanced ReferralUsage entity
@Entity
@Table(name = "referral_usage")
public class ReferralUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false, unique = true)
    private User referrer;
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 100;
    
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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}