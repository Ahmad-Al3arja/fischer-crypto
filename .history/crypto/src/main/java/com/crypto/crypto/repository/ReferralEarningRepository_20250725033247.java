package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralEarning;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralEarningRepository extends JpaRepository<ReferralEarning, Long> {
    List<ReferralEarning> findByReferrer(User referrer);
    
    // Add this new method
    List<ReferralEarning> findByReferrerAndReferredUser(User referrer, User referredUser);
    
    // Add this method to get all earnings for a specific referrer
    @Query("SELECT re FROM ReferralEarning re WHERE re.referrer = :referrer ORDER BY re.createdAt DESC")
    List<ReferralEarning> findByReferrerOrderByCreatedAtDesc(@Param("referrer") User referrer);
    
    // Add this method to get total commission earned by referrer
    @Query("SELECT COALESCE(SUM(re.amount), 0) FROM ReferralEarning re WHERE re.referrer = :referrer")
    java.math.BigDecimal getTotalCommissionByReferrer(@Param("referrer") User referrer);
    
    // Add this method to get commission by type
    @Query("SELECT COALESCE(SUM(re.amount), 0) FROM ReferralEarning re WHERE re.referrer = :referrer AND re.commissionType = :type")
    java.math.BigDecimal getCommissionByReferrerAndType(@Param("referrer") User referrer, @Param("type") com.crypto.crypto.entity.CommissionType type);
    
    // Add this method to get earnings by user
    List<ReferralEarning> findByUser(User user);
} 