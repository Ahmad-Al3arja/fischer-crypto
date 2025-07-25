package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
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
        
        if (existingWallet != null) {
            throw new RuntimeException("Wallet address has already been set and cannot be changed. Contact admin if you need to update it.");
        }
        
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setUsdtAddress(usdtAddress);
        wallet.setIsLocked(true); // Lock immediately after setting
        walletRepository.save(wallet);
    }
    
    public TransactionDTOs.WalletResponse getWalletInfo(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        
        TransactionDTOs.WalletResponse response = new TransactionDTOs.WalletResponse();
        if (wallet != null) {
            response.setUsdtAddress(wallet.getUsdtAddress());
            response.setLocked(wallet.getIsLocked());
            response.setCreatedAt(wallet.getCreatedAt());
            response.setUpdatedAt(wallet.getUpdatedAt());
        }
        
        return response;
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