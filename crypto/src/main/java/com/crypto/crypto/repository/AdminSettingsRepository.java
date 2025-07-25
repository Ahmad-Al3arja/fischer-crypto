package com.crypto.crypto.repository;

import com.crypto.crypto.entity.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    Optional<AdminSettings> findByKeyName(String keyName);
    
    @Query("SELECT a FROM AdminSettings a WHERE a.keyName = :keyName")
    Optional<AdminSettings> findByKey(@Param("keyName") String keyName);
    
    boolean existsByKeyName(String keyName);
    
    @Modifying
    @Query("UPDATE AdminSettings a SET a.value = :value, a.updatedAt = CURRENT_TIMESTAMP WHERE a.keyName = :keyName")
    int updateValueByKeyName(@Param("keyName") String keyName, @Param("value") String value);
    
    List<AdminSettings> findByKeyNameStartingWith(String prefix);
} 