package com.crypto.crypto.controller;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.dto.PlanDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.PromoCodeService;
import com.crypto.crypto.service.AdminSettingsService;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.WalletService;
import com.crypto.crypto.service.PlanService;
import com.crypto.crypto.service.ReferralUsageService;
import com.crypto.crypto.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="*")
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
    private PlanService planService;
    
    @Autowired
    private ReferralUsageService referralUsageService;
//User Management
@GetMapping("/users")
public ResponseEntity<?> getAllUsers(@RequestParam(required=false) Long planId) {
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
@PostMapping("/users/{userId}/activate")
public ResponseEntity<?> activateUser(@PathVariable Long userId) {
    try {
        userService.activateUser(userId);
        return ResponseEntity.ok(new SuccessResponse("User activated successfully"));
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
//Deposit Management
@GetMapping("/deposits")
public ResponseEntity<?> getAllDeposits(@RequestParam(required=false) String status) {
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
//Withdrawal Management
@GetMapping("/withdrawals")
public ResponseEntity<?> getAllWithdrawals(@RequestParam(required=false) String status) {
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
//Daily Counter Management
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
//Promo Code Management
@PostMapping("/promo-codes")
public ResponseEntity<?> createPromoCode(@Valid @RequestBody AdminDTOs.CreatePromoCodeRequest request) {
    try {
        AdminDTOs.PromoCodeResponse response = promoCodeService.createPromoCode(request);
        return ResponseEntity.ok(response);
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
//Plan Management
@GetMapping("/plans")
public ResponseEntity<?> getAllPlans() {
    try {
        PlanDTOs.PlansListResponse response = planService.getAllPlans();
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@PostMapping("/plans")
public ResponseEntity<?> createPlan(@Valid @RequestBody PlanDTOs.CreatePlanRequest request) {
    try {
        PlanDTOs.PlanResponse response = planService.createPlan(request);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@PutMapping("/plans/{planId}")
public ResponseEntity<?> updatePlan(@PathVariable Long planId, @Valid @RequestBody PlanDTOs.UpdatePlanRequest request) {
    try {
        PlanDTOs.PlanResponse response = planService.updatePlan(planId, request);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@DeleteMapping("/plans/{planId}")
public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
    try {
        planService.deletePlan(planId);
        return ResponseEntity.ok(new SuccessResponse("Plan deleted successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
//Admin Settings
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
@GetMapping("/settings/default-usage-limit")
public ResponseEntity<?> getDefaultUsageLimit() {
    try {
        int limit = adminSettingsService.getDefaultUsageLimit();
        return ResponseEntity.ok(new DefaultUsageLimitResponse(limit));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@PostMapping("/settings/default-usage-limit")
public ResponseEntity<?> updateDefaultUsageLimit(@RequestBody DefaultUsageLimitRequest request) {
    try {
        if (request.getUsageLimit() <= 0) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Usage limit must be greater than 0"));
        }
        adminSettingsService.updateDefaultUsageLimit(request.getUsageLimit());
        return ResponseEntity.ok(new SuccessResponse("Default usage limit updated successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
//Wallet Change Request Management
@GetMapping("/wallet-change-requests")
public ResponseEntity<?> getAllWalletChangeRequests() {
    try {
        List<com.crypto.crypto.entity.WalletChangeRequest> requests = walletService.getAllRequests();
        List<AdminDTOs.WalletChangeSummary> summaries = requests.stream().map(this::convertToWalletChangeSummary).collect(java.util.stream.Collectors.toList());
        AdminDTOs.WalletChangeListResponse response = new AdminDTOs.WalletChangeListResponse(summaries);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@GetMapping("/wallet-change-requests/pending")
public ResponseEntity<?> getPendingWalletChangeRequests() {
    try {
        List<com.crypto.crypto.entity.WalletChangeRequest> requests = walletService.getAllPendingRequests();
        List<AdminDTOs.WalletChangeSummary> summaries = requests.stream().map(this::convertToWalletChangeSummary).collect(java.util.stream.Collectors.toList());
        AdminDTOs.WalletChangeListResponse response = new AdminDTOs.WalletChangeListResponse(summaries);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@PostMapping("/wallet-change-requests/{requestId}/approve")
public ResponseEntity<?> approveWalletChangeRequest(@PathVariable Long requestId, @Valid @RequestBody AdminDTOs.WalletChangeActionRequest request) {
    try {
        User admin = userService.getCurrentUser();
        walletService.approveWalletChangeRequest(requestId, admin, request.getAdminNotes());
        return ResponseEntity.ok(new SuccessResponse("Wallet change request approved successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
@PostMapping("/wallet-change-requests/{requestId}/reject")
public ResponseEntity<?> rejectWalletChangeRequest(@PathVariable Long requestId, @Valid @RequestBody AdminDTOs.WalletChangeActionRequest request) {
    try {
        User admin = userService.getCurrentUser();
        walletService.rejectWalletChangeRequest(requestId, admin, request.getAdminNotes());
        return ResponseEntity.ok(new SuccessResponse("Wallet change request rejected successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
//Request/Response classes
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
private static class DefaultUsageLimitRequest {
    private int usageLimit;
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
}
private static class DefaultUsageLimitResponse {
    private int usageLimit;
    public DefaultUsageLimitResponse(int usageLimit) { this.usageLimit = usageLimit; }
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
}

@PostMapping("/users/{userId}/referral-limit")
public ResponseEntity<?> updateUserReferralLimit(@PathVariable Long userId, @RequestBody ReferralLimitRequest request) {
    try {
        referralUsageService.updateReferralLimit(userId, request.getLimit());
        return ResponseEntity.ok(new SuccessResponse("Referral limit updated successfully"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}

@GetMapping("/users/{userId}/referral-usage")
public ResponseEntity<?> getUserReferralUsage(@PathVariable Long userId) {
    try {
        AdminDTOs.ReferralUsageResponse response = referralUsageService.getUserReferralUsage(userId);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}

// Add these request/response classes at the bottom of AdminController
private static class ReferralLimitRequest {
    private Integer limit;
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
private AdminDTOs.WalletChangeSummary convertToWalletChangeSummary(com.crypto.crypto.entity.WalletChangeRequest request) {
    AdminDTOs.WalletChangeSummary summary = new AdminDTOs.WalletChangeSummary();
    summary.setId(request.getId());
    summary.setUserName(request.getUser().getDisplayUsername());
    summary.setUserPhone(request.getUser().getPhoneNumber());
    summary.setCurrentAddress(request.getCurrentAddress());
    summary.setNewAddress(request.getNewAddress());
    summary.setReason(request.getReason());
    summary.setStatus(request.getStatus().name());
    summary.setCreatedAt(request.getCreatedAt());
    summary.setAdminNotes(request.getAdminNotes());
    return summary;
}
private static class ErrorResponse {
    private String message;
    public ErrorResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
}
private static class SuccessResponse {
    private String message;
    public SuccessResponse(String message) { this.message = message; }
    public String getMessage() { return message; }
}

@GetMapping("/stats/overview")
public ResponseEntity<?> getAdminOverview() {
    try {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsersCount());
        stats.put("activeUsers", userService.getActiveUsersCount());
        stats.put("pendingDeposits", depositService.getPendingDepositsCount());
        stats.put("pendingWithdrawals", withdrawalService.getPendingWithdrawalsCount());
        stats.put("totalDeposited", depositService.getTotalDeposited());
        stats.put("totalWithdrawn", withdrawalService.getTotalWithdrawn());
        return ResponseEntity.ok(stats);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
} 

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/api/platform")
public class PlatformInfoController {
    @Autowired
    private AdminSettingsService adminSettingsService;

    @GetMapping("/info")
    public ResponseEntity<?> getPlatformInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("platformName", "منصة الاستثمار");
            info.put("usdtWallet", adminSettingsService.getStringSetting("platform_wallet", "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"));
            info.put("minWithdrawal", 10.00);
            info.put("withdrawalFee", 2.00);
            info.put("maintenanceMode", adminSettingsService.getBooleanSetting("maintenance_mode", false));
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
} 