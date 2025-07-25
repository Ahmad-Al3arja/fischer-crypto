package com.crypto.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_counters")
public class DailyCounter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private Integer planDay = 1; // 1-30 days
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal currentDayProfit;
    
    @Column(nullable = false)
    private Boolean isActive = false;
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getPlanDay() { return planDay; }
    public void setPlanDay(Integer planDay) { this.planDay = planDay; }
    
    public BigDecimal getCurrentDayProfit() { return currentDayProfit; }
    public void setCurrentDayProfit(BigDecimal currentDayProfit) { this.currentDayProfit = currentDayProfit; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
} 