
// Enhanced UserRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByDisplayUsername(String displayUsername);
    
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByDisplayUsername(String displayUsername);
    
    List<User> findByReferrer(User referrer);
    List<User> findByGrandReferrer(User grandReferrer);
    List<User> findByCurrentPlanId(Long planId);
    List<User> findByStatus(UserStatus status);
    List<User> findByCurrentPlanIsNotNull();
    List<User> findByRoleOrderByCreatedAtDesc(com.crypto.crypto.entity.Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.referrer = :user")
    long countDirectReferrals(@Param("user") User user);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.grandReferrer = :user")
    long countSecondLevelReferrals(@Param("user") User user);
    
    // Enhanced search functionality
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.displayUsername) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "u.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<User> findByDisplayUsernameContainingOrFullNameContainingOrPhoneNumberContaining(
            @Param("query") String query1, 
            @Param("query") String query2, 
            @Param("query") String query3);
}
