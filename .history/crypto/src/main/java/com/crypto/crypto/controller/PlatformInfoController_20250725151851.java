package com.crypto.crypto.controller;

import com.crypto.crypto.service.AdminSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequestMapping("/api/platform")
public class PlatformInfoController {
    @Autowired
    private AdminSettingsService adminSettingsService;

    @GetMapping("/info")
    public ResponseEntity<?> getPlatformInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("platformName", "منصة الاستثمار");
            info.put("usdtWallet", adminSettingsService.getStringSetting("platform_wallet", "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"));
            info.put("minWithdrawal", 10.00);
            info.put("withdrawalFee", 2.00);
            info.put("maintenanceMode", adminSettingsService.getBooleanSetting("maintenance_mode", false));
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
} 