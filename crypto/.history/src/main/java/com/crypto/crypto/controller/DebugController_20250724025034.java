package com.crypto.crypto.controller;

import com.crypto.crypto.entity.Role;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    // REMOVE THIS IN PRODUCTION!
    @PostMapping("/generate-password")
    public ResponseEntity<?> generatePasswordHash(@RequestBody PasswordRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        PasswordResponse response = new PasswordResponse();
        response.setPlainPassword(request.getPassword());
        response.setEncodedPassword(encodedPassword);
        response.setMatches(passwordEncoder.matches(request.getPassword(), encodedPassword));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser() {
        try {
            // Check if admin already exists
            Optional<User> existingAdmin = userRepository.findByPhoneNumber("1234567890");
            if (existingAdmin.isPresent()) {
                return ResponseEntity.badRequest().body("Admin user already exists");
            }
            
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setUsername("admin");
            admin.setPhoneNumber("1234567890");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRole(Role.ADMIN);
            admin.setTotalBalance(BigDecimal.ZERO);
            admin.setFrozenBalance(BigDecimal.ZERO);
            admin.setReferralEarnings(BigDecimal.ZERO);
            
            userRepository.save(admin);
            
            return ResponseEntity.ok("Admin user created successfully! Phone: 1234567890, Password: admin123");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating admin: " + e.getMessage());
        }
    }
    
    @GetMapping("/check-users")
    public ResponseEntity<?> checkUsers() {
        List<User> users = userRepository.findAll();
        List<UserInfo> userInfos = users.stream().map(user -> {
            UserInfo info = new UserInfo();
            info.setId(user.getId());
            info.setUsername(user.getUsername());
            info.setPhoneNumber(user.getPhoneNumber());
            info.setRole(user.getRole().name());
            info.setStatus(user.getStatus().name());
            info.setPasswordLength(user.getPassword() != null ? user.getPassword().length() : 0);
            info.setPasswordStarts(user.getPassword() != null ? user.getPassword().substring(0, Math.min(10, user.getPassword().length())) : "null");
            return info;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(userInfos);
    }
    
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody PasswordTestRequest request) {
        Optional<User> user = userRepository.findByPhoneNumber(request.getPhoneNumber());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found with phone: " + request.getPhoneNumber());
        }
        
        User foundUser = user.get();
        boolean matches = passwordEncoder.matches(request.getPassword(), foundUser.getPassword());
        
        PasswordTestResponse response = new PasswordTestResponse();
        response.setPhoneNumber(request.getPhoneNumber());
        response.setPassword(request.getPassword());
        response.setStoredHash(foundUser.getPassword());
        response.setMatches(matches);
        response.setUsername(foundUser.getUsername());
        response.setRole(foundUser.getRole().name());
        response.setStatus(foundUser.getStatus().name());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuthentication(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthCheckResponse response = new AuthCheckResponse();
        response.setHasAuthHeader(authHeader != null);
        response.setAuthHeader(authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            response.setHasBearerToken(true);
            response.setTokenLength(token.length());
            response.setTokenStart(token.substring(0, Math.min(20, token.length())));
            
            try {
                boolean isValid = jwtUtils.validateJwtToken(token);
                response.setTokenValid(isValid);
                
                if (isValid) {
                    String phoneNumber = jwtUtils.getPhoneNumberFromJwtToken(token);
                    response.setPhoneNumber(phoneNumber);
                    
                    Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
                    if (user.isPresent()) {
                        response.setUserExists(true);
                        response.setUsername(user.get().getUsername());
                        response.setRole(user.get().getRole().name());
                        response.setStatus(user.get().getStatus().name());
                    }
                }
            } catch (Exception e) {
                response.setError(e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    // Request/Response classes
    public static class PasswordRequest {
        private String password;
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class PasswordResponse {
        private String plainPassword;
        private String encodedPassword;
        private boolean matches;
        
        public String getPlainPassword() { return plainPassword; }
        public void setPlainPassword(String plainPassword) { this.plainPassword = plainPassword; }
        
        public String getEncodedPassword() { return encodedPassword; }
        public void setEncodedPassword(String encodedPassword) { this.encodedPassword = encodedPassword; }
        
        public boolean isMatches() { return matches; }
        public void setMatches(boolean matches) { this.matches = matches; }
    }
    
    public static class UserInfo {
        private Long id;
        private String username;
        private String phoneNumber;
        private String role;
        private String status;
        private int passwordLength;
        private String passwordStarts;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public int getPasswordLength() { return passwordLength; }
        public void setPasswordLength(int passwordLength) { this.passwordLength = passwordLength; }
        
        public String getPasswordStarts() { return passwordStarts; }
        public void setPasswordStarts(String passwordStarts) { this.passwordStarts = passwordStarts; }
    }
    
    public static class PasswordTestRequest {
        private String phoneNumber;
        private String password;
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class PasswordTestResponse {
        private String phoneNumber;
        private String password;
        private String storedHash;
        private boolean matches;
        private String username;
        private String role;
        private String status;
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getStoredHash() { return storedHash; }
        public void setStoredHash(String storedHash) { this.storedHash = storedHash; }
        
        public boolean isMatches() { return matches; }
        public void setMatches(boolean matches) { this.matches = matches; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    public static class AuthCheckResponse {
        private boolean hasAuthHeader;
        private String authHeader;
        private boolean hasBearerToken;
        private int tokenLength;
        private String tokenStart;
        private boolean tokenValid;
        private String phoneNumber;
        private boolean userExists;
        private String username;
        private String role;
        private String status;
        private String error;
        
        // Getters and setters
        public boolean isHasAuthHeader() { return hasAuthHeader; }
        public void setHasAuthHeader(boolean hasAuthHeader) { this.hasAuthHeader = hasAuthHeader; }
        
        public String getAuthHeader() { return authHeader; }
        public void setAuthHeader(String authHeader) { this.authHeader = authHeader; }
        
        public boolean isHasBearerToken() { return hasBearerToken; }
        public void setHasBearerToken(boolean hasBearerToken) { this.hasBearerToken = hasBearerToken; }
        
        public int getTokenLength() { return tokenLength; }
        public void setTokenLength(int tokenLength) { this.tokenLength = tokenLength; }
        
        public String getTokenStart() { return tokenStart; }
        public void setTokenStart(String tokenStart) { this.tokenStart = tokenStart; }
        
        public boolean isTokenValid() { return tokenValid; }
        public void setTokenValid(boolean tokenValid) { this.tokenValid = tokenValid; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public boolean isUserExists() { return userExists; }
        public void setUserExists(boolean userExists) { this.userExists = userExists; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
} 