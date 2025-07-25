// crypto/src/main/java/com/crypto/crypto/controller/EnhancedAdminController.java
package com.crypto.crypto.controller;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.PromoCodeService;
import com.crypto.crypto.service.AdminSettingsService;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.WalletService;
import com.crypto.crypto.service.EnhancedReferralService;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.repository.ReferralUsageRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class EnhancedAdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EnhancedReferralService enhancedReferralService;
    
    @Autowired
    private ReferralUsageRepository referralUsageRepository;
    
    @Autowired
    private DepositService depositService;
    
    @Autowired
    private WithdrawalService withdrawalService;
    
    @Autowired
    private PromoCodeService promoCodeService;
    
    @Autowired
    private AdminSettingsService adminSettingsService;
    
    @Autowired
    private DailyCounterService dailyCounterService;
    
    @Autowired
    private WalletService walletService;
    
    // Existing methods remain the same...
    
    // New Referral Management Endpoints
    
    @GetMapping("/referrals")
    public ResponseEntity<?> getAllReferralUsage() {
        try {
            List<ReferralUsage> referralUsages = referralUsageRepository.findAll();
            
            List<ReferralUsageResponse> responses = referralUsages.stream()
                    .map(this::convertToReferralUsageResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ReferralUsageListResponse(responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/{userId}/update-limit")
    public ResponseEntity<?> updateReferralLimit(@PathVariable Long userId, @RequestBody UpdateReferralLimitRequest request) {
        try {
            if (request.getNewLimit() < 0 || request.getNewLimit() > 1000) {
                throw new RuntimeException("Referral limit must be between 0 and 1000");
            }
            
            enhancedReferralService.updateReferralLimit(userId, request.getNewLimit());
            return ResponseEntity.ok(new SuccessResponse("Referral limit updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/{userId}/reset")
    public ResponseEntity<?> resetReferralUsage(@PathVariable Long userId) {
        try {
            enhancedReferralService.resetReferralUsage(userId);
            return ResponseEntity.ok(new SuccessResponse("Referral usage reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/referrals/{userId}")
    public ResponseEntity<?> getReferralUsageDetails(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            ReferralUsage usage = referralUsageRepository.findByReferrer(user)
                    .orElseThrow(() -> new RuntimeException("Referral usage not found"));
            
            ReferralUsageResponse response = convertToReferralUsageResponse(usage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/bulk-update")
    public ResponseEntity<?> bulkUpdateReferralLimits(@RequestBody BulkUpdateReferralLimitRequest request) {
        try {
            for (Long userId : request.getUserIds()) {
                enhancedReferralService.updateReferralLimit(userId, request.getNewLimit());
            }
            return ResponseEntity.ok(new SuccessResponse("Bulk referral limit update completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Existing methods...
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) Long planId) {
        try {
            AdminDTOs.UserListResponse response = userService.getAllUsers(planId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        try {
            AdminDTOs.UserDetailsResponse response = userService.getUserDetails(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Long userId) {
        try {
            userService.suspendUser(userId);
            return ResponseEntity.ok(new SuccessResponse("User suspended successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/balance")
    public ResponseEntity<?> updateUserBalance(@PathVariable Long userId, @RequestBody BalanceUpdateRequest request) {
        try {
            userService.updateUserBalance(userId, request.getAmount(), request.getReason());
            return ResponseEntity.ok(new SuccessResponse("User balance updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Existing deposit and withdrawal methods...
    @GetMapping("/deposits")
    public ResponseEntity<?> getAllDeposits(@RequestParam(required = false) String status) {
        try {
            AdminDTOs.DepositListResponse response = depositService.getAllDeposits(status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/deposits/{depositId}/approve")
    public ResponseEntity<?> approveDeposit(@PathVariable Long depositId) {
        try {
            depositService.approveDeposit(depositId);
            return ResponseEntity.ok(new SuccessResponse("Deposit approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/deposits/{depositId}/reject")
    public ResponseEntity<?> rejectDeposit(@PathVariable Long depositId) {
        try {
            depositService.rejectDeposit(depositId);
            return ResponseEntity.ok(new SuccessResponse("Deposit rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Helper methods
    private ReferralUsageResponse convertToReferralUsageResponse(ReferralUsage usage) {
        ReferralUsageResponse response = new ReferralUsageResponse();
        response.setId(usage.getId());
        response.setUserId(usage.getReferrer().getId());
        response.setUsername(usage.getReferrer().getDisplayUsername());
        response.setFullName(usage.getReferrer().getFullName());
        response.setUsageCount(usage.getUsageCount());
        response.setUsageLimit(usage.getUsageLimit());
        response.setIsActive(usage.getIsActive());
        response.setCreatedAt(usage.getCreatedAt());
        response.setUpdatedAt(usage.getUpdatedAt());
        return response;
    }

    // Request/Response classes
    private static class UpdateReferralLimitRequest {
        private Integer newLimit;
        
        public Integer getNewLimit() { return newLimit; }
        public void setNewLimit(Integer newLimit) { this.newLimit = newLimit; }
    }
    
    private static class BulkUpdateReferralLimitRequest {
        private List<Long> userIds;
        private Integer newLimit;
        
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        
        public Integer getNewLimit() { return newLimit; }
        public void setNewLimit(Integer newLimit) { this.newLimit = newLimit; }
    }
    
    private static class ReferralUsageResponse {
        private Long id;
        private Long userId;
        private String username;
        private String fullName;
        private Integer usageCount;
        private Integer usageLimit;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
        
        public Integer getUsageLimit() { return usageLimit; }
        public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    private static class ReferralUsageListResponse {
        private List<ReferralUsageResponse> referralUsages;
        
        public ReferralUsageListResponse(List<ReferralUsageResponse> referralUsages) {
            this.referralUsages = referralUsages;
        }
        
        public List<ReferralUsageResponse> getReferralUsages() { return referralUsages; }
        public void setReferralUsages(List<ReferralUsageResponse> referralUsages) { this.referralUsages = referralUsages; }
    }
    
    private static class BalanceUpdateRequest {
        private BigDecimal amount;
        private String reason;
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}