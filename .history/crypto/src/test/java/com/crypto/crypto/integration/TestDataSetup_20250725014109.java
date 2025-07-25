package com.crypto.crypto.integration;

import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.Role;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("test")
public class TestDataSetup implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlanRepository planRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        createTestData();
    }
    
    private void createTestData() {
        // Create test plans
        createTestPlans();
        
        // Create test users
        createTestUsers();
    }
    
    private void createTestPlans() {
        if (planRepository.count() == 0) {
            Plan plan1 = new Plan();
            plan1.setName("Test Plan 1");
            plan1.setPrice(new BigDecimal("100.00"));
            plan1.setDailyProfitMin(new BigDecimal("2.00"));
            plan1.setDailyProfitMax(new BigDecimal("5.00"));
            plan1.setPlanLevel(1);
            planRepository.save(plan1);
            
            Plan plan2 = new Plan();
            plan2.setName("Test Plan 2");
            plan2.setPrice(new BigDecimal("200.00"));
            plan2.setDailyProfitMin(new BigDecimal("4.00"));
            plan2.setDailyProfitMax(new BigDecimal("10.00"));
            plan2.setPlanLevel(2);
            planRepository.save(plan2);
        }
    }
    
    private void createTestUsers() {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setFullName("Test Admin");
            admin.setDisplayUsername("admin");
            admin.setPhoneNumber("1234567890");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRole(Role.ADMIN);
            admin.setTotalBalance(BigDecimal.ZERO);
            admin.setFrozenBalance(BigDecimal.ZERO);
            admin.setReferralEarnings(BigDecimal.ZERO);
            userRepository.save(admin);
            
            // Create test user
            User user = new User();
            user.setFullName("Test User");
            user.setDisplayUsername("testuser");
            user.setPhoneNumber("9876543210");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(Role.USER);
            user.setTotalBalance(new BigDecimal("1000.00"));
            user.setFrozenBalance(BigDecimal.ZERO);
            user.setReferralEarnings(BigDecimal.ZERO);
            user.setReferrer(admin);
            userRepository.save(user);
        }
    }
} 