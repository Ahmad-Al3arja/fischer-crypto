package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.AdminSettings;
import com.crypto.crypto.repository.AdminSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminSettingsService {
    
    @Autowired
    private AdminSettingsRepository adminSettingsRepository;
    
    @Value("${app.platform.usdt-wallet}")
    private String usdtWalletAddress;
    
    public AdminDTOs.AdminSettingsResponse getAdminSettings() {
        AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
        response.setMaintenanceMode(getMaintenanceMode());
        response.setAboutContent(getAboutContent());
        response.setUsdtWalletAddress(usdtWalletAddress);
        
        return response;
    }
    
    public void setMaintenanceMode(boolean enabled) {
        setSetting("maintenance_mode", String.valueOf(enabled), "Platform maintenance mode");
    }
    
    public void updateAboutContent(String content) {
        setSetting("about_content", content, "About platform content");
    }
    
    private boolean getMaintenanceMode() {
        String value = getSettingValue("maintenance_mode");
        return value != null && Boolean.parseBoolean(value);
    }
    
    private String getAboutContent() {
        String value = getSettingValue("about_content");
        return value != null ? value : "Welcome to our investment platform!";
    }
    
    public String getSettingValue(String key) {
        return adminSettingsRepository.findBySettingKey(key)
                .map(AdminSettings::getSettingValue)
                .orElse(null);
    }
    
    public void setSetting(String key, String value, String description) {
        AdminSettings setting = adminSettingsRepository.findBySettingKey(key)
                .orElse(new AdminSettings());
        
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        
        // Set timestamps for new entities
        if (setting.getId() == null) {
            setting.setCreatedAt(java.time.LocalDateTime.now());
        }
        setting.setUpdatedAt(java.time.LocalDateTime.now());
        
        adminSettingsRepository.save(setting);
    }
} 