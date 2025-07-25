package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.AdminSettings;
import com.crypto.crypto.repository.AdminSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminSettingsService {
    @Autowired
    private AdminSettingsRepository adminSettingsRepository;
    
    public AdminDTOs.AdminSettingsResponse getAdminSettings() {
        try {
            AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
            response.setMaintenanceMode(getBooleanSetting("maintenance_mode", false));
            response.setAboutContent(getStringSetting("about_content", "Welcome to our investment platform!"));
            response.setUsdtWalletAddress(getStringSetting("platform_wallet", "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"));
            response.setDefaultUsageLimit(getIntegerSetting("default_usage_limit", 100));
            return response;
        } catch (Exception e) {
            System.err.println("Error getting admin settings: " + e.getMessage());
            // Return default settings if there's an error
            AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
            response.setMaintenanceMode(false);
            response.setAboutContent("Welcome to our investment platform!");
            response.setUsdtWalletAddress("TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE");
            response.setDefaultUsageLimit(100);
            return response;
        }
    }
    
    public void setMaintenanceMode(boolean enabled) {
        saveSetting("maintenance_mode", String.valueOf(enabled));
    }
    
    public void updateAboutContent(String content) {
        saveSetting("about_content", content);
    }
    
    public int getDefaultUsageLimit() {
        return getIntegerSetting("default_usage_limit", 100);
    }
    
    public void updateDefaultUsageLimit(int usageLimit) {
        saveSetting("default_usage_limit", String.valueOf(usageLimit));
    }
    
    // Enhanced helper methods with better error handling
    public String getStringSetting(String key, String defaultValue) {
        try {
            return adminSettingsRepository.findByKeyName(key)
                    .map(AdminSettings::getValue)
                    .orElse(defaultValue);
        } catch (Exception e) {
            System.err.println("Error getting string setting '" + key + "': " + e.getMessage());
            return defaultValue;
        }
    }
    
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        try {
            return adminSettingsRepository.findByKeyName(key)
                    .map(setting -> {
                        try {
                            return Boolean.parseBoolean(setting.getValue());
                        } catch (Exception e) {
                            System.err.println("Error parsing boolean setting '" + key + "' value '" + setting.getValue() + "': " + e.getMessage());
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (Exception e) {
            System.err.println("Error getting boolean setting '" + key + "': " + e.getMessage());
            return defaultValue;
        }
    }
    
    public int getIntegerSetting(String key, int defaultValue) {
        try {
            return adminSettingsRepository.findByKeyName(key)
                    .map(setting -> {
                        try {
                            return Integer.parseInt(setting.getValue());
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing integer setting '" + key + "' value '" + setting.getValue() + "': " + e.getMessage());
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (Exception e) {
            System.err.println("Error getting integer setting '" + key + "': " + e.getMessage());
            return defaultValue;
        }
    }
    
    private void saveSetting(String key, String value) {
        try {
            AdminSettings setting = adminSettingsRepository.findByKeyName(key)
                    .orElse(new AdminSettings());
            setting.setKeyName(key);
            setting.setValue(value);
            if (setting.getId() == null) {
                setting.setDescription("Auto-generated setting");
            }
            adminSettingsRepository.save(setting);
        } catch (Exception e) {
            System.err.println("Error saving setting '" + key + "' with value '" + value + "': " + e.getMessage());
            throw new RuntimeException("Failed to save setting: " + e.getMessage());
        }
    }
} 