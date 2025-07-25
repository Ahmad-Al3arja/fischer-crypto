package com.crypto.crypto.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_settings")
public class AdminSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String settingKey;
    
    @Column(columnDefinition = "TEXT")
    private String settingValue;
    
    private String description;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 