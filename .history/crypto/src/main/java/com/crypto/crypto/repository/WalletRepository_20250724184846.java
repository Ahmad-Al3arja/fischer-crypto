
// Enhanced WalletRepository.java
package com.crypto.crypto.repository;

import com.crypto.crypto.entity.User;
import com.crypto.crypto.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUser(User user);
    boolean existsByUsdtAddress(String usdtAddress);
    boolean existsByUsdtAddressAndUserNot(String usdtAddress, User user);
    Optional<Wallet> findByUsdtAddress(String usdtAddress);
}