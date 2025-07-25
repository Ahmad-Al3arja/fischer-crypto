package com.crypto.crypto.controller;
import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.ReferralService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserDTOs.UpdateProfileRequest request) {
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
    public ResponseEntity<?> getDailyCounter() {
        try {
            UserDTOs.DailyCounterResponse response = dailyCounterService.getUserDailyCounter();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    private static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
} 