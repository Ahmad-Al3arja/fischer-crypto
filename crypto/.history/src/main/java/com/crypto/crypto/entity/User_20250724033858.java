package com.crypto.crypto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String fullName;
    
    @NotBlank
    @Column(unique = true, nullable = false)
    private String displayUsername; // Changed from 'username'
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @Column(unique = true, nullable = false)
    private String phoneNumber;
    
    @NotBlank
    @Column(nullable = false)
    private String password;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalBalance = BigDecimal.ZERO;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal frozenBalance = BigDecimal.ZERO;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal referralEarnings = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_plan_id")
    private Plan currentPlan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_id")
    private User referrer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grand_referrer_id")
    private User grandReferrer;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.INACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
    
    private LocalDateTime subscriptionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getDisplayUsername() { return displayUsername; }
    public void setDisplayUsername(String displayUsername) { this.displayUsername = displayUsername; }
    
    // UserDetails implementation - use phone number for authentication
    @Override
    public String getUsername() {
        return phoneNumber;
    }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
    
    public BigDecimal getFrozenBalance() { return frozenBalance; }
    public void setFrozenBalance(BigDecimal frozenBalance) { this.frozenBalance = frozenBalance; }
    
    public BigDecimal getReferralEarnings() { return referralEarnings; }
    public void setReferralEarnings(BigDecimal referralEarnings) { this.referralEarnings = referralEarnings; }
    
    public Plan getCurrentPlan() { return currentPlan; }
    public void setCurrentPlan(Plan currentPlan) { this.currentPlan = currentPlan; }
    
    public User getReferrer() { return referrer; }
    public void setReferrer(User referrer) { this.referrer = referrer; }
    
    public User getGrandReferrer() { return grandReferrer; }
    public void setGrandReferrer(User grandReferrer) { this.grandReferrer = grandReferrer; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public LocalDateTime getSubscriptionDate() { return subscriptionDate; }
    public void setSubscriptionDate(LocalDateTime subscriptionDate) { this.subscriptionDate = subscriptionDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    public BigDecimal getWithdrawableBalance() {
        return totalBalance.subtract(frozenBalance);
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public boolean isAccountNonExpired() { return true; }
    
    @Override
    public boolean isAccountNonLocked() { return true; }
    
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    
    @Override
    public boolean isEnabled() { return status == UserStatus.ACTIVE; }
} 