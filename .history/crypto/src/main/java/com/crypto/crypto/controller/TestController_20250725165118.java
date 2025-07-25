package com.crypto.crypto.controller;

import com.crypto.crypto.entity.Plan;
import com.crypto.crypto.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private PlanRepository planRepository;
    
    @GetMapping("/plans")
    public ResponseEntity<?> testPlans() {
        try {
            List<Plan> plans = planRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Plans retrieved successfully");
            response.put("count", plans.size());
            response.put("plans", plans);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("message", "Error retrieving plans");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Test database connection
            long planCount = planRepository.count();
            details.put("database", "UP");
            details.put("plans_count", planCount);
            
            // Test application status
            details.put("application", "UP");
            details.put("version", "1.0.0");
            
            response.put("status", "UP");
            response.put("message", "Application is healthy");
            response.put("timestamp", System.currentTimeMillis());
            response.put("details", details);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            details.put("database", "DOWN");
            details.put("error", e.getMessage());
            
            response.put("status", "DOWN");
            response.put("message", "Application is unhealthy");
            response.put("timestamp", System.currentTimeMillis());
            response.put("details", details);
            
            return ResponseEntity.status(503).body(response);
        }
    }

    @PostMapping("/insert-plans")
    public ResponseEntity<?> insertPlans() {
        try {
            // Check if plans already exist
            List<Plan> existingPlans = planRepository.findAll();
            if (!existingPlans.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Plans already exist");
                response.put("count", existingPlans.size());
                response.put("plans", existingPlans);
                return ResponseEntity.ok(response);
            }
            // Insert the correct 9 plans as per requirements
            Plan level1 = new Plan();
            level1.setName("المستوى الأول");
            level1.setPrice(new BigDecimal("60.00"));
            level1.setMonthlyProfit(new BigDecimal("45.00"));
            level1.setDailyProfitMin(new BigDecimal("1.20"));
            level1.setDailyProfitMax(new BigDecimal("1.80"));
            level1.setPlanLevel(1);
            planRepository.save(level1);
            Plan level2 = new Plan();
            level2.setName("المستوى الثاني");
            level2.setPrice(new BigDecimal("150.00"));
            level2.setMonthlyProfit(new BigDecimal("110.00"));
            level2.setDailyProfitMin(new BigDecimal("2.40"));
            level2.setDailyProfitMax(new BigDecimal("4.90"));
            level2.setPlanLevel(2);
            planRepository.save(level2);
            Plan level3 = new Plan();
            level3.setName("المستوى الثالث");
            level3.setPrice(new BigDecimal("300.00"));
            level3.setMonthlyProfit(new BigDecimal("175.00"));
            level3.setDailyProfitMin(new BigDecimal("4.90"));
            level3.setDailyProfitMax(new BigDecimal("6.90"));
            level3.setPlanLevel(3);
            planRepository.save(level3);
            Plan level4 = new Plan();
            level4.setName("المستوى الرابع");
            level4.setPrice(new BigDecimal("500.00"));
            level4.setMonthlyProfit(new BigDecimal("245.00"));
            level4.setDailyProfitMin(new BigDecimal("6.90"));
            level4.setDailyProfitMax(new BigDecimal("9.90"));
            level4.setPlanLevel(4);
            planRepository.save(level4);
            Plan level5 = new Plan();
            level5.setName("المستوى الخامس");
            level5.setPrice(new BigDecimal("800.00"));
            level5.setMonthlyProfit(new BigDecimal("505.00"));
            level5.setDailyProfitMin(new BigDecimal("11.90"));
            level5.setDailyProfitMax(new BigDecimal("22.00"));
            level5.setPlanLevel(5);
            planRepository.save(level5);
            Plan level6 = new Plan();
            level6.setName("المستوى السادس");
            level6.setPrice(new BigDecimal("1500.00"));
            level6.setMonthlyProfit(new BigDecimal("1020.00"));
            level6.setDailyProfitMin(new BigDecimal("23.00"));
            level6.setDailyProfitMax(new BigDecimal("45.00"));
            level6.setPlanLevel(6);
            planRepository.save(level6);
            Plan level7 = new Plan();
            level7.setName("المستوى السابع");
            level7.setPrice(new BigDecimal("3000.00"));
            level7.setMonthlyProfit(new BigDecimal("1624.50"));
            level7.setDailyProfitMin(new BigDecimal("45.00"));
            level7.setDailyProfitMax(new BigDecimal("63.30"));
            level7.setPlanLevel(7);
            planRepository.save(level7);
            Plan level8 = new Plan();
            level8.setName("المستوى الثامن");
            level8.setPrice(new BigDecimal("5000.00"));
            level8.setMonthlyProfit(new BigDecimal("2374.50"));
            level8.setDailyProfitMin(new BigDecimal("63.30"));
            level8.setDailyProfitMax(new BigDecimal("95.00"));
            level8.setPlanLevel(8);
            planRepository.save(level8);
            Plan level9 = new Plan();
            level9.setName("المستوى التاسع");
            level9.setPrice(new BigDecimal("8000.00"));
            level9.setMonthlyProfit(new BigDecimal("5224.50"));
            level9.setDailyProfitMin(new BigDecimal("125.00"));
            level9.setDailyProfitMax(new BigDecimal("223.30"));
            level9.setPlanLevel(9);
            planRepository.save(level9);
            List<Plan> allPlans = planRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Correct 9 plans inserted successfully");
            response.put("count", allPlans.size());
            response.put("plans", allPlans);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("message", "Error inserting plans");
            return ResponseEntity.badRequest().body(response);
        }
    }
} 