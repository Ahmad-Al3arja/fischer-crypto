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
        AdminSettings settings = getOrCreateSettings();
        
        AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
        response.setMaintenanceMode(settings.isMaintenanceMode());
        response.setAboutContent(settings.getAboutContent());
        response.setUsdtWalletAddress(usdtWalletAddress);
        
        return response;
    }
    
    public void setMaintenanceMode(boolean enabled) {
        AdminSettings settings = getOrCreateSettings();
        settings.setMaintenanceMode(enabled);
        adminSettingsRepository.save(settings);
    }
    
    public void updateAboutContent(String content) {
        AdminSettings settings = getOrCreateSettings();
        settings.setAboutContent(content);
        adminSettingsRepository.save(settings);
    }
    
    private AdminSettings getOrCreateSettings() {
        return adminSettingsRepository.findFirst()
                .orElseGet(() -> {
                    AdminSettings settings = new AdminSettings();
                    settings.setMaintenanceMode(false);
                    settings.setAboutContent("Welcome to our investment platform!");
                    return adminSettingsRepository.save(settings);
                });
    }
} 