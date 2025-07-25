// src/main/java/com/crypto/crypto/service/PromoCodeService.java
package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.PromoCode;
import com.crypto.crypto.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@Transactional
public class PromoCodeService {
    
    @Autowired
    private PromoCodeRepository promoCodeRepository;
    
    @Autowired
    private AdminSettingsService adminSettingsService;
    
    public AdminDTOs.PromoCodeResponse createPromoCode(AdminDTOs.CreatePromoCodeRequest request) {
        // Check if code already exists
        if (promoCodeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Promo code already exists");
        }
        
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(request.getCode());
        promoCode.setBonusValue(request.getBonusValue());
        
        // Use provided usage limit or get default from admin settings
        Integer usageLimit = request.getUsageLimit();
        if (usageLimit == null || usageLimit <= 0) {
            String defaultLimitStr = adminSettingsService.getSettingValue("default_promo_usage_limit");
            usageLimit = Integer.parseInt(defaultLimitStr);
        }
        promoCode.setUsageLimit(usageLimit);
        
        promoCode.setUsedCount(0);
        promoCode.setIsActive(true);
        promoCode.setExpiresAt(request.getExpiresAt());
        
        promoCodeRepository.save(promoCode);
        
        return convertToPromoCodeResponse(promoCode);
    }
    
    public AdminDTOs.PromoCodeListResponse getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        
        List<AdminDTOs.PromoCodeResponse> promoCodeResponses = promoCodes.stream()
                .map(this::convertToPromoCodeResponse)
                .collect(Collectors.toList());
        
        return new AdminDTOs.PromoCodeListResponse(promoCodeResponses);
    }
    
    public void togglePromoCode(Long promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new RuntimeException("Promo code not found"));
        
        promoCode.setIsActive(!promoCode.getIsActive());
        promoCodeRepository.save(promoCode);
    }
    
    private AdminDTOs.PromoCodeResponse convertToPromoCodeResponse(PromoCode promoCode) {
        AdminDTOs.PromoCodeResponse response = new AdminDTOs.PromoCodeResponse();
        response.setId(promoCode.getId());
        response.setCode(promoCode.getCode());
        response.setBonusValue(promoCode.getBonusValue());
        response.setUsageLimit(promoCode.getUsageLimit());
        response.setUsedCount(promoCode.getUsedCount());
        response.setActive(promoCode.getIsActive());
        response.setCreatedAt(promoCode.getCreatedAt());
        response.setExpiresAt(promoCode.getExpiresAt());
        return response;
    }
}