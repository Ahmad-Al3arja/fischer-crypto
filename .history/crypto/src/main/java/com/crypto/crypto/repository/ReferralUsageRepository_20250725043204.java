package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralUsage;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralUsageRepository extends JpaRepository<ReferralUsage, Long> {
    Optional<ReferralUsage> findByReferrer(User referrer);
    boolean existsByReferrer(User referrer);
} 