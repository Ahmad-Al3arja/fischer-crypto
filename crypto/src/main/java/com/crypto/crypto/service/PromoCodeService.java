package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.PromoCode;
import com.crypto.crypto.exception.CustomExceptions;
import com.crypto.crypto.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromoCodeService {
    @Autowired
    private PromoCodeRepository promoCodeRepository;

    public PromoCode validatePromoCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomExceptions.PromoCodeNotFoundException("Promo code not found"));

        if (!promoCode.getIsActive()) {
            throw new CustomExceptions.PromoCodeNotFoundException("Promo code is inactive");
        }

        if (promoCode.getExpiresAt() != null && promoCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomExceptions.PromoCodeExpiredException("Promo code has expired");
        }

        if (promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new CustomExceptions.PromoCodeUsageLimitExceededException("Promo code usage limit exceeded");
        }

        return promoCode;
    }

    public void consumePromoCode(PromoCode promoCode) {
        promoCode.setUsedCount(promoCode.getUsedCount() + 1);
        promoCodeRepository.save(promoCode);
    }

    public AdminDTOs.PromoCodeResponse createPromoCode(AdminDTOs.CreatePromoCodeRequest request) {
        // Check if code already exists
        if (promoCodeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Promo code already exists");
        }

        PromoCode promoCode = new PromoCode();
        promoCode.setCode(request.getCode());
        promoCode.setBonusValue(request.getBonusValue());
        promoCode.setUsageLimit(request.getUsageLimit());
        promoCode.setUsedCount(0);
        promoCode.setIsActive(true);
        promoCode.setExpiresAt(request.getExpiresAt());

        promoCodeRepository.save(promoCode);

        return convertToPromoCodeResponse(promoCode);
    }

    public AdminDTOs.PromoCodeListResponse getAllPromoCodes() {
        List<PromoCode> promoCodes = promoCodeRepository.findAll();
        List<AdminDTOs.PromoCodeResponse> responses = promoCodes.stream()
                .map(this::convertToPromoCodeResponse)
                .collect(Collectors.toList());
        return new AdminDTOs.PromoCodeListResponse(responses);
    }

    public void togglePromoCode(Long promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new CustomExceptions.PromoCodeNotFoundException("Promo code not found"));

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