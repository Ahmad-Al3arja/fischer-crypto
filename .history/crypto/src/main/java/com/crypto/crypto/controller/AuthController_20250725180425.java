package com.crypto.crypto.controller;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.security.JwtUtils;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.entity.Role;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;
    
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
            
            // Phone number validation removed - accepting any format
            
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
    
    @PostMapping("/register-debug")
    public ResponseEntity<?> registerDebug(@RequestBody Map<String, String> request) {
        try {
            System.out.println("=== DEBUG REGISTRATION START ===");
            
            // Get request fields
            String fullName = request.get("fullName");
            String username = request.get("username");
            String phoneNumber = request.get("phoneNumber");
            String password = request.get("password");
            
            System.out.println("Full Name: " + fullName);
            System.out.println("Username: " + username);
            System.out.println("Phone: " + phoneNumber);
            
            // Basic validation
            if (fullName == null || fullName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
            }
            
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
            }
            
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            }
            
            // Check if phone number already exists
            if (userRepository.existsByPhoneNumber(phoneNumber)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Phone number already registered"));
            }
            
            // Check if username already exists
            if (userRepository.existsByDisplayUsername(username)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
            }
            
            System.out.println("✓ Validation passed, creating user...");
            
            // Create user directly without referral system
            User user = new User();
            user.setFullName(fullName.trim());
            user.setDisplayUsername(username.trim());
            user.setPhoneNumber(phoneNumber.trim());
            user.setPassword(passwordEncoder.encode(password));
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(Role.USER);
            user.setTotalBalance(BigDecimal.ZERO);
            user.setFrozenBalance(BigDecimal.ZERO);
            user.setReferralEarnings(BigDecimal.ZERO);
            
            System.out.println("✓ User object created, saving to database...");
            
            // Save user
            User savedUser = userRepository.save(user);
            System.out.println("✓ User saved with ID: " + savedUser.getId());
            
            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(savedUser.getPhoneNumber());
            System.out.println("✓ JWT token generated");
            
            AuthDTOs.AuthResponse response = new AuthDTOs.AuthResponse(
                jwt, 
                savedUser.getId(), 
                savedUser.getDisplayUsername(), 
                savedUser.getRole().name()
            );
            
            System.out.println("=== DEBUG REGISTRATION SUCCESS ===");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Debug registration error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Registration failed", 
                        "message", e.getMessage(),
                        "type", e.getClass().getSimpleName()
                    ));
        }
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