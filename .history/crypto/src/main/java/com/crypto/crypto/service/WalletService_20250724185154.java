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

@Service
@Transactional
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletChangeRequestRepository walletChangeRequestRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Save wallet address - can only be done once per user
     */
    public void saveWalletAddress(User user, String usdtAddress) {
        // Check if user already has a wallet
        Wallet existingWallet = walletRepository.findByUser(user).orElse(null);
        if (existingWallet != null) {
            throw new RuntimeException("Wallet address has already been set and cannot be changed. " +
                    "Please contact admin if you need to update it or use the wallet change request feature.");
        }

        // Validate USDT address format
        if (!isValidTRC20Address(usdtAddress)) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format. " +
                    "Address must be 34 characters long and start with 'T'.");
        }

        // Check if address is already used by another user
        if (walletRepository.existsByUsdtAddress(usdtAddress)) {
            throw new RuntimeException("This wallet address is already registered by another user.");
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setUsdtAddress(usdtAddress);
        wallet.setIsLocked(true); // Lock immediately after setting
        walletRepository.save(wallet);

        // Send notification to admin
        notificationService.notifyAdminNewWallet(user, usdtAddress);
    }

    /**
     * Request wallet address change - requires admin approval
     */
    public void requestWalletChange(User user, String newUsdtAddress, String reason) {
        // Check if user has an existing wallet
        Wallet existingWallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No wallet found. Please set your wallet address first."));

        // Validate new USDT address format
        if (!isValidTRC20Address(newUsdtAddress)) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format. " +
                    "Address must be 34 characters long and start with 'T'.");
        }

        // Check if new address is different from current
        if (existingWallet.getUsdtAddress().equals(newUsdtAddress)) {
            throw new RuntimeException("New wallet address cannot be the same as current address.");
        }

        // Check if address is already used by another user
        if (walletRepository.existsByUsdtAddressAndUserNot(newUsdtAddress, user)) {
            throw new RuntimeException("This wallet address is already registered by another user.");
        }

        // Check if user has pending request
        if (walletChangeRequestRepository.existsByUserAndStatus(user, WalletChangeStatus.PENDING)) {
            throw new RuntimeException("You already have a pending wallet change request. " +
                    "Please wait for admin approval or cancellation.");
        }

        // Validate reason
        if (reason == null || reason.trim().length() < 10) {
            throw new RuntimeException("Please provide a detailed reason for changing your wallet address (minimum 10 characters).");
        }

        // Create change request
        WalletChangeRequest request = new WalletChangeRequest();
        request.setUser(user);
        request.setCurrentAddress(existingWallet.getUsdtAddress());
        request.setNewAddress(newUsdtAddress);
        request.setReason(reason.trim());
        request.setStatus(WalletChangeStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        
        walletChangeRequestRepository.save(request);

        // Send notification to admin
        notificationService.notifyAdminWalletChangeRequest(user, request);
    }

    public TransactionDTOs.WalletResponse getWalletInfo(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElse(null);
        
        TransactionDTOs.WalletResponse response = new TransactionDTOs.WalletResponse();
        
        if (wallet != null) {
            response.setUsdtAddress(wallet.getUsdtAddress());
            response.setLocked(wallet.getIsLocked());
            response.setCreatedAt(wallet.getCreatedAt());
            response.setUpdatedAt(wallet.getUpdatedAt());

            // Check for pending change requests
            WalletChangeRequest pendingRequest = walletChangeRequestRepository
                    .findByUserAndStatus(user, WalletChangeStatus.PENDING).orElse(null);
            
            response.setHasPendingChangeRequest(pendingRequest != null);
            if (pendingRequest != null) {
                response.setPendingNewAddress(pendingRequest.getNewAddress());
                response.setPendingRequestDate(pendingRequest.getRequestedAt());
                response.setChangeRequestStatus(pendingRequest.getStatus().name());
            }
        } else {
            // User hasn't set wallet address yet
            response.setHasPendingChangeRequest(false);
        }

        return response;
    }

    // Admin methods
    public List<WalletChangeRequest> getPendingWalletChangeRequests() {
        return walletChangeRequestRepository.findByStatusOrderByRequestedAtAsc(WalletChangeStatus.PENDING);
    }

    public void approveWalletChangeRequest(Long requestId, Long adminId) {
        WalletChangeRequest request = walletChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Wallet change request not found"));

        if (request.getStatus() != WalletChangeStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        // Check if new address is still available
        if (walletRepository.existsByUsdtAddressAndUserNot(request.getNewAddress(), request.getUser())) {
            throw new RuntimeException("The requested wallet address is now taken by another user.");
        }

        // Update wallet
        Wallet wallet = walletRepository.findByUser(request.getUser())
                .orElseThrow(() -> new RuntimeException("User wallet not found"));
        
        String oldAddress = wallet.getUsdtAddress();
        wallet.setUsdtAddress(request.getNewAddress());
        walletRepository.save(wallet);

        // Update request status
        request.setStatus(WalletChangeStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(adminId);
        walletChangeRequestRepository.save(request);

        // Send notification to user
        notificationService.notifyUserWalletChangeApproved(request.getUser(), oldAddress, request.getNewAddress());
    }

    public void rejectWalletChangeRequest(Long requestId, Long adminId, String rejectionReason) {
        WalletChangeRequest request = walletChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Wallet change request not found"));

        if (request.getStatus() != WalletChangeStatus.PENDING) {
            throw new RuntimeException("Request is not pending");
        }

        // Update request status
        request.setStatus(WalletChangeStatus.REJECTED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(adminId);
        request.setRejectionReason(rejectionReason);
        walletChangeRequestRepository.save(request);

        // Send notification to user
        notificationService.notifyUserWalletChangeRejected(request.getUser(), rejectionReason);
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

    public void unlockWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setIsLocked(false);
        walletRepository.save(wallet);
    }

    public void updateWalletAddressByUser(User user, String newAddress) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Validate address format
        if (!isValidTRC20Address(newAddress)) {
            throw new RuntimeException("Invalid USDT TRC20 wallet address format");
        }

        // Check if address is already used
        if (walletRepository.existsByUsdtAddressAndUserNot(newAddress, user)) {
            throw new RuntimeException("This wallet address is already registered by another user");
        }

        wallet.setUsdtAddress(newAddress);
        walletRepository.save(wallet);
    }

    /**
     * Enhanced TRC20 address validation
     */
    private boolean isValidTRC20Address(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }

        address = address.trim();

        // Basic format validation
        if (address.length() != 34 || !address.startsWith("T")) {
            return false;
        }

        // Check if it matches TRC20 pattern
        if (!address.matches("^T[A-Za-z0-9]{33}$")) {
            return false;
        }

        // Additional validation: should not contain spaces or special characters
        if (address.contains(" ")) {
            return false;
        }

        // Should contain mixed case (more realistic addresses have mixed case)
        boolean hasUpperCase = !address.equals(address.toLowerCase());
        boolean hasLowerCase = !address.equals(address.toUpperCase());

        return hasUpperCase || hasLowerCase; // At least one should be true for realistic addresses
    }

    // Get user's wallet change requests history
    public List<WalletChangeRequest> getUserWalletChangeRequests(User user) {
        return walletChangeRequestRepository.findByUserOrderByRequestedAtDesc(user);
    }
}