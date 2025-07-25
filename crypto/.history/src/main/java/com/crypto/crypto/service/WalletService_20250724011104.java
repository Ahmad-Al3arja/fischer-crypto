package com.crypto.crypto.service;

import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Wallet;
import com.crypto.crypto.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WalletService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    public void saveWalletAddress(User user, String usdtAddress) {
        Wallet existingWallet = walletRepository.findByUser(user).orElse(null);
        
        if (existingWallet != null && existingWallet.getIsLocked()) {
            throw new RuntimeException("Wallet address is locked. Only admin can update.");
        }
        
        if (existingWallet != null) {
            existingWallet.setUsdtAddress(usdtAddress);
            walletRepository.save(existingWallet);
        } else {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setUsdtAddress(usdtAddress);
            walletRepository.save(wallet);
        }
    }
    
    public Wallet getUserWallet(User user) {
        return walletRepository.findByUser(user).orElse(null);
    }
    
    public void lockWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setIsLocked(true);
        walletRepository.save(wallet);
    }
    
    public void updateWalletAddress(Long walletId, String newAddress) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setUsdtAddress(newAddress);
        walletRepository.save(wallet);
    }
} 