package com.crypto.crypto.controller;
import com.crypto.crypto.dto.AdminDTOs;
import com.crypto.crypto.dto.PlanDTOs;
import com.crypto.crypto.service.UserService;
import com.crypto.crypto.service.DepositService;
import com.crypto.crypto.service.WithdrawalService;
import com.crypto.crypto.service.PromoCodeService;
import com.crypto.crypto.service.AdminSettingsService;
import com.crypto.crypto.service.DailyCounterService;
import com.crypto.crypto.service.WalletService;
import com.crypto.crypto.service.PlanService;
import com.crypto.crypto.entity.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private WithdrawalService withdrawalService;
    @Autowired
    private PromoCodeService promoCodeService;
    @Autowired
    private AdminSettingsService adminSettingsService;
    @Autowired
    private DailyCounterService dailyCounterService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private PlanService planService;
    // ... existing code ...
} 