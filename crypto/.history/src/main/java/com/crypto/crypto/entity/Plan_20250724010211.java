package com.crypto.crypto.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlyProfit;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal dailyProfitMin;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal dailyProfitMax;
    
    @Column(nullable = false)
    private Integer planLevel;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getMonthlyProfit() { return monthlyProfit; }
    public void setMonthlyProfit(BigDecimal monthlyProfit) { this.monthlyProfit = monthlyProfit; }
    
    public BigDecimal getDailyProfitMin() { return dailyProfitMin; }
    public void setDailyProfitMin(BigDecimal dailyProfitMin) { this.dailyProfitMin = dailyProfitMin; }
    
    public BigDecimal getDailyProfitMax() { return dailyProfitMax; }
    public void setDailyProfitMax(BigDecimal dailyProfitMax) { this.dailyProfitMax = dailyProfitMax; }
    
    public Integer getPlanLevel() { return planLevel; }
    public void setPlanLevel(Integer planLevel) { this.planLevel = planLevel; }
} 