package com.crypto.crypto.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "admin_settings")
public class AdminSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "maintenance_mode", nullable = false)
    private Boolean maintenanceMode = false;
    
    @Column(name = "about_content", columnDefinition = "TEXT")
    private String aboutContent = "Welcome to our investment platform!";
    
    @Column(name = "default_usage_limit", nullable = false)
    private Integer defaultUsageLimit = 100;
    
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private java.time.LocalDateTime updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Boolean getMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(Boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }
    
    public String getAboutContent() { return aboutContent; }
    public void setAboutContent(String aboutContent) { this.aboutContent = aboutContent; }
    
    public Integer getDefaultUsageLimit() { return defaultUsageLimit; }
    public void setDefaultUsageLimit(Integer defaultUsageLimit) { this.defaultUsageLimit = defaultUsageLimit; }
    
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 