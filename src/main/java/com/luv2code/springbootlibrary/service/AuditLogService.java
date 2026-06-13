package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.entity.AuditLog;
import com.luv2code.springbootlibrary.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {

    // ========== MÉTHODES SIMPLES ==========
    void logAction(AuditLog.AuditActionType actionType, String details);
    void logAction(AuditLog.AuditActionType actionType,
                   String entityType,
                   Long entityId,
                   String entityName,
                   String details);

    // ========== JOURNALISATION DES CONNEXIONS ==========
    void logUserAction(AuditLog.AuditActionType actionType,
                       Long userId,
                       String userEmail,
                       String userRole,
                       String details,
                       String ipAddress);

    void logLogin(String email, boolean success, String ipAddress,
                  String userAgent, String errorMessage);

    void logLogout(String email); // AJOUT

    // ========== JOURNALISATION DES ACTIONS UTILISATEURS ==========
    void logBorrow(Long userId, String userEmail, Long bookId,
                   String bookTitle, String ipAddress);

    // AJOUT : Méthode simplifiée pour BorrowServiceImpl
    void logBorrowBook(Long userId, Long bookId, String bookTitle);

    void logReturn(Long userId, String userEmail, Long bookId,
                   String bookTitle, Double penalty, String ipAddress);

    // AJOUT : Méthode simplifiée pour BorrowServiceImpl
    void logReturnBook(Long userId, Long bookId, String bookTitle, Double penalty);

    void logViewBook(Long userId, Long bookId, String bookTitle); // AJOUT

    void logDownloadPdf(Long userId, Long bookId, String bookTitle); // AJOUT

    // ========== JOURNALISATION DES ACTIONS ADMIN ==========
    void logAdminAction(AuditLog.AuditActionType actionType,
                        Long targetEntityId,
                        String targetEntityName,
                        String details);

    void logAdminCreateBook(Long adminId, Long bookId, String bookTitle); // AJOUT
    void logAdminUpdateBook(Long adminId, Long bookId, String bookTitle); // AJOUT
    void logAdminDeleteBook(Long adminId, Long bookId, String bookTitle); // AJOUT
    void logAdminUpdateUserRole(Long adminId, Long targetUserId,
                                String targetUserEmail, String oldRole, String newRole); // AJOUT
    void logAdminToggleUserStatus(Long adminId, Long targetUserId,
                                  String targetUserEmail, boolean active); // AJOUT
    void logAdminViewStats(Long adminId); // AJOUT

    // ========== CONSULTATION ==========
    Page<AuditLogResponse> getAuditLogs(Long userId,
                                        AuditLog.AuditActionType actionType,
                                        String entityType,
                                        Long entityId,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        Pageable pageable);

    String getAuditStatistics();

    // ========== NETTOYAGE ==========
    void cleanupOldLogs(int daysToKeep);
}