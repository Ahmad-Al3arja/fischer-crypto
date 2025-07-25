package com.crypto.crypto.repository;

import com.crypto.crypto.entity.Deposit;
import com.crypto.crypto.entity.DepositStatus;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
    List<Deposit> findByUserAndStatus(User user, DepositStatus status);
    List<Deposit> findByStatus(DepositStatus status);
    List<Deposit> findByUserOrderByCreatedAtDesc(User user);
    List<Deposit> findByUser(User user);
    Optional<Deposit> findByIdAndUser(Long id, User user);
    
    @Query("SELECT d FROM Deposit d WHERE d.user = :user AND d.status = 'APPROVED' ORDER BY d.approvedAt ASC")
    Optional<Deposit> findFirstApprovedDeposit(@Param("user") User user);
    
    long countByStatus(DepositStatus status);
} 