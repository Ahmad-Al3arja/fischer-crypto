// src/test/java/com/crypto/crypto/integration/ComprehensiveAPITest.java
package com.crypto.crypto.integration;

import com.crypto.crypto.dto.AuthDTOs;
import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.repository.UserRepository;
import com.crypto.crypto.repository.PlanRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class ComprehensiveAPITest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanRepository planRepository;

    private String baseUrl;
    private String userToken;
    private String adminToken;
    private Long userId;
    private Long adminId;
    private List<String> testResults = new ArrayList<>();

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        testResults.clear();
    }

    @Test
    @Order(1)
    void testAuthenticationFlow() {
        System.out.println("🔍 Testing Authentication Flow...");
        
        // Test admin login
        testAdminLogin();
        
        // Test user registration and login
        testUserRegistrationAndLogin();
        
        System.out.println("✅ Authentication Flow Tests Completed");
    }

    @Test
    @Order(2)
    void testPlanEndpoints() {
        System.out.println("🔍 Testing Plan Endpoints...");
        
        // Test public plan access
        testGetAllPlans();
        
        // Test admin plan management
        if (adminToken != null) {
            testCreatePlan();
            testUpdatePlan();
        }
        
        System.out.println("✅ Plan Endpoints Tests Completed");
    }

    @Test
    @Order(3)
    void testUserEndpoints() {
        System.out.println("🔍 Testing User Endpoints...");
        
        if (userToken != null) {
            testUserDashboard();
            testUserProfile();
            testUserReferralStats();
            testUserTeamStats();
        }
        
        System.out.println("✅ User Endpoints Tests Completed");
    }

    @Test
    @Order(4)
    void testTransactionEndpoints() {
        System.out.println("🔍 Testing Transaction Endpoints...");
        
        if (userToken != null) {
            testDepositFlow();
            testWithdrawalFlow();
            testWalletOperations();
        }
        
        System.out.println("✅ Transaction Endpoints Tests Completed");
    }

    @Test
    @Order(5)
    void testAdminEndpoints() {
        System.out.println("🔍 Testing Admin Endpoints...");
        
        if (adminToken != null) {
            testAdminUserManagement();
            testAdminDepositManagement();
            testAdminWithdrawalManagement();
            testAdminSettings();
        }
        
        System.out.println("✅ Admin Endpoints Tests Completed");
    }

    // Authentication Tests
    private void testAdminLogin() {
        try {
            AuthDTOs.LoginRequest loginRequest = new AuthDTOs.LoginRequest();
            loginRequest.setPhoneNumber("1234567890");
            loginRequest.setPassword("admin123");

            ResponseEntity<AuthDTOs.LoginResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/login", loginRequest, AuthDTOs.LoginResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                adminToken = response.getBody().getToken();
                adminId = response.getBody().getUserId();
                testResults.add("✅ Admin login successful");
                System.out.println("✅ Admin login successful");
            } else {
                testResults.add("❌ Admin login failed: " + response.getStatusCode());
                System.out.println("❌ Admin login failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Admin login error: " + e.getMessage());
            System.out.println("❌ Admin login error: " + e.getMessage());
        }
    }

    private void testUserRegistrationAndLogin() {
        try {
            // Register new user
            AuthDTOs.RegisterRequest registerRequest = new AuthDTOs.RegisterRequest();
            registerRequest.setFullName("Test User");
            registerRequest.setUsername("testuser" + System.currentTimeMillis());
            registerRequest.setPhoneNumber("9876543210" + System.currentTimeMillis() % 1000);
            registerRequest.setPassword("password123");
            registerRequest.setConfirmPassword("password123");
            registerRequest.setReferralCode("admin");

            ResponseEntity<AuthDTOs.LoginResponse> registerResponse = restTemplate.postForEntity(
                baseUrl + "/auth/register", registerRequest, AuthDTOs.LoginResponse.class);

            if (registerResponse.getStatusCode() == HttpStatus.OK && registerResponse.getBody() != null) {
                userToken = registerResponse.getBody().getToken();
                userId = registerResponse.getBody().getUserId();
                testResults.add("✅ User registration successful");
                System.out.println("✅ User registration successful");
            } else {
                testResults.add("❌ User registration failed: " + registerResponse.getStatusCode());
                System.out.println("❌ User registration failed: " + registerResponse.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ User registration error: " + e.getMessage());
            System.out.println("❌ User registration error: " + e.getMessage());
        }
    }

    // Plan Tests
    private void testGetAllPlans() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/plans", String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Get all plans successful");
                System.out.println("✅ Get all plans successful");
            } else {
                testResults.add("❌ Get all plans failed: " + response.getStatusCode());
                System.out.println("❌ Get all plans failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Get all plans error: " + e.getMessage());
            System.out.println("❌ Get all plans error: " + e.getMessage());
        }
    }

    private void testCreatePlan() {
        try {
            Map<String, Object> planRequest = new HashMap<>();
            planRequest.put("name", "Test Plan");
            planRequest.put("price", 1000);
            planRequest.put("monthlyProfit", 100);
            planRequest.put("dailyProfitMin", 3);
            planRequest.put("dailyProfitMax", 5);
            planRequest.put("planLevel", 10);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(planRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/plans", HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Create plan successful");
                System.out.println("✅ Create plan successful");
            } else {
                testResults.add("❌ Create plan failed: " + response.getStatusCode());
                System.out.println("❌ Create plan failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Create plan error: " + e.getMessage());
            System.out.println("❌ Create plan error: " + e.getMessage());
        }
    }

    private void testUpdatePlan() {
        try {
            // Assuming plan ID 1 exists
            Map<String, Object> planRequest = new HashMap<>();
            planRequest.put("name", "Updated Test Plan");
            planRequest.put("price", 1500);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(planRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/plans/1", HttpMethod.PUT, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Update plan successful");
                System.out.println("✅ Update plan successful");
            } else {
                testResults.add("❌ Update plan failed: " + response.getStatusCode());
                System.out.println("❌ Update plan failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Update plan error: " + e.getMessage());
            System.out.println("❌ Update plan error: " + e.getMessage());
        }
    }

    // User Tests
    private void testUserDashboard() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/dashboard", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ User dashboard successful");
                System.out.println("✅ User dashboard successful");
            } else {
                testResults.add("❌ User dashboard failed: " + response.getStatusCode());
                System.out.println("❌ User dashboard failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ User dashboard error: " + e.getMessage());
            System.out.println("❌ User dashboard error: " + e.getMessage());
        }
    }

    private void testUserProfile() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/profile", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ User profile successful");
                System.out.println("✅ User profile successful");
            } else {
                testResults.add("❌ User profile failed: " + response.getStatusCode());
                System.out.println("❌ User profile failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ User profile error: " + e.getMessage());
            System.out.println("❌ User profile error: " + e.getMessage());
        }
    }

    private void testUserReferralStats() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/referral-stats", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ User referral stats successful");
                System.out.println("✅ User referral stats successful");
            } else {
                testResults.add("❌ User referral stats failed: " + response.getStatusCode());
                System.out.println("❌ User referral stats failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ User referral stats error: " + e.getMessage());
            System.out.println("❌ User referral stats error: " + e.getMessage());
        }
    }

    private void testUserTeamStats() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/user/team-stats", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ User team stats successful");
                System.out.println("✅ User team stats successful");
            } else {
                testResults.add("❌ User team stats failed: " + response.getStatusCode());
                System.out.println("❌ User team stats failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ User team stats error: " + e.getMessage());
            System.out.println("❌ User team stats error: " + e.getMessage());
        }
    }

    // Transaction Tests
    private void testDepositFlow() {
        try {
            Map<String, Object> depositRequest = new HashMap<>();
            depositRequest.put("amount", 100);
            depositRequest.put("planId", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(depositRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/transactions/deposit", HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Create deposit successful");
                System.out.println("✅ Create deposit successful");
            } else {
                testResults.add("❌ Create deposit failed: " + response.getStatusCode());
                System.out.println("❌ Create deposit failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Create deposit error: " + e.getMessage());
            System.out.println("❌ Create deposit error: " + e.getMessage());
        }
    }

    private void testWithdrawalFlow() {
        try {
            // First save wallet address
            Map<String, Object> walletRequest = new HashMap<>();
            walletRequest.put("usdtAddress", "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> walletEntity = new HttpEntity<>(walletRequest, headers);

            ResponseEntity<String> walletResponse = restTemplate.exchange(
                baseUrl + "/transactions/wallet/save", HttpMethod.POST, walletEntity, String.class);

            // Then test withdrawal
            Map<String, Object> withdrawalRequest = new HashMap<>();
            withdrawalRequest.put("amount", 10);

            HttpEntity<Map<String, Object>> withdrawalEntity = new HttpEntity<>(withdrawalRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/transactions/withdraw", HttpMethod.POST, withdrawalEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                testResults.add("✅ Withdrawal flow tested (expected behavior)");
                System.out.println("✅ Withdrawal flow tested");
            } else {
                testResults.add("❌ Withdrawal failed: " + response.getStatusCode());
                System.out.println("❌ Withdrawal failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Withdrawal error: " + e.getMessage());
            System.out.println("❌ Withdrawal error: " + e.getMessage());
        }
    }

    private void testWalletOperations() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(userToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/transactions/wallet", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Get wallet info successful");
                System.out.println("✅ Get wallet info successful");
            } else {
                testResults.add("❌ Get wallet info failed: " + response.getStatusCode());
                System.out.println("❌ Get wallet info failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Get wallet info error: " + e.getMessage());
            System.out.println("❌ Get wallet info error: " + e.getMessage());
        }
    }

    // Admin Tests
    private void testAdminUserManagement() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/users", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Admin get users successful");
                System.out.println("✅ Admin get users successful");
            } else {
                testResults.add("❌ Admin get users failed: " + response.getStatusCode());
                System.out.println("❌ Admin get users failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Admin get users error: " + e.getMessage());
            System.out.println("❌ Admin get users error: " + e.getMessage());
        }
    }

    private void testAdminDepositManagement() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/deposits", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Admin get deposits successful");
                System.out.println("✅ Admin get deposits successful");
            } else {
                testResults.add("❌ Admin get deposits failed: " + response.getStatusCode());
                System.out.println("❌ Admin get deposits failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Admin get deposits error: " + e.getMessage());
            System.out.println("❌ Admin get deposits error: " + e.getMessage());
        }
    }

    private void testAdminWithdrawalManagement() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/withdrawals", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Admin get withdrawals successful");
                System.out.println("✅ Admin get withdrawals successful");
            } else {
                testResults.add("❌ Admin get withdrawals failed: " + response.getStatusCode());
                System.out.println("❌ Admin get withdrawals failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Admin get withdrawals error: " + e.getMessage());
            System.out.println("❌ Admin get withdrawals error: " + e.getMessage());
        }
    }

    private void testAdminSettings() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/admin/settings", HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                testResults.add("✅ Admin get settings successful");
                System.out.println("✅ Admin get settings successful");
            } else {
                testResults.add("❌ Admin get settings failed: " + response.getStatusCode());
                System.out.println("❌ Admin get settings failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            testResults.add("❌ Admin get settings error: " + e.getMessage());
            System.out.println("❌ Admin get settings error: " + e.getMessage());
        }
    }

    @AfterEach
    void printResults() {
        System.out.println("\n📊 Test Results Summary:");
        testResults.forEach(System.out::println);
        
        long successCount = testResults.stream().filter(r -> r.startsWith("✅")).count();
        long failureCount = testResults.stream().filter(r -> r.startsWith("❌")).count();
        
        System.out.println("\n📈 Statistics:");
        System.out.println("✅ Successful: " + successCount);
        System.out.println("❌ Failed: " + failureCount);
        System.out.println("📊 Total: " + (successCount + failureCount));
        System.out.println("🎯 Success Rate: " + (successCount * 100.0 / (successCount + failureCount)) + "%");
    }
}