package com.crypto.crypto.service;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.AdminSettings;
import com.crypto.crypto.exception.CustomExceptions;
import com.crypto.crypto.repository.AdminSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
@Service
@Transactional
public class AdminSettingsService {
    @Autowired
    private AdminSettingsRepository adminSettingsRepository;
    public AdminDTOs.AdminSettingsResponse getAdminSettings() {
        AdminSettings settings = getOrCreateSettings();
        return convertToAdminSettingsResponse(settings);
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
    public int getDefaultUsageLimit() {
        AdminSettings settings = getOrCreateSettings();
        return settings.getDefaultUsageLimit();
    }
    public void updateDefaultUsageLimit(int usageLimit) {
        AdminSettings settings = getOrCreateSettings();
        settings.setDefaultUsageLimit(usageLimit);
        adminSettingsRepository.save(settings);
    }
    private AdminSettings getOrCreateSettings() {
        Optional<AdminSettings> optionalSettings = adminSettingsRepository.findFirst();
        if (optionalSettings.isPresent()) {
            return optionalSettings.get();
        } else {
            AdminSettings settings = new AdminSettings();
            settings.setMaintenanceMode(false);
            settings.setAboutContent("Welcome to our investment platform!");
            settings.setDefaultUsageLimit(100);
            return adminSettingsRepository.save(settings);
        }
    }
    private AdminDTOs.AdminSettingsResponse convertToAdminSettingsResponse(AdminSettings settings) {
        AdminDTOs.AdminSettingsResponse response = new AdminDTOs.AdminSettingsResponse();
        response.setMaintenanceMode(settings.getMaintenanceMode());
        response.setAboutContent(settings.getAboutContent());
        response.setDefaultUsageLimit(settings.getDefaultUsageLimit());
        return response;
    }
} 