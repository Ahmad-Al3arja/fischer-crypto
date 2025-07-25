package com.crypto.crypto.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "referral_usage")
public class ReferralUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id", nullable = false, unique = true)
    private User referrer;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit = 100; // Default limit is 100

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public ReferralUsage() {}

    public ReferralUsage(User referrer) {
        this.referrer = referrer;
        this.usageCount = 0;
        this.usageLimit = 100; // Default limit
        this.isActive = true;
    }

    public ReferralUsage(User referrer, Integer usageLimit) {
        this.referrer = referrer;
        this.usageCount = 0;
        this.usageLimit = usageLimit != null ? usageLimit : 100;
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReferrer() {
        return referrer;
    }

    public void setReferrer(User referrer) {
        this.referrer = referrer;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    public boolean canAcceptReferrals() {
        return isActive && usageCount < usageLimit;
    }

    public void incrementUsage() {
        this.usageCount++;
        if (this.usageCount >= this.usageLimit) {
            this.isActive = false;
        }
    }

    public int getRemainingReferrals() {
        return Math.max(0, usageLimit - usageCount);
    }

    public double getUsagePercentage() {
        if (usageLimit == 0) return 0.0;
        return (double) usageCount / usageLimit * 100.0;
    }

    public boolean isNearingLimit() {
        return getUsagePercentage() >= 90.0; // 90% or more
    }

    public boolean hasReachedLimit() {
        return usageCount >= usageLimit;
    }

    // Helper methods
    public void resetUsage() {
        this.usageCount = 0;
        this.isActive = true;
    }

    public void setLimit(Integer newLimit) {
        this.usageLimit = newLimit;
        // Reactivate if usage count is below new limit
        if (this.usageCount < newLimit) {
            this.isActive = true;
        }
    }

    @Override
    public String toString() {
        return "ReferralUsage{" +
                "id=" + id +
                ", referrer=" + (referrer != null ? referrer.getDisplayUsername() : null) +
                ", usageCount=" + usageCount +
                ", usageLimit=" + usageLimit +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferralUsage that = (ReferralUsage) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}