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
        System.out.println("[canAcceptReferral] Checking for referrer: " + (referrer != null ? referrer.getId() : null));
        try {
            var usage = referralUsageRepository.findByReferrer(referrer);
            if (usage.isEmpty()) {
                System.out.println("[canAcceptReferral] No usage record, can accept");
                return true; // First time, create usage record
            }
            var usageRecord = usage.get();
            boolean canAccept = usageRecord.getUsageCount() < usageRecord.getUsageLimit() && usageRecord.getIsActive();
            System.out.println("[canAcceptReferral] Usage count: " + usageRecord.getUsageCount() + ", Limit: " + usageRecord.getUsageLimit() + ", Active: " + usageRecord.getIsActive() + ", Can accept: " + canAccept);
            return canAccept;
        } catch (Exception e) {
            System.err.println("[canAcceptReferral] Error: " + e.getMessage());
            e.printStackTrace();
            return true; // Allow referral if there's an error
        }
    }
    
    public void incrementUsage(User referrer) {
        System.out.println("[incrementUsage] For referrer: " + (referrer != null ? referrer.getId() : null));
        try {
            // First try to find existing usage record
            var usageOpt = referralUsageRepository.findByReferrer(referrer);
            
            ReferralUsage usage;
            if (usageOpt.isPresent()) {
                // Use existing record
                usage = usageOpt.get();
                System.out.println("[incrementUsage] Found existing usage record, count: " + usage.getUsageCount());
            } else {
                // Create new record only if one doesn't exist
                System.out.println("[incrementUsage] No existing record found, creating new one");
                usage = createNewUsageRecord(referrer);
            }
            
            int oldCount = usage.getUsageCount();
            usage.setUsageCount(usage.getUsageCount() + 1);
            ReferralUsage savedUsage = referralUsageRepository.save(usage);
            System.out.println("[incrementUsage] Usage incremented to: " + savedUsage.getUsageCount());
            
        } catch (Exception e) {
            System.err.println("[incrementUsage] Error: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - just log and continue
        }
    }
    
    private ReferralUsage createNewUsageRecord(User referrer) {
        try {
            System.out.println("Creating new referral usage record for user: " + referrer.getId());
            
            // Double-check that no record exists before creating
            var existingOpt = referralUsageRepository.findByReferrer(referrer);
            if (existingOpt.isPresent()) {
                System.out.println("Record already exists, returning existing one");
                return existingOpt.get();
            }
            
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
            
            // Try to get existing record if creation failed
            try {
                var existingOpt = referralUsageRepository.findByReferrer(referrer);
                if (existingOpt.isPresent()) {
                    System.out.println("Found existing record after creation error, using it");
                    return existingOpt.get();
                }
            } catch (Exception ex) {
                System.err.println("Could not retrieve existing record: " + ex.getMessage());
            }
            
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