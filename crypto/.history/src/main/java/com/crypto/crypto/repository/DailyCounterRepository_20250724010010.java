package com.crypto.crypto.repository;

import com.crypto.crypto.entity.DailyCounter;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCounterRepository extends JpaRepository<DailyCounter, Long> {
    Optional<DailyCounter> findByUser(User user);
    
    @Query("SELECT dc FROM DailyCounter dc WHERE dc.isActive = true AND dc.endTime <= :currentTime")
    List<DailyCounter> findExpiredCounters(LocalDateTime currentTime);
} 