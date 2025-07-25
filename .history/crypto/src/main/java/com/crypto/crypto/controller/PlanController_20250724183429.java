// Enhanced PlanController.java
package com.crypto.crypto.controller;

import com.crypto.crypto.dto.PlanDTOs;
import com.crypto.crypto.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanController {
    
    @Autowired
    private PlanService planService;
    
    // Public endpoint - all users can view plans
    @GetMapping
    public ResponseEntity<?> getAllPlans() {
        try {
            PlanDTOs.PlansListResponse response = planService.getAllPlans();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlanById(@PathVariable Long planId) {
        try {
            PlanDTOs.PlanResponse response = planService.getPlanById(planId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Admin-only endpoints for plan management
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPlan(@Valid @RequestBody PlanDTOs.CreatePlanRequest request) {
        try {
            // Validate plan data
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Plan price must be greater than 0");
            }
            if (request.getDailyProfitMin().compareTo(request.getDailyProfitMax()) > 0) {
                throw new RuntimeException("Daily profit minimum cannot be greater than maximum");
            }
            if (request.getPlanLevel() <= 0) {
                throw new RuntimeException("Plan level must be greater than 0");
            }
            
            PlanDTOs.PlanResponse response = planService.createPlan(request);
            return ResponseEntity.ok(new SuccessResponse("Plan created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePlan(@PathVariable Long planId, @Valid @RequestBody PlanDTOs.UpdatePlanRequest request) {
        try {
            // Validate update data
            if (request.getPrice() != null && request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Plan price must be greater than 0");
            }
            if (request.getDailyProfitMin() != null && request.getDailyProfitMax() != null &&
                request.getDailyProfitMin().compareTo(request.getDailyProfitMax()) > 0) {
                throw new RuntimeException("Daily profit minimum cannot be greater than maximum");
            }
            if (request.getPlanLevel() != null && request.getPlanLevel() <= 0) {
                throw new RuntimeException("Plan level must be greater than 0");
            }
            
            PlanDTOs.PlanResponse response = planService.updatePlan(planId, request);
            return ResponseEntity.ok(new SuccessResponse("Plan updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            planService.deletePlan(planId);
            return ResponseEntity.ok(new SuccessResponse("Plan deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Get plan statistics for admin
    @GetMapping("/{planId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPlanStats(@PathVariable Long planId) {
        try {
            PlanDTOs.PlanStatsResponse response = planService.getPlanStats(planId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // Response classes
    private static class ErrorResponse {
        private String message;
        private String status = "error";
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }
    
    private static class SuccessResponse {
        private String message;
        private String status = "success";
        private Object data;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public SuccessResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }
        
        public String getMessage() { return message; }
        public String getStatus() { return status; }
        public Object getData() { return data; }
    }
}