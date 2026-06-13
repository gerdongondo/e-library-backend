package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserId(Long userId);

    void deleteByToken(String token);

    void deleteByExpiryDateBefore(java.time.LocalDateTime date);
}