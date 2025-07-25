package com.crypto.crypto.config;

import com.crypto.crypto.entity.Role;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(1)
public class AdminUserDataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
    }
    
    private void createAdminUserIfNotExists() {
        String adminPhone = "1234567890";
        
        Optional<User> existingAdmin = userRepository.findByPhoneNumber(adminPhone);
        
        if (!existingAdmin.isPresent()) {
            System.out.println("Creating admin user...");
            createAdminUser();
        } else {
            System.out.println("Admin user already exists");
            // Optionally update password if needed
            User admin = existingAdmin.get();
            String encodedPassword = passwordEncoder.encode("admin123");
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                System.out.println("Updating admin password...");
                admin.setPassword(encodedPassword);
                userRepository.save(admin);
                System.out.println("Admin password updated");
            }
        }
    }
    
    private void createAdminUser() {
        User admin = new User();
        admin.setFullName("System Administrator");
        admin.setDisplayUsername("admin");
        admin.setPhoneNumber("1234567890");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(Role.ADMIN);
        admin.setTotalBalance(BigDecimal.ZERO);
        admin.setFrozenBalance(BigDecimal.ZERO);
        admin.setReferralEarnings(BigDecimal.ZERO);
        
        userRepository.save(admin);
        System.out.println("Admin user created successfully!");
        System.out.println("Phone: 1234567890");
        System.out.println("Password: admin123");
    }
} 