package com.luv2code.springbootlibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_log")
@Data
@NoArgsConstructor
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String action; // LOGIN, BORROW_BOOK, RETURN_BOOK, UPDATE_PROFILE, etc.

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public UserActivityLog(User user, String action, String details, String ipAddress) {
        this.user = user;
        this.action = action;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }
}