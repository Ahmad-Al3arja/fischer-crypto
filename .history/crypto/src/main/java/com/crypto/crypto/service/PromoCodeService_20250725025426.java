package com.crypto.crypto.service;

import com.crypto.crypto.entity.PromoCode;
import com.crypto.crypto.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PromoCodeService {
    @Autowired
    private PromoCodeRepository promoCodeRepository;

    public PromoCode validatePromoCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Promo code not found"));
        if (!promoCode.getIsActive()) {
            throw new RuntimeException("Promo code is not active");
        }
        if (promoCode.getUsageLimit() != null && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new RuntimeException("Promo code usage limit reached");
        }
        if (promoCode.getExpiresAt() != null && promoCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Promo code has expired");
        }
        return promoCode;
    }
} 