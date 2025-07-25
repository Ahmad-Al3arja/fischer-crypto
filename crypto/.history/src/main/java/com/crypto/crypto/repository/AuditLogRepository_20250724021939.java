// AuditLogRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.AuditLog;
import com.crypto.crypto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
    List<AuditLog> findByAdminOrderByCreatedAtDesc(User admin);
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}