package com.crypto.crypto.service;

import com.crypto.crypto.dto.UserDTOs;
import com.crypto.crypto.entity.*;
import com.crypto.crypto.repository.ReferralEarningRepository;
import com.crypto.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReferralService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReferralEarningRepository referralEarningRepository;
    
    @Autowired
    private UserService userService;
    
    // FIX: Correct commission rates as per requirements
    private static final BigDecimal DIRECT_COMMISSION_RATE = new BigDecimal("0.12"); // 12%
    private static final BigDecimal GRAND_COMMISSION_RATE = new BigDecimal("0.06"); // 6%
    
    public void processReferralCommissions(User referredUser, Deposit deposit) {
        BigDecimal depositAmount = deposit.getAmount();
        
        // Process direct referrer commission (12%)
        if (referredUser.getReferrer() != null) {
            BigDecimal directCommission = depositAmount.multiply(DIRECT_COMMISSION_RATE);
            processCommission(referredUser.getReferrer(), referredUser, deposit, 
                    directCommission, CommissionType.DIRECT);
        }
        
        // Process grand referrer commission (6%)
        if (referredUser.getGrandReferrer() != null) {
            BigDecimal grandCommission = depositAmount.multiply(GRAND_COMMISSION_RATE);
            processCommission(referredUser.getGrandReferrer(), referredUser, deposit, 
                    grandCommission, CommissionType.GRAND);
        }
    }
    
    private void processCommission(User referrer, User referredUser, Deposit deposit, 
                                 BigDecimal commissionAmount, CommissionType commissionType) {
        // Create referral earning record
        ReferralEarning earning = new ReferralEarning();
        earning.setUser(referredUser); // The user who made the deposit
        earning.setReferrer(referrer); // The user who gets the commission
        earning.setReferredUser(referredUser); // The referred user (same as user)
        earning.setDeposit(deposit); // The deposit that triggered the commission
        earning.setAmount(commissionAmount);
        earning.setCommissionType(commissionType);
        
        referralEarningRepository.save(earning);
        
        // Add commission to referrer's balance and referral earnings
        referrer.setTotalBalance(referrer.getTotalBalance().add(commissionAmount));
        referrer.setReferralEarnings(referrer.getReferralEarnings().add(commissionAmount));
        
        userRepository.save(referrer);
    }
    
    public UserDTOs.ReferralStatsResponse getReferralStats(User user) {
        UserDTOs.ReferralStatsResponse response = new UserDTOs.ReferralStatsResponse();
        
        // Count direct referrals
        int directReferrals = (int) userRepository.countDirectReferrals(user);
        response.setTotalDirectReferrals(directReferrals);
        
        // Count second-level referrals
        int secondLevelReferrals = (int) userRepository.countSecondLevelReferrals(user);
        response.setTotalSecondLevelReferrals(secondLevelReferrals);
        
        // Get total referral earnings
        response.setTotalReferralEarnings(user.getReferralEarnings());
        
        // Generate referral link
        response.setReferralLink("https://yourapp.com/register?ref=" + user.getDisplayUsername());
        
        return response;
    }

    public void processReferralEarnings(User user, BigDecimal amount) {
        // Process direct referral commission (12%)
        if (user.getReferrer() != null) {
            BigDecimal directCommission = amount.multiply(DIRECT_COMMISSION_RATE);
            addReferralEarning(user.getReferrer(), user, directCommission, CommissionType.DIRECT);
        }
        
        // Process grand referral commission (6%)
        if (user.getGrandReferrer() != null) {
            BigDecimal grandCommission = amount.multiply(GRAND_COMMISSION_RATE);
            addReferralEarning(user.getGrandReferrer(), user, grandCommission, CommissionType.GRAND);
        }
    }
    
    public UserDTOs.ReferralListResponse getUserReferrals() {
        User user = userService.getCurrentUser();
        List<User> directReferrals = userRepository.findByReferrer(user);
        List<UserDTOs.ReferralDetail> referralDetails = directReferrals.stream()
                .map(this::convertToReferralDetail)
                .collect(Collectors.toList());
        return new UserDTOs.ReferralListResponse(referralDetails);
    }
    
    public UserDTOs.ReferralEarningsResponse getReferralEarnings() {
        User user = userService.getCurrentUser();
        List<ReferralEarning> earnings = referralEarningRepository.findByUser(user);
        BigDecimal totalEarnings = earnings.stream()
                .map(ReferralEarning::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<UserDTOs.ReferralDetail> earningDetails = earnings.stream()
                .map(this::convertToReferralDetailFromEarning)
                .collect(Collectors.toList());
        
        return new UserDTOs.ReferralEarningsResponse(totalEarnings, earningDetails);
    }
    
    private void addReferralEarning(User referrer, User referredUser, BigDecimal amount, CommissionType type) {
        ReferralEarning earning = new ReferralEarning();
        earning.setUser(referrer);
        earning.setReferredUser(referredUser);
        earning.setAmount(amount);
        earning.setCommissionType(type);
        referralEarningRepository.save(earning);
        
        // Update referrer's total referral earnings
        referrer.setReferralEarnings(referrer.getReferralEarnings().add(amount));
        userRepository.save(referrer);
    }
    
    private UserDTOs.ReferralDetail convertToReferralDetail(User referral) {
        UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
        detail.setUsername(referral.getDisplayUsername());
        detail.setPhoneNumber(referral.getPhoneNumber());
        detail.setPlanName(referral.getCurrentPlan() != null ? referral.getCurrentPlan().getName() : "No Plan");
        detail.setInvestmentAmount(referral.getTotalBalance());
        detail.setCommissionEarned(BigDecimal.ZERO); // TODO: Calculate actual commission
        detail.setLevel("DIRECT");
        detail.setJoinedAt(referral.getCreatedAt());
        return detail;
    }
    
    private UserDTOs.ReferralDetail convertToReferralDetailFromEarning(ReferralEarning earning) {
        UserDTOs.ReferralDetail detail = new UserDTOs.ReferralDetail();
        detail.setUsername(earning.getReferredUser().getDisplayUsername());
        detail.setPhoneNumber(earning.getReferredUser().getPhoneNumber());
        detail.setPlanName(earning.getReferredUser().getCurrentPlan() != null ? 
                earning.getReferredUser().getCurrentPlan().getName() : "No Plan");
        detail.setInvestmentAmount(earning.getAmount());
        detail.setCommissionEarned(earning.getAmount());
        detail.setLevel(earning.getCommissionType().name());
        detail.setJoinedAt(earning.getCreatedAt());
        return detail;
    }
} 