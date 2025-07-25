package com.crypto.crypto.repository;

import com.crypto.crypto.entity.ReferralEarning;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralEarningRepository extends JpaRepository<ReferralEarning, Long> {
    List<ReferralEarning> findByReferrer(User referrer);
} 