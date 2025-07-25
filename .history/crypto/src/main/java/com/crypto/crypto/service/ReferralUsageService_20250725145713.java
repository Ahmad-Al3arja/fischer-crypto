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
            var usage = referralUsageRepository.findByReferrer(referrer)
                .orElse(createNewUsageRecord(referrer));
            usage.setUsageCount(usage.getUsageCount() + 1);
            referralUsageRepository.save(usage);
            System.out.println("[incrementUsage] Usage incremented to: " + usage.getUsageCount());
        } catch (Exception e) {
            System.err.println("[incrementUsage] Error: " + e.getMessage());
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