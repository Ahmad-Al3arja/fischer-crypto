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
        AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
        response.setMaintenanceMode(getBooleanSetting("maintenance_mode", false));
        response.setAboutContent(getStringSetting("about_content", "Welcome to our investment platform!"));
        response.setUsdtWalletAddress(getStringSetting("platform_wallet", "TQn9Y2khEsLJW1ChVWFMSMeRDow5KcbLSE"));
        response.setDefaultUsageLimit(getIntegerSetting("default_usage_limit", 100));
        return response;
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
    
    // Helper methods
    public String getStringSetting(String key, String defaultValue) {
        return adminSettingsRepository.findByKeyName(key)
                .map(AdminSettings::getValue)
                .orElse(defaultValue);
    }
    
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        return adminSettingsRepository.findByKeyName(key)
                .map(setting -> Boolean.parseBoolean(setting.getValue()))
                .orElse(defaultValue);
    }
    
    private int getIntegerSetting(String key, int defaultValue) {
        return adminSettingsRepository.findByKeyName(key)
                .map(setting -> Integer.parseInt(setting.getValue()))
                .orElse(defaultValue);
    }
    
    private void saveSetting(String key, String value) {
        AdminSettings setting = adminSettingsRepository.findByKeyName(key)
                .orElse(new AdminSettings());
        setting.setKeyName(key);
        setting.setValue(value);
        if (setting.getId() == null) {
            setting.setDescription("Auto-generated setting");
        }
        adminSettingsRepository.save(setting);
    }
} 