package com.crypto.crypto.service;

import com.crypto.crypto.dto.TransactionDTOs;
import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Wallet;
import com.crypto.crypto.entity.WalletChangeRequest;
import com.crypto.crypto.entity.WalletChangeStatus;
import com.crypto.crypto.repository.WalletRepository;
import com.crypto.crypto.repository.WalletChangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private WalletChangeRequestRepository walletChangeRequestRepository;
    
    public void saveWalletAddress(User user, String usdtAddress) {
        Wallet existingWallet = walletRepository.findByUser(user).orElse(null);
        
        if (existingWallet != null && existingWallet.getAddressSet()) {
            throw new RuntimeException("Wallet address has already been set. You cannot change it yourself. Please submit a change request to admin.");
        }
        
        if (existingWallet != null && existingWallet.getIsLocked()) {
            throw new RuntimeException("Wallet address is locked. Only admin can update.");
        }
        
        if (existingWallet != null) {
            existingWallet.setUsdtAddress(usdtAddress);
            existingWallet.setAddressSet(true);
            walletRepository.save(existingWallet);
        } else {
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setUsdtAddress(usdtAddress);
            wallet.setAddressSet(true);
            walletRepository.save(wallet);
        }
    }
    
    public TransactionDTOs.WalletResponse getWalletInfo(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        
        TransactionDTOs.WalletResponse response = new TransactionDTOs.WalletResponse();
        if (wallet != null) {
            response.setUsdtAddress(wallet.getUsdtAddress());
            response.setLocked(wallet.getIsLocked());
            response.setAddressSet(wallet.getAddressSet());
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