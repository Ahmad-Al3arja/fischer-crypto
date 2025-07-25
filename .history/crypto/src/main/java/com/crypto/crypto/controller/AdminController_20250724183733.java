// Enhanced AdminController.java
package com.crypto.crypto.controller;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.service.*;
import com.crypto.crypto.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
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
    
    @Autowired
    private ReferralService referralService;
    
    @Autowired
    private PlanService planService;
    
    // ============ USER MANAGEMENT ============
    
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {
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
    
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            AdminDTOs.UserSearchResponse response = userService.searchUsers(query);
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
    
    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        try {
            userService.activateUser(userId);
            return ResponseEntity.ok(new SuccessResponse("User activated successfully"));
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
    
    // ============ WALLET MANAGEMENT ============
    
    @GetMapping("/wallet-change-requests")
    public ResponseEntity<?> getWalletChangeRequests() {
        try {
            List<AdminDTOs.WalletChangeRequestSummary> requests = walletService.getPendingWalletChangeRequests()
                    .stream()
                    .map(this::convertToWalletChangeRequestSummary)
                    .toList();
            return ResponseEntity.ok(new AdminDTOs.WalletChangeRequestListResponse(requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/wallet-change-requests/{requestId}/approve")
    public ResponseEntity<?> approveWalletChangeRequest(@PathVariable Long requestId) {
        try {
            User currentAdmin = userService.getCurrentUser();
            walletService.approveWalletChangeRequest(requestId, currentAdmin.getId());
            return ResponseEntity.ok(new SuccessResponse("Wallet change request approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/wallet-change-requests/{requestId}/reject")
    public ResponseEntity<?> rejectWalletChangeRequest(@PathVariable Long requestId, @RequestBody RejectionRequest request) {
        try {
            User currentAdmin = userService.getCurrentUser();
            walletService.rejectWalletChangeRequest(requestId, currentAdmin.getId(), request.getReason());
            return ResponseEntity.ok(new SuccessResponse("Wallet change request rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/wallet")
    public ResponseEntity<?> updateUserWallet(@PathVariable Long userId, @RequestBody WalletUpdateRequest request) {
        try {
            User user = userService.getUserById(userId);
            walletService.updateWalletAddressByUser(user, request.getUsdtAddress());
            return ResponseEntity.ok(new SuccessResponse("User wallet address updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ DEPOSIT MANAGEMENT ============
    
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
    
    // ============ WITHDRAWAL MANAGEMENT ============
    
    @GetMapping("/withdrawals")
    public ResponseEntity<?> getAllWithdrawals(@RequestParam(required = false) String status) {
        try {
            AdminDTOs.WithdrawalListResponse response = withdrawalService.getAllWithdrawals(status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/withdrawals/{withdrawalId}/approve")
    public ResponseEntity<?> approveWithdrawal(@PathVariable Long withdrawalId) {
        try {
            withdrawalService.approveWithdrawal(withdrawalId);
            return ResponseEntity.ok(new SuccessResponse("Withdrawal approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/withdrawals/{withdrawalId}/reject")
    public ResponseEntity<?> rejectWithdrawal(@PathVariable Long withdrawalId, @RequestBody RejectionRequest request) {
        try {
            withdrawalService.rejectWithdrawal(withdrawalId, request.getReason());
            return ResponseEntity.ok(new SuccessResponse("Withdrawal rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ REFERRAL MANAGEMENT ============
    
    @GetMapping("/referrals")
    public ResponseEntity<?> getAllReferralUsages() {
        try {
            AdminDTOs.ReferralUsageListResponse response = referralService.getAllReferralUsages();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/{userId}/limit")
    public ResponseEntity<?> updateReferralLimit(@PathVariable Long userId, @RequestBody ReferralLimitRequest request) {
        try {
            referralService.updateReferralUsageLimit(userId, request.getNewLimit());
            return ResponseEntity.ok(new SuccessResponse("Referral limit updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/{userId}/toggle")
    public ResponseEntity<?> toggleReferralCode(@PathVariable Long userId) {
        try {
            referralService.toggleReferralCodeStatus(userId);
            return ResponseEntity.ok(new SuccessResponse("Referral code status toggled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/referrals/{userId}/reset")
    public ResponseEntity<?> resetReferralUsage(@PathVariable Long userId) {
        try {
            referralService.resetReferralUsageCount(userId);
            return ResponseEntity.ok(new SuccessResponse("Referral usage count reset successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ DAILY COUNTER MANAGEMENT ============
    
    @PostMapping("/users/{userId}/counter/activate")
    public ResponseEntity<?> activateUserCounter(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            dailyCounterService.activateCounter(user);
            return ResponseEntity.ok(new SuccessResponse("User counter activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/users/{userId}/counter/deactivate")
    public ResponseEntity<?> deactivateUserCounter(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            dailyCounterService.deactivateCounter(user);
            return ResponseEntity.ok(new SuccessResponse("User counter deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ PROMO CODE MANAGEMENT ============
    
    @PostMapping("/promo-codes")
    public ResponseEntity<?> createPromoCode(@Valid @RequestBody AdminDTOs.CreatePromoCodeRequest request) {
        try {
            AdminDTOs.PromoCodeResponse response = promoCodeService.createPromoCode(request);
            return ResponseEntity.ok(new SuccessResponse("Promo code created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/promo-codes")
    public ResponseEntity<?> getAllPromoCodes() {
        try {
            AdminDTOs.PromoCodeListResponse response = promoCodeService.getAllPromoCodes();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/promo-codes/{promoCodeId}/toggle")
    public ResponseEntity<?> togglePromoCode(@PathVariable Long promoCodeId) {
        try {
            promoCodeService.togglePromoCode(promoCodeId);
            return ResponseEntity.ok(new SuccessResponse("Promo code toggled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ PLAN MANAGEMENT ============
    
    @GetMapping("/plans/stats")
    public ResponseEntity<?> getAllPlansStats() {
        try {
            AdminDTOs.PlansStatsResponse response = planService.getAllPlansStats();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ ADMIN SETTINGS ============
    
    @GetMapping("/settings")
    public ResponseEntity<?> getAdminSettings() {
        try {
            AdminDTOs.AdminSettingsResponse response = adminSettingsService.getAdminSettings();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/settings/maintenance")
    public ResponseEntity<?> toggleMaintenanceMode(@RequestBody MaintenanceRequest request) {
        try {
            adminSettingsService.setMaintenanceMode(request.isEnabled());
            return ResponseEntity.ok(new SuccessResponse("Maintenance mode updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/settings/about")
    public ResponseEntity<?> updateAboutContent(@RequestBody AboutContentRequest request) {
        try {
            adminSettingsService.updateAboutContent(request.getContent());
            return ResponseEntity.ok(new SuccessResponse("About content updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/settings/system")
    public ResponseEntity<?> updateSystemSetting(@RequestBody SystemSettingRequest request) {
        try {
            adminSettingsService.updateSystemSetting(request.getCategory(), 
                    request.getKeyName(), request.getValue());
            return ResponseEntity.ok(new SuccessResponse("System setting updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ DASHBOARD & ANALYTICS ============
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            AdminDTOs.DashboardResponse response = adminSettingsService.getAdminDashboard();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String type) {
        try {
            AdminDTOs.AnalyticsResponse response = adminSettingsService.getAnalytics(period, type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // ============ HELPER METHODS ============
    
    private AdminDTOs.WalletChangeRequestSummary convertToWalletChangeRequestSummary(Object request) {
        // Implementation to convert WalletChangeRequest to summary
        // This would need proper implementation based on the actual entity
        return new AdminDTOs.WalletChangeRequestSummary();
    }
    
    // ============ REQUEST/RESPONSE CLASSES ============
    
    private static class BalanceUpdateRequest {
        private BigDecimal amount;
        private String reason;
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    private static class RejectionRequest {
        private String reason;
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    private static class MaintenanceRequest {
        private boolean enabled;
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    private static class AboutContentRequest {
        private String content;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    private static class WalletUpdateRequest {
        private String usdtAddress;
        
        public String getUsdtAddress() { return usdtAddress; }
        public void setUsdtAddress(String usdtAddress) { this.usdtAddress = usdtAddress; }
    }
    
    private static class ReferralLimitRequest {
        private Integer newLimit;
        
        public Integer getNewLimit() { return newLimit; }
        public void setNewLimit(Integer newLimit) { this.newLimit = newLimit; }
    }
    
    private static class SystemSettingRequest {
        private String category;
        private String keyName;
        private String value;
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getKeyName() { return keyName; }
        public void setKeyName(String keyName) { this.keyName = keyName; }
        
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
    
    private static class ErrorResponse {
        private String message;
        private String status = "error";
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
    
    private static class SuccessResponse {
        private String message;
        private String status = "success";
        private Object data;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public SuccessResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
        public Object getData() { return data; }
    }
}