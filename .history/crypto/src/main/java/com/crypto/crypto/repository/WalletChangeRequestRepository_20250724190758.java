package com.crypto.crypto.repository;

import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.WalletChangeRequest;
import com.crypto.crypto.entity.WalletChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletChangeRequestRepository extends JpaRepository<WalletChangeRequest, Long> {
    
    List<WalletChangeRequest> findByUserOrderByCreatedAtDesc(User user);
    
    List<WalletChangeRequest> findByStatus(WalletChangeStatus status);
    
    List<WalletChangeRequest> findByUserAndStatus(User user, WalletChangeStatus status);
    
    @Query("SELECT w FROM WalletChangeRequest w WHERE w.user = :user AND w.status = 'PENDING' ORDER BY w.createdAt DESC")
    List<WalletChangeRequest> findPendingRequestsByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(w) FROM WalletChangeRequest w WHERE w.user = :user AND w.status = 'PENDING'")
    long countPendingRequestsByUser(@Param("user") User user);
    
    Optional<WalletChangeRequest> findByIdAndUser(Long id, User user);
} 