package com.crypto.crypto.repository;

import com.crypto.crypto.entity.Withdrawal;
import com.crypto.crypto.entity.WithdrawalStatus;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
    
    List<Withdrawal> findByUserOrderByCreatedAtDesc(User user);
    List<Withdrawal> findByStatus(WithdrawalStatus status);
    List<Withdrawal> findByUserAndStatus(User user, WithdrawalStatus status);
    
    // Find withdrawals within a date range and status
    List<Withdrawal> findByUserAndCreatedAtBetweenAndStatus(
            User user, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            WithdrawalStatus status);
    
    // Find withdrawals within a date range
    List<Withdrawal> findByUserAndCreatedAtBetween(
            User user, 
            LocalDateTime startDate, 
            LocalDateTime endDate);
    
    // Find recent withdrawals for a user
    @Query("SELECT w FROM Withdrawal w WHERE w.user = :user ORDER BY w.createdAt DESC")
    List<Withdrawal> findRecentWithdrawalsForUser(@Param("user") User user);
    
    // Find all withdrawals ordered by creation date
    List<Withdrawal> findAllByOrderByCreatedAtDesc();
    
    // Find withdrawals by status ordered by creation date
    List<Withdrawal> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);
    
    // Find withdrawals by user and date range
    @Query("SELECT w FROM Withdrawal w WHERE w.user = :user AND w.createdAt >= :startDate AND w.createdAt <= :endDate ORDER BY w.createdAt DESC")
    List<Withdrawal> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Count withdrawals by status
    @Query("SELECT COUNT(w) FROM Withdrawal w WHERE w.status = :status")
    long countByStatus(@Param("status") WithdrawalStatus status);
    
    // Count withdrawals by user and status
    @Query("SELECT COUNT(w) FROM Withdrawal w WHERE w.user = :user AND w.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") WithdrawalStatus status);
    
    // Find withdrawals created today
    @Query("SELECT w FROM Withdrawal w WHERE DATE(w.createdAt) = CURRENT_DATE ORDER BY w.createdAt DESC")
    List<Withdrawal> findTodaysWithdrawals();
    
    // Find withdrawals by user created today
    @Query("SELECT w FROM Withdrawal w WHERE w.user = :user AND DATE(w.createdAt) = CURRENT_DATE ORDER BY w.createdAt DESC")
    List<Withdrawal> findTodaysWithdrawalsForUser(@Param("user") User user);
    
    // Find withdrawals pending for more than specified hours
    @Query("SELECT w FROM Withdrawal w WHERE w.status = 'PENDING' AND w.createdAt < :cutoffTime ORDER BY w.createdAt ASC")
    List<Withdrawal> findPendingWithdrawalsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
}