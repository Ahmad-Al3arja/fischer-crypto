package com.crypto.crypto.repository;

import com.crypto.crypto.entity.Withdrawal;
import com.crypto.crypto.entity.WithdrawalStatus;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    List<Withdrawal> findByUserOrderByCreatedAtDesc(User user);
    List<Withdrawal> findByStatus(WithdrawalStatus status);
} 