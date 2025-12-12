package com.subcycle.repository;

import com.subcycle.entity.PasswordResetToken;
import com.subcycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByUser(User user);
}
