package com.crypto.crypto.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "monthly_profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlyProfit;

    @Column(name = "daily_profit_min", precision = 19, scale = 2, nullable = false)
    private BigDecimal dailyProfitMin;

    @Column(name = "daily_profit_max", precision = 19, scale = 2, nullable = false)
    private BigDecimal dailyProfitMax;

    @Column(name = "plan_level", nullable = false)
    private Integer planLevel;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public Plan() {}

    public Plan(String name, BigDecimal price, BigDecimal monthlyProfit, 
                BigDecimal dailyProfitMin, BigDecimal dailyProfitMax, Integer planLevel) {
        this.name = name;
        this.price = price;
        this.monthlyProfit = monthlyProfit;
        this.dailyProfitMin = dailyProfitMin;
        this.dailyProfitMax = dailyProfitMax;
        this.planLevel = planLevel;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMonthlyProfit() {
        return monthlyProfit;
    }

    public void setMonthlyProfit(BigDecimal monthlyProfit) {
        this.monthlyProfit = monthlyProfit;
    }

    public BigDecimal getDailyProfitMin() {
        return dailyProfitMin;
    }

    public void setDailyProfitMin(BigDecimal dailyProfitMin) {
        this.dailyProfitMin = dailyProfitMin;
    }

    public BigDecimal getDailyProfitMax() {
        return dailyProfitMax;
    }

    public void setDailyProfitMax(BigDecimal dailyProfitMax) {
        this.dailyProfitMax = dailyProfitMax;
    }

    public Integer getPlanLevel() {
        return planLevel;
    }

    public void setPlanLevel(Integer planLevel) {
        this.planLevel = planLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    // Helper methods
    public boolean isActive() {
        return isActive != null && isActive;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", planLevel=" + planLevel +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan plan = (Plan) o;
        return id != null && id.equals(plan.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}