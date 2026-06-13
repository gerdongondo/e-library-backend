package com.luv2code.springbootlibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    // Constructeur
    public PasswordResetToken(String token, User user, int expirationMinutes) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    // Vérifie si le token est expiré
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    // Vérifie si le token est valide
    public boolean isValid() {
        return !used && !isExpired();
    }
}