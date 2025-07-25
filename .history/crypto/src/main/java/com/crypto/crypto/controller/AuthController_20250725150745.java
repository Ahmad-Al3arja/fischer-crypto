package com.crypto.crypto.controller;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        try {
            System.out.println("=== REGISTRATION REQUEST START ===");
            System.out.println("Phone: " + request.getPhoneNumber());
            System.out.println("Username: " + request.getUsername());
            System.out.println("Full name: " + request.getFullName());
            System.out.println("Referral code: " + request.getReferralCode());
            
            // Basic validation
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Full name is required"));
            }
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Username is required"));
            }
            
            if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Phone number is required"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Password is required"));
            }
            
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Confirm password is required"));
            }
            
            // Password validation
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Passwords do not match"));
            }
            
            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Password must be at least 6 characters"));
            }
            
            // Username validation
            if (request.getUsername().length() < 3 || request.getUsername().length() > 20) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Username must be between 3 and 20 characters"));
            }
            
            if (!request.getUsername().matches("^[a-zA-Z0-9]+$")) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Username can only contain letters and numbers"));
            }
            
            // Phone number validation (Saudi format)
            if (!request.getPhoneNumber().matches("^(\\+966|966|0)?5[0-9]{8}$")) {
                return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Invalid phone number format. Please use Saudi mobile number format"));
            }
            
            System.out.println("✓ Basic validation passed");
            
            // Call service to register user
            AuthDTOs.AuthResponse response = userService.register(request);
            
            System.out.println("✓ Registration completed successfully");
            System.out.println("=== REGISTRATION REQUEST END ===");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            System.err.println("Registration error (RuntimeException): " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("REGISTRATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Registration error (General Exception): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        try {
            System.out.println("Login attempt for phone: " + request.getPhoneNumber());
            AuthDTOs.AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Login error: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Login error (unexpected): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            AuthDTOs.ProfileResponse response = userService.getCurrentUserProfile();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Profile error: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            userService.logout();
            return ResponseEntity.ok(new SuccessResponse("Logged out successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody AuthDTOs.ChangePasswordRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(new SuccessResponse("Test endpoint working"));
    }
    
    // Enhanced error response classes with more detail
    private static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;
        
        public ErrorResponse(String message) {
            this.error = "REGISTRATION_ERROR";
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
    
    private static class SuccessResponse {
        private String message;
        private long timestamp;
        
        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
} 