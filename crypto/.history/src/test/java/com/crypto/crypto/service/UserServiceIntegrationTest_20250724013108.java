package com.crypto.crypto.service;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.entity.Role;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import com.crypto.crypto.repository.PlanRepository;
import com.crypto.crypto.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Plan testPlan;

    @BeforeEach
    void setUp() {
        // Create a test plan
        testPlan = new Plan();
        testPlan.setName("Test Plan");
        testPlan.setPrice(new java.math.BigDecimal("100.00"));
        testPlan.setMonthlyProfit(new java.math.BigDecimal("10.00"));
        testPlan.setDailyProfitMin(new java.math.BigDecimal("0.30"));
        testPlan.setDailyProfitMax(new java.math.BigDecimal("0.40"));
        testPlan.setPlanLevel(1);
        testPlan = planRepository.save(testPlan);

        // Create a referrer user
        User referrer = new User();
        referrer.setFullName("Test Referrer");
        referrer.setUsername("referrer");
        referrer.setPhoneNumber("+1234567890");
        referrer.setPassword(passwordEncoder.encode("password"));
        referrer.setStatus(UserStatus.ACTIVE);
        referrer.setRole(Role.USER);
        referrer = userRepository.save(referrer);

        // Create test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setUsername("testuser");
        testUser.setPhoneNumber("+0987654321");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(Role.USER);
        testUser.setReferrer(referrer);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testRegisterUser() {
        AuthDTOs.RegisterRequest request = new AuthDTOs.RegisterRequest();
        request.setFullName("New User");
        request.setUsername("newuser");
        request.setPhoneNumber("+1111111111");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setReferralCode("referrer");

        AuthDTOs.LoginResponse response = userService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals("USER", response.getRole());

        User savedUser = userRepository.findByUsername("newuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("New User", savedUser.getFullName());
        assertEquals(UserStatus.INACTIVE, savedUser.getStatus());
    }

    @Test
    void testLoginUser() {
        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest();
        request.setPhoneNumber("+0987654321");
        request.setPassword("password");

        AuthDTOs.LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());
    }

    @Test
    void testLoginInactiveUser() {
        testUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(testUser);

        AuthDTOs.LoginRequest request = new AuthDTOs.LoginRequest();
        request.setPhoneNumber("+0987654321");
        request.setPassword("password");

        assertThrows(RuntimeException.class, () -> userService.login(request));
    }

    @Test
    void testGetCurrentUser() {
        // This test would require setting up security context
        // For now, we'll test the getUserById method
        User foundUser = userService.getUserById(testUser.getId());
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void testActivateUser() {
        testUser.setStatus(UserStatus.INACTIVE);
        userRepository.save(testUser);

        userService.activateUser(testUser.getId());

        User activatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(activatedUser);
        assertEquals(UserStatus.ACTIVE, activatedUser.getStatus());
    }

    @Test
    void testSuspendUser() {
        userService.suspendUser(testUser.getId());

        User suspendedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(suspendedUser);
        assertEquals(UserStatus.SUSPENDED, suspendedUser.getStatus());
    }
} 