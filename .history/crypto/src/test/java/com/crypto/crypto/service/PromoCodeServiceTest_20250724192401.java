package com.crypto.crypto.service;

import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.entity.PromoCode;
import com.crypto.crypto.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @Mock
    private AdminSettingsService adminSettingsService;

    @InjectMocks
    private PromoCodeService promoCodeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPromoCode_WithNullUsageLimit_ShouldUseDefault() {
        // Arrange
        AdminDTOs.CreatePromoCodeRequest request = new AdminDTOs.CreatePromoCodeRequest();
        request.setCode("TEST123");
        request.setBonusValue(new BigDecimal("10.00"));
        request.setUsageLimit(null); // No usage limit provided

        when(promoCodeRepository.existsByCode("TEST123")).thenReturn(false);
        when(adminSettingsService.getDefaultUsageLimit()).thenReturn(100);
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
            PromoCode saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        AdminDTOs.PromoCodeResponse response = promoCodeService.createPromoCode(request);

        // Assert
        assertNotNull(response);
        assertEquals(100, response.getUsageLimit());
        verify(adminSettingsService).getDefaultUsageLimit();
        verify(promoCodeRepository).save(any(PromoCode.class));
    }

    @Test
    void createPromoCode_WithZeroUsageLimit_ShouldUseDefault() {
        // Arrange
        AdminDTOs.CreatePromoCodeRequest request = new AdminDTOs.CreatePromoCodeRequest();
        request.setCode("TEST456");
        request.setBonusValue(new BigDecimal("20.00"));
        request.setUsageLimit(0); // Zero usage limit

        when(promoCodeRepository.existsByCode("TEST456")).thenReturn(false);
        when(adminSettingsService.getDefaultUsageLimit()).thenReturn(100);
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
            PromoCode saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        AdminDTOs.PromoCodeResponse response = promoCodeService.createPromoCode(request);

        // Assert
        assertNotNull(response);
        assertEquals(100, response.getUsageLimit());
        verify(adminSettingsService).getDefaultUsageLimit();
    }

    @Test
    void createPromoCode_WithValidUsageLimit_ShouldUseProvided() {
        // Arrange
        AdminDTOs.CreatePromoCodeRequest request = new AdminDTOs.CreatePromoCodeRequest();
        request.setCode("TEST789");
        request.setBonusValue(new BigDecimal("30.00"));
        request.setUsageLimit(50); // Valid usage limit

        when(promoCodeRepository.existsByCode("TEST789")).thenReturn(false);
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(invocation -> {
            PromoCode saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        // Act
        AdminDTOs.PromoCodeResponse response = promoCodeService.createPromoCode(request);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getUsageLimit());
        verify(adminSettingsService, never()).getDefaultUsageLimit();
    }
} 