
// Enhanced ReferralUsageRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralUsageRepository extends JpaRepository<ReferralUsage, Long> {
    Optional<ReferralUsage> findByReferrer(User referrer);
    
    @Query("SELECT ru FROM ReferralUsage ru ORDER BY ru.usageCount DESC")
    List<ReferralUsage> findAllByOrderByUsageCountDesc();
    
    @Query("SELECT ru FROM ReferralUsage ru WHERE ru.isActive = true AND ru.usageCount < ru.usageLimit")
    List<ReferralUsage> findActiveWithAvailableUsage();
}