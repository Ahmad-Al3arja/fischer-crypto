// WalletChangeRequestRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.WalletChangeRequest;
import com.crypto.crypto.entity.WalletChangeStatus;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletChangeRequestRepository extends JpaRepository<WalletChangeRequest, Long> {
    List<WalletChangeRequest> findByStatusOrderByRequestedAtAsc(WalletChangeStatus status);
    Optional<WalletChangeRequest> findByUserAndStatus(User user, WalletChangeStatus status);
    boolean existsByUserAndStatus(User user, WalletChangeStatus status);
    List<WalletChangeRequest> findByUserOrderByRequestedAtDesc(User user);
}