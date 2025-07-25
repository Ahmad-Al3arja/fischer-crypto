package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralEarning;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReferralEarningRepository extends JpaRepository<ReferralEarning, Long> {
    List<ReferralEarning> findByReferrer(User referrer);
    
    @Query("SELECT COALESCE(SUM(re.amount), 0) FROM ReferralEarning re WHERE re.referrer = :user")
    BigDecimal getTotalEarningsByReferrer(@Param("user") User user);
} 