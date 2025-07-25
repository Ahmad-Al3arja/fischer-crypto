package com.crypto.crypto.controller;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.ReferralService;
import com.crypto.crypto.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('USER')")
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
            User currentUser = userService.getCurrentUser();
            UserDTOs.DashboardResponse response = userService.getDashboard(currentUser);
            
            // Add navigation links
            response.setNavigationLinks(new UserDTOs.NavigationLinks());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            User currentUser = userService.getCurrentUser();
            UserDTOs.ProfileResponse response = userService.getProfile(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/referral-stats")
    public ResponseEntity<?> getReferralStats() {
        try {
            User currentUser = userService.getCurrentUser();
            UserDTOs.ReferralStatsResponse response = referralService.getReferralStats(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/activate-counter")
    public ResponseEntity<?> activateCounter() {
        try {
            User currentUser = userService.getCurrentUser();
            dailyCounterService.activateCounter(currentUser);
            return ResponseEntity.ok(new SuccessResponse("Counter activated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/complete-counter")
    public ResponseEntity<?> completeCounter() {
        try {
            User currentUser = userService.getCurrentUser();
            dailyCounterService.completeCounter(currentUser);
            return ResponseEntity.ok(new SuccessResponse("Counter completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/team-stats")
    public ResponseEntity<?> getTeamStats() {
        try {
            User currentUser = userService.getCurrentUser();
            UserDTOs.TeamStatsResponse response = userService.getTeamStats(currentUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/referral-link")
    public ResponseEntity<?> getReferralLink() {
        try {
            User currentUser = userService.getCurrentUser();
            String referralLink = "https://yourapp.com/register?ref=" + currentUser.getDisplayUsername();
            Map<String, String> response = new HashMap<>();
            response.put("referralLink", referralLink);
            response.put("referralCode", currentUser.getDisplayUsername());
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