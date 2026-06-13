package com.luv2code.springbootlibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_role", length = 50)
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(name = "entity_type", length = 100)
    private String entityType; // BOOK, USER, BORROW, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name", length = 500)
    private String entityName; // Titre du livre, email utilisateur, etc.

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Enumération des types d'actions
    public enum AuditActionType {
        // Authentification
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        REGISTER,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,

        // Actions utilisateur
        BORROW_BOOK,
        RETURN_BOOK,
        VIEW_BOOK,
        DOWNLOAD_PDF,
        UPDATE_PROFILE,

        // Actions admin
        ADMIN_CREATE_BOOK,
        ADMIN_UPDATE_BOOK,
        ADMIN_DELETE_BOOK,
        ADMIN_UPDATE_USER_ROLE,
        ADMIN_ACTIVATE_USER,
        ADMIN_DEACTIVATE_USER,
        ADMIN_VIEW_STATS,
        ADMIN_VIEW_LOGS,
        ADMIN_EXTEND_BORROW,
        ADMIN_CANCEL_BORROW,

        // Erreurs système
        SYSTEM_ERROR,
        SECURITY_VIOLATION
    }

    // Constructeur pratique
    public AuditLog(AuditActionType actionType, String details) {
        this.actionType = actionType;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
}