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
            System.out.println("Admin user already exists - updating...");
            User admin = existingAdmin.get();
            
            // Ensure displayUsername is set
            if (admin.getDisplayUsername() == null || admin.getDisplayUsername().isEmpty() || !admin.getDisplayUsername().equals("admin")) {
                System.out.println("Updating admin displayUsername...");
                admin.setDisplayUsername("admin");
            }
            
            // Ensure fullName is set
            if (admin.getFullName() == null || admin.getFullName().isEmpty()) {
                admin.setFullName("System Administrator");
            }
            
            // Update password if needed
            String encodedPassword = passwordEncoder.encode("admin123");
            if (!passwordEncoder.matches("admin123", admin.getPassword())) {
                System.out.println("Updating admin password...");
                admin.setPassword(encodedPassword);
            }
            
            // Ensure role and status are correct
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            
            userRepository.save(admin);
            System.out.println("Admin user updated successfully!");
            System.out.println("Phone: 1234567890");
            System.out.println("Username: " + admin.getDisplayUsername());
            System.out.println("Password: admin123");
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
        System.out.println("Username: admin");
        System.out.println("Password: admin123");
    }
}