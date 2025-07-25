package com.crypto.crypto.service;

import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.repository.ReferralUsageRepository;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.dto.AdminDTOs;
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
    
    public boolean canAcceptReferral(User referrer) {
        try {
            // Check if referrer has remaining usage limit
            var usage = referralUsageRepository.findByReferrer(referrer);
            if (usage.isEmpty()) {
                return true; // First time, create usage record
            }
            
            var usageRecord = usage.get();
            return usageRecord.getUsageCount() < usageRecord.getUsageLimit() && usageRecord.getIsActive();
        } catch (Exception e) {
            // Log the error and return true to allow registration
            System.err.println("Error checking referral usage for user " + referrer.getId() + ": " + e.getMessage());
            return true;
        }
    }
    
    public void incrementUsage(User referrer) {
        var usage = referralUsageRepository.findByReferrer(referrer)
            .orElse(createNewUsageRecord(referrer));
        
        usage.setUsageCount(usage.getUsageCount() + 1);
        referralUsageRepository.save(usage);
    }
    
    private ReferralUsage createNewUsageRecord(User referrer) {
        ReferralUsage usage = new ReferralUsage();
        usage.setReferrer(referrer);
        usage.setUsageCount(0);
        usage.setUsageLimit(100); // Default limit
        usage.setIsActive(true);
        return referralUsageRepository.save(usage);
    }
    
    public void updateReferralLimit(Long userId, Integer newLimit) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        var usage = referralUsageRepository.findByReferrer(user)
            .orElse(createNewUsageRecord(user));
            
        if (newLimit < usage.getUsageCount()) {
            throw new RuntimeException("New limit cannot be less than current usage count");
        }
        
        usage.setUsageLimit(newLimit);
        referralUsageRepository.save(usage);
    }
    
    public AdminDTOs.ReferralUsageResponse getUserReferralUsage(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        var usage = referralUsageRepository.findByReferrer(user)
            .orElse(createNewUsageRecord(user));
            
        AdminDTOs.ReferralUsageResponse response = new AdminDTOs.ReferralUsageResponse();
        response.setUsageCount(usage.getUsageCount());
        response.setUsageLimit(usage.getUsageLimit());
        response.setIsActive(usage.getIsActive());
        response.setRemainingCount(usage.getUsageLimit() - usage.getUsageCount());
        
        return response;
    }
} 