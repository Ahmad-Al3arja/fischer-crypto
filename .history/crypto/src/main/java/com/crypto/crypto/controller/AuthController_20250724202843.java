package com.crypto.crypto.controller;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTOs.RegisterRequest request) {
        try {
            AuthDTOs.LoginResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GlobalExceptionHandler.ErrorResponse("Registration Error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTOs.LoginRequest request) {
        try {
            AuthDTOs.LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GlobalExceptionHandler.ErrorResponse("Login Error", e.getMessage()));
        }
    }
    
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin() {
        try {
            // Check if admin already exists
            if (userRepository.findByDisplayUsername("admin").isPresent()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Admin user already exists"));
            }
            
            // Create admin user
            var admin = new com.crypto.crypto.entity.User();
            admin.setFullName("Admin User");
            admin.setDisplayUsername("admin");
            admin.setPhoneNumber("1234567890");
            admin.setPassword("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi"); // admin123
            admin.setStatus(com.crypto.crypto.entity.UserStatus.ACTIVE);
            admin.setRole(com.crypto.crypto.entity.Role.ADMIN);
            admin.setTotalBalance(java.math.BigDecimal.ZERO);
            admin.setFrozenBalance(java.math.BigDecimal.ZERO);
            admin.setReferralEarnings(java.math.BigDecimal.ZERO);
            
            userRepository.save(admin);
            
            return ResponseEntity.ok(Map.of(
                "message", "Admin user created successfully",
                "username", "admin",
                "phoneNumber", "1234567890",
                "password", "admin123"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GlobalExceptionHandler.ErrorResponse("Admin Creation Error", e.getMessage()));
        }
    }
    
    @GetMapping("/test-admin")
    public ResponseEntity<?> testAdmin() {
        try {
            var admin = userRepository.findByDisplayUsername("admin");
            if (admin.isPresent()) {
                var user = admin.get();
                return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "username", user.getDisplayUsername(),
                    "phoneNumber", user.getPhoneNumber(),
                    "status", user.getStatus().name(),
                    "role", user.getRole().name(),
                    "passwordLength", user.getPassword().length()
                ));
            } else {
                return ResponseEntity.ok(Map.of("exists", false));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GlobalExceptionHandler.ErrorResponse("Admin Test Error", e.getMessage()));
        }
    }
    
    @GetMapping("/test-referral/{referralCode}")
    public ResponseEntity<?> testReferral(@PathVariable String referralCode) {
        try {
            var user = userRepository.findByDisplayUsername(referralCode);
            if (user.isPresent()) {
                var foundUser = user.get();
                return ResponseEntity.ok(Map.of(
                    "found", true,
                    "referralCode", referralCode,
                    "username", foundUser.getDisplayUsername(),
                    "phoneNumber", foundUser.getPhoneNumber(),
                    "status", foundUser.getStatus().name(),
                    "role", foundUser.getRole().name()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "found", false,
                    "referralCode", referralCode,
                    "message", "No user found with this referral code"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GlobalExceptionHandler.ErrorResponse("Referral Test Error", e.getMessage()));
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
} 