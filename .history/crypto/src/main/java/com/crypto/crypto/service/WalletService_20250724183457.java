// Enhanced WalletService.java
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
        
        // Create change request
        WalletChangeRequest request = new WalletChangeRequest();
        request.setUser(user);
        request.setCurrentAddress(existingWallet.getUsdtAddress());
        request.setNewAddress(newUsdtAddress);
        request.setReason(reason);
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
            }
        }
        
        return response;
    }
    
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
        
        wallet.setUsdtAddress(newAddress);
        walletRepository.save(wallet);
    }
    
    private boolean isValidTRC20Address(String address) {
        // Enhanced TRC20 address validation
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        
        address = address.trim();
        
        return address.length() == 34 && 
               address.startsWith("T") &&
               address.matches("^T[A-Za-z0-9]{33}$") &&
               !address.contains(" ") &&
               !address.toLowerCase().equals(address) && // Should contain uppercase letters
               !address.toUpperCase().equals(address);   // Should contain lowercase letters
    }
}

// Additional entity for wallet change requests
@Entity
@Table(name = "wallet_change_requests")
public class WalletChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "current_address", nullable = false)
    private String currentAddress;
    
    @Column(name = "new_address", nullable = false)
    private String newAddress;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletChangeStatus status;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by")
    private Long processedBy;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    
    public String getNewAddress() { return newAddress; }
    public void setNewAddress(String newAddress) { this.newAddress = newAddress; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public WalletChangeStatus getStatus() { return status; }
    public void setStatus(WalletChangeStatus status) { this.status = status; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public Long getProcessedBy() { return processedBy; }
    public void setProcessedBy(Long processedBy) { this.processedBy = processedBy; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}

enum WalletChangeStatus {
    PENDING, APPROVED, REJECTED
}