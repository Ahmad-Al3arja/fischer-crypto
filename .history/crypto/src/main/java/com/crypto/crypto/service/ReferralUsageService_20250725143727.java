package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.repository.ReferralUsageRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReferralUsageService {
    
    @Autowired
    private ReferralUsageRepository referralUsageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminSettingsService adminSettingsService;
    
    public boolean canAcceptReferral(User referrer) {
        try {
            System.out.println("Checking referral usage for user: " + referrer.getDisplayUsername());
            
            // Check if referrer has remaining usage limit
            var usage = referralUsageRepository.findByReferrer(referrer);
            if (usage.isEmpty()) {
                System.out.println("No usage record found - creating new one for user: " + referrer.getId());
                // First time, create usage record but don't save yet - just check
                return true;
            }
            
            var usageRecord = usage.get();
            boolean canAccept = usageRecord.getUsageCount() < usageRecord.getUsageLimit() && usageRecord.getIsActive();
            
            System.out.println("Usage check result: " + canAccept + 
                             " (count: " + usageRecord.getUsageCount() + 
                             ", limit: " + usageRecord.getUsageLimit() + 
                             ", active: " + usageRecord.getIsActive() + ")");
            
            return canAccept;
            
        } catch (Exception e) {
            // Log the error and return true to allow registration
            System.err.println("Error checking referral usage for user " + referrer.getId() + ": " + e.getMessage());
            e.printStackTrace();
            System.out.println("Allowing referral due to error in usage check");
            return true;
        }
    }
    
    public void incrementUsage(User referrer) {
        try {
            System.out.println("Incrementing referral usage for user: " + referrer.getDisplayUsername() + " (ID: " + referrer.getId() + ")");
            
            // Ensure the referrer is properly loaded and has an ID
            if (referrer.getId() == null) {
                System.err.println("Warning: Referrer ID is null, cannot increment usage");
                return;
            }
            
            var usage = referralUsageRepository.findByReferrer(referrer)
                .orElseGet(() -> {
                    System.out.println("Creating new usage record for referrer: " + referrer.getId());
                    return createNewUsageRecord(referrer);
                });
            
            int oldCount = usage.getUsageCount();
            usage.setUsageCount(usage.getUsageCount() + 1);
            
            ReferralUsage savedUsage = referralUsageRepository.save(usage);
            System.out.println("✓ Referral usage incremented from " + oldCount + " to " + savedUsage.getUsageCount());
            
        } catch (Exception e) {
            // Log the error but don't fail the registration
            System.err.println("Error incrementing referral usage for user " + referrer.getId() + ": " + e.getMessage());
            e.printStackTrace();
            System.out.println("⚠ Registration will continue despite referral usage increment error");
        }
    }
    
    private ReferralUsage createNewUsageRecord(User referrer) {
        try {
            System.out.println("Creating new referral usage record for user: " + referrer.getId());
            
            ReferralUsage usage = new ReferralUsage();
            usage.setReferrer(referrer);
            usage.setUsageCount(0);
            
            // Get default limit from settings or use fallback
            int defaultLimit = getDefaultUsageLimit();
            usage.setUsageLimit(defaultLimit);
            usage.setIsActive(true);
            
            ReferralUsage saved = referralUsageRepository.save(usage);
            System.out.println("✓ New referral usage record created with limit: " + defaultLimit);
            
            return saved;
            
        } catch (Exception e) {
            System.err.println("Error creating new usage record for user " + referrer.getId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return a default record without saving if there's an error
            ReferralUsage fallback = new ReferralUsage();
            fallback.setReferrer(referrer);
            fallback.setUsageCount(0);
            fallback.setUsageLimit(100); // Fallback default
            fallback.setIsActive(true);
            
            return fallback;
        }
    }
    
    private int getDefaultUsageLimit() {
        try {
            return adminSettingsService.getIntegerSetting("default_usage_limit", 100);
        } catch (Exception e) {
            System.err.println("Error getting default usage limit from settings: " + e.getMessage());
            return 100; // Fallback default
        }
    }
    
    public void updateReferralLimit(Long userId, Integer newLimit) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            var usage = referralUsageRepository.findByReferrer(user)
                .orElseGet(() -> createNewUsageRecord(user));
                
            if (newLimit < usage.getUsageCount()) {
                throw new RuntimeException("New limit cannot be less than current usage count");
            }
            
            usage.setUsageLimit(newLimit);
            referralUsageRepository.save(usage);
            
        } catch (Exception e) {
            System.err.println("Error updating referral limit for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to update referral limit: " + e.getMessage());
        }
    }
    
    public AdminDTOs.ReferralUsageResponse getUserReferralUsage(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
            var usage = referralUsageRepository.findByReferrer(user)
                .orElseGet(() -> createNewUsageRecord(user));
                
            AdminDTOs.ReferralUsageResponse response = new AdminDTOs.ReferralUsageResponse();
            response.setUsageCount(usage.getUsageCount());
            response.setUsageLimit(usage.getUsageLimit());
            response.setIsActive(usage.getIsActive());
            response.setRemainingCount(usage.getUsageLimit() - usage.getUsageCount());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error getting referral usage for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to get referral usage: " + e.getMessage());
        }
    }
} 