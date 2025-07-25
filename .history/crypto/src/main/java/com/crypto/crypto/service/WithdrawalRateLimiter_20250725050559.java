package com.crypto.crypto.service;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Component
public class WithdrawalRateLimiter {
    private final Map<Long, List<LocalDateTime>> userWithdrawals = new ConcurrentHashMap<>();
    
    public boolean canWithdraw(Long userId) {
        List<LocalDateTime> withdrawals = userWithdrawals.computeIfAbsent(userId, k -> new ArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        withdrawals.removeIf(time -> time.isBefore(now.minusHours(24)));
        return withdrawals.size() < 3;
    }
    
    public void recordWithdrawal(Long userId) {
        userWithdrawals.computeIfAbsent(userId, k -> new ArrayList<>()).add(LocalDateTime.now());
    }
} 