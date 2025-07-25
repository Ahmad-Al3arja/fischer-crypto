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
            
            // Insert plans
            Plan starter = new Plan();
            starter.setName("Starter");
            starter.setPrice(new BigDecimal("100.00"));
            starter.setMonthlyProfit(new BigDecimal("10.00"));
            starter.setDailyProfitMin(new BigDecimal("0.30"));
            starter.setDailyProfitMax(new BigDecimal("0.40"));
            starter.setPlanLevel(1);
            planRepository.save(starter);
            
            Plan silver = new Plan();
            silver.setName("Silver");
            silver.setPrice(new BigDecimal("500.00"));
            silver.setMonthlyProfit(new BigDecimal("60.00"));
            silver.setDailyProfitMin(new BigDecimal("1.80"));
            silver.setDailyProfitMax(new BigDecimal("2.20"));
            silver.setPlanLevel(2);
            planRepository.save(silver);
            
            Plan gold = new Plan();
            gold.setName("Gold");
            gold.setPrice(new BigDecimal("1000.00"));
            gold.setMonthlyProfit(new BigDecimal("130.00"));
            gold.setDailyProfitMin(new BigDecimal("4.00"));
            gold.setDailyProfitMax(new BigDecimal("4.80"));
            gold.setPlanLevel(3);
            planRepository.save(gold);
            
            Plan platinum = new Plan();
            platinum.setName("Platinum");
            platinum.setPrice(new BigDecimal("2500.00"));
            platinum.setMonthlyProfit(new BigDecimal("350.00"));
            platinum.setDailyProfitMin(new BigDecimal("10.50"));
            platinum.setDailyProfitMax(new BigDecimal("12.60"));
            platinum.setPlanLevel(4);
            planRepository.save(platinum);
            
            Plan diamond = new Plan();
            diamond.setName("Diamond");
            diamond.setPrice(new BigDecimal("5000.00"));
            diamond.setMonthlyProfit(new BigDecimal("750.00"));
            diamond.setDailyProfitMin(new BigDecimal("22.50"));
            diamond.setDailyProfitMax(new BigDecimal("27.00"));
            diamond.setPlanLevel(5);
            planRepository.save(diamond);
            
            List<Plan> allPlans = planRepository.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Plans inserted successfully");
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