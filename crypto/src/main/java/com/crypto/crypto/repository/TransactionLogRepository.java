package com.crypto.crypto.repository;

import com.crypto.crypto.entity.TransactionLog;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    List<TransactionLog> findByUserOrderByCreatedAtDesc(User user);
    List<TransactionLog> findByTransactionType(String transactionType);
    List<TransactionLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<TransactionLog> findByUserAndTransactionType(User user, String transactionType);
}