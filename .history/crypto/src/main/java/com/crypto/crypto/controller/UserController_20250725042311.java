package com.crypto.crypto.controller;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.ReferralService;
import com.crypto.crypto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins="*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DailyCounterService dailyCounterService;
    
    @Autowired
    private ReferralService referralService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {
        try {
            UserDTOs.DashboardResponse response = userService.getDashboard();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            UserDTOs.ProfileResponse response = userService.getUserProfile();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTOs.UpdateProfileRequest request) {
        try {
            UserDTOs.ProfileResponse response = userService.updateProfile(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/referrals")
    public ResponseEntity<?> getUserReferrals() {
        try {
            UserDTOs.ReferralListResponse response = referralService.getUserReferrals();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/referral-earnings")
    public ResponseEntity<?> getReferralEarnings() {
        try {
            UserDTOs.ReferralEarningsResponse response = referralService.getReferralEarnings();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/daily-counter")
    public ResponseEntity<?> getUserDailyCounter() {
        try {
            UserDTOs.DailyCounterResponse response = dailyCounterService.getUserDailyCounter();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/daily-counter/activate")
    public ResponseEntity<?> activateCounter() {
        try {
            User user = userService.getCurrentUser();
            
            // Check if user has an active plan
            if (user.getCurrentPlan() == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("You must purchase a plan first to activate the counter"));
            }
            
            // Check if user account is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Your account must be activated by admin first"));
            }
            
            dailyCounterService.activateCounter(user);
            return ResponseEntity.ok(new SuccessResponse("Daily counter activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/daily-counter/complete")
    public ResponseEntity<?> completeCounter() {
        try {
            User user = userService.getCurrentUser();
            dailyCounterService.completeCounter(user);
            return ResponseEntity.ok(new SuccessResponse("Daily counter completed and profits added to balance"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getUserBalance() {
        try {
            User user = userService.getCurrentUser();
            UserDTOs.BalanceResponse response = new UserDTOs.BalanceResponse();
            response.setTotalBalance(user.getTotalBalance());
            response.setFrozenBalance(user.getFrozenBalance());
            response.setWithdrawableBalance(user.getWithdrawableBalance());
            response.setReferralEarnings(user.getReferralEarnings());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/team-stats")
    public ResponseEntity<?> getTeamStats() {
        try {
            User user = userService.getCurrentUser();
            UserDTOs.TeamStatsResponse response = userService.getTeamStats(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
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
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
} 