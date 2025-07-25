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
    
    public TransactionDTOs.WalletChangeResponse createWalletChangeRequest(User user, TransactionDTOs.WalletChangeRequest request) {
        // Validate TRC20 address format
        if (!isValidTRC20Address(request.getNewAddress())) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format");
        }
        
        // Check if user has a wallet
        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        if (wallet == null) {
            throw new RuntimeException("No wallet found. Please set your wallet address first.");
        }
        
        // Check if new address is different from current
        if (wallet.getUsdtAddress().equals(request.getNewAddress())) {
            throw new RuntimeException("New address must be different from current address");
        }
        
        // Check if user has pending requests
        long pendingCount = walletChangeRequestRepository.countPendingRequestsByUser(user);
        if (pendingCount > 0) {
            throw new RuntimeException("You already have a pending wallet change request. Please wait for admin approval.");
        }
        
        // Create change request
        WalletChangeRequest changeRequest = new WalletChangeRequest();
        changeRequest.setUser(user);
        changeRequest.setCurrentAddress(wallet.getUsdtAddress());
        changeRequest.setNewAddress(request.getNewAddress());
        changeRequest.setReason(request.getReason());
        changeRequest.setStatus(WalletChangeStatus.PENDING);
        
        walletChangeRequestRepository.save(changeRequest);
        
        return convertToWalletChangeResponse(changeRequest);
    }
    
    public TransactionDTOs.WalletChangeHistoryResponse getWalletChangeHistory(User user) {
        List<WalletChangeRequest> requests = walletChangeRequestRepository.findByUserOrderByCreatedAtDesc(user);
        
        List<TransactionDTOs.WalletChangeResponse> responses = requests.stream()
                .map(this::convertToWalletChangeResponse)
                .collect(Collectors.toList());
        
        return new TransactionDTOs.WalletChangeHistoryResponse(responses);
    }
    
    // Admin methods
    public void approveWalletChangeRequest(Long requestId, User admin, String adminNotes) {
        WalletChangeRequest request = walletChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Wallet change request not found"));
        
        if (request.getStatus() != WalletChangeStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }
        
        // Update wallet address
        Wallet wallet = walletRepository.findByUser(request.getUser()).orElse(null);
        if (wallet == null) {
            throw new RuntimeException("User wallet not found");
        }
        
        wallet.setUsdtAddress(request.getNewAddress());
        walletRepository.save(wallet);
        
        // Update request status
        request.setStatus(WalletChangeStatus.APPROVED);
        request.setProcessedBy(admin);
        request.setAdminNotes(adminNotes);
        request.setProcessedAt(LocalDateTime.now());
        
        walletChangeRequestRepository.save(request);
    }
    
    public void rejectWalletChangeRequest(Long requestId, User admin, String adminNotes) {
        WalletChangeRequest request = walletChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Wallet change request not found"));
        
        if (request.getStatus() != WalletChangeStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }
        
        // Update request status
        request.setStatus(WalletChangeStatus.REJECTED);
        request.setProcessedBy(admin);
        request.setAdminNotes(adminNotes);
        request.setProcessedAt(LocalDateTime.now());
        
        walletChangeRequestRepository.save(request);
    }
    
    public List<WalletChangeRequest> getAllPendingRequests() {
        return walletChangeRequestRepository.findByStatus(WalletChangeStatus.PENDING);
    }
    
    public List<WalletChangeRequest> getAllRequests() {
        return walletChangeRequestRepository.findAll();
    }
    
    private boolean isValidTRC20Address(String address) {
        return address != null && 
               address.length() == 34 && 
               address.startsWith("T") &&
               address.matches("^T[A-Za-z0-9]{33}$");
    }
    
    private TransactionDTOs.WalletChangeResponse convertToWalletChangeResponse(WalletChangeRequest request) {
        TransactionDTOs.WalletChangeResponse response = new TransactionDTOs.WalletChangeResponse();
        response.setId(request.getId());
        response.setCurrentAddress(request.getCurrentAddress());
        response.setNewAddress(request.getNewAddress());
        response.setReason(request.getReason());
        response.setStatus(request.getStatus().name());
        response.setAdminNotes(request.getAdminNotes());
        response.setCreatedAt(request.getCreatedAt());
        response.setProcessedAt(request.getProcessedAt());
        return response;
    }
} 