package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.entity.AuditLog;
import com.luv2code.springbootlibrary.dto.AuditLogResponse;
import com.luv2code.springbootlibrary.repository.AuditLogRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Value("${audit.log.retention.days:180}")
    private int retentionDays;



    // ========== NOUVELLES MÉTHODES POUR BORROWSERVICE ==========

    @Override
    public void logBorrowBook(Long userId, Long bookId, String bookTitle) {
        try {
            var userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                var user = userOptional.get();
                // Utilise la méthode existante logBorrow avec l'email et l'IP
                logBorrow(userId, user.getEmail(), bookId, bookTitle, getClientIpAddress());
            }
        } catch (Exception e) {
            log.error("Erreur journalisation emprunt livre: {}", e.getMessage());
        }
    }

    @Override
    public void logReturnBook(Long userId, Long bookId, String bookTitle, Double penalty) {
        try {
            var userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                var user = userOptional.get();
                // Utilise la méthode existante logReturn avec l'email et l'IP
                logReturn(userId, user.getEmail(), bookId, bookTitle, penalty, getClientIpAddress());
            }
        } catch (Exception e) {
            log.error("Erreur journalisation retour livre: {}", e.getMessage());
        }
    }


    // ========== IMPLÉMENTATION DES MÉTHODES MANQUANTES ==========

    @Override
    public void logViewBook(Long userId, Long bookId, String bookTitle) {
        // Récupérer l'email et le rôle de l'utilisateur
        var userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            var user = userOptional.get();
            logUserAction(AuditLog.AuditActionType.VIEW_BOOK,
                    userId,
                    user.getEmail(),
                    user.getRole().name(),
                    "Consultation du livre: " + bookTitle,
                    getClientIpAddress());
        }
    }

    @Override
    public void logDownloadPdf(Long userId, Long bookId, String bookTitle) {
        // Récupérer l'email et le rôle de l'utilisateur
        var userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            var user = userOptional.get();
            logUserAction(AuditLog.AuditActionType.DOWNLOAD_PDF,
                    userId,
                    user.getEmail(),
                    user.getRole().name(),
                    "Téléchargement PDF: " + bookTitle,
                    getClientIpAddress());
        }
    }

    // ========== MÉTHODES ADMIN MANQUANTES ==========

    @Override
    public void logAdminCreateBook(Long adminId, Long bookId, String bookTitle) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                logUserAction(AuditLog.AuditActionType.ADMIN_CREATE_BOOK,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        "Création du livre: " + bookTitle + " (ID: " + bookId + ")",
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a créé le livre {}", admin.getEmail(), bookTitle);
            }
        } catch (Exception e) {
            log.error("Erreur journalisation création livre: {}", e.getMessage());
        }
    }

    @Override
    public void logAdminUpdateBook(Long adminId, Long bookId, String bookTitle) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                logUserAction(AuditLog.AuditActionType.ADMIN_UPDATE_BOOK,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        "Mise à jour du livre: " + bookTitle + " (ID: " + bookId + ")",
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a mis à jour le livre {}", admin.getEmail(), bookTitle);
            }
        } catch (Exception e) {
            log.error("Erreur journalisation mise à jour livre: {}", e.getMessage());
        }
    }

    @Override
    public void logAdminDeleteBook(Long adminId, Long bookId, String bookTitle) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                logUserAction(AuditLog.AuditActionType.ADMIN_DELETE_BOOK,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        "Suppression du livre: " + bookTitle + " (ID: " + bookId + ")",
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a supprimé le livre {}", admin.getEmail(), bookTitle);
            }
        } catch (Exception e) {
            log.error("Erreur journalisation suppression livre: {}", e.getMessage());
        }
    }

    @Override
    public void logAdminUpdateUserRole(Long adminId, Long targetUserId,
                                       String targetUserEmail, String oldRole, String newRole) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                logUserAction(AuditLog.AuditActionType.ADMIN_UPDATE_USER_ROLE,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        "Changement rôle de " + targetUserEmail + " de " + oldRole + " vers " + newRole,
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a changé le rôle de {}: {} -> {}",
                        admin.getEmail(), targetUserEmail, oldRole, newRole);
            }
        } catch (Exception e) {
            log.error("Erreur journalisation changement rôle: {}", e.getMessage());
        }
    }

    @Override
    public void logAdminToggleUserStatus(Long adminId, Long targetUserId,
                                         String targetUserEmail, boolean active) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                AuditLog.AuditActionType actionType = active ?
                        AuditLog.AuditActionType.ADMIN_ACTIVATE_USER :
                        AuditLog.AuditActionType.ADMIN_DEACTIVATE_USER;

                String action = active ? "activé" : "désactivé";

                logUserAction(actionType,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        action + " le compte utilisateur: " + targetUserEmail,
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a {} le compte {}",
                        admin.getEmail(), action, targetUserEmail);
            }
        } catch (Exception e) {
            log.error("Erreur journalisation changement statut utilisateur: {}", e.getMessage());
        }
    }

    @Override
    public void logAdminViewStats(Long adminId) {
        try {
            var adminOptional = userRepository.findById(adminId);
            if (adminOptional.isPresent()) {
                var admin = adminOptional.get();
                logUserAction(AuditLog.AuditActionType.ADMIN_VIEW_STATS,
                        adminId,
                        admin.getEmail(),
                        admin.getRole().name(),
                        "Consultation des statistiques",
                        getClientIpAddress());
                log.info("[AUDIT] Admin {} a consulté les statistiques", admin.getEmail());
            }
        } catch (Exception e) {
            log.error("Erreur journalisation consultation stats: {}", e.getMessage());
        }
    }



    // ========== MÉTHODES EXISTANTES (inchangées) ==========

    @Override
    public void logAction(AuditLog.AuditActionType actionType, String details) {
        try {
            AuditLog auditLog = new AuditLog(actionType, details);

            // Récupérer les infos de l'utilisateur connecté si disponible
            try {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                auditLog.setUserEmail(username);
            } catch (Exception e) {
                // Pas d'utilisateur connecté
            }

            // Récupérer l'adresse IP
            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
            log.info("[AUDIT] {} - {} - IP: {}", actionType, details,
                    auditLog.getIpAddress() != null ? auditLog.getIpAddress() : "N/A");

        } catch (Exception e) {
            log.error("Erreur lors de la journalisation: {}", e.getMessage());
        }
    }

    @Override
    public void logAction(AuditLog.AuditActionType actionType, String entityType,
                          Long entityId, String entityName, String details) {
        try {
            AuditLog auditLog = new AuditLog(actionType, details);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEntityName(entityName);

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
            }

            auditLogRepository.save(auditLog);
            log.info("[AUDIT] {} - {} {}: {} - {}",
                    actionType, entityType, entityId, entityName, details);

        } catch (Exception e) {
            log.error("Erreur journalisation étendue: {}", e.getMessage());
        }
    }

    @Override
    public void logUserAction(AuditLog.AuditActionType actionType, Long userId,
                              String userEmail, String userRole, String details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog(actionType, details);
            auditLog.setUserId(userId);
            auditLog.setUserEmail(userEmail);
            auditLog.setUserRole(userRole);
            auditLog.setIpAddress(ipAddress);

            if (request != null && ipAddress == null) {
                auditLog.setIpAddress(getClientIpAddress());
            }

            auditLogRepository.save(auditLog);
            log.info("[AUDIT_USER] {} - {} ({}) - {}",
                    actionType, userEmail, userRole, details);

        } catch (Exception e) {
            log.error("Erreur journalisation utilisateur: {}", e.getMessage());
        }
    }

    @Override
    public void logLogin(String email, boolean success, String ipAddress,
                         String userAgent, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog(
                    success ? AuditLog.AuditActionType.LOGIN_SUCCESS :
                            AuditLog.AuditActionType.LOGIN_FAILED,
                    success ? "Connexion réussie" : "Échec connexion: " + errorMessage
            );

            auditLog.setUserEmail(email);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);

            auditLogRepository.save(auditLog);
            log.info("[AUTH_{}] {} - IP: {} - Agent: {}",
                    success ? "SUCCESS" : "FAILED", email, ipAddress, userAgent);

        } catch (Exception e) {
            log.error("Erreur journalisation connexion: {}", e.getMessage());
        }
    }

    @Override
    public void logLogout(String email) {
        try {
            AuditLog auditLog = new AuditLog(
                    AuditLog.AuditActionType.LOGOUT,
                    "Déconnexion utilisateur"
            );

            auditLog.setUserEmail(email);

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);

            log.info("[AUTH_LOGOUT] {} - IP: {}", email, auditLog.getIpAddress());

        } catch (Exception e) {
            log.error("Erreur journalisation logout: {}", e.getMessage());
        }
    }


    @Override
    public void logBorrow(Long userId, String userEmail, Long bookId,
                          String bookTitle, String ipAddress) {
        logUserAction(AuditLog.AuditActionType.BORROW_BOOK, userId, userEmail, "STUDENT",
                "Emprunt du livre: " + bookTitle + " (ID: " + bookId + ")", ipAddress);
    }

    @Override
    public void logReturn(Long userId, String userEmail, Long bookId,
                          String bookTitle, Double penalty, String ipAddress) {
        String details = "Retour du livre: " + bookTitle + " (ID: " + bookId + ")";
        if (penalty != null && penalty > 0) {
            details += " - Pénalité: " + penalty + "€";
        }

        logUserAction(AuditLog.AuditActionType.RETURN_BOOK, userId, userEmail, "STUDENT",
                details, ipAddress);
    }

    @Override
    public void logAdminAction(AuditLog.AuditActionType actionType,
                               Long targetEntityId, String targetEntityName, String details) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            AuditLog auditLog = new AuditLog(actionType, details);
            auditLog.setUserEmail(username);
            auditLog.setUserRole("ADMIN");
            auditLog.setEntityId(targetEntityId);
            auditLog.setEntityName(targetEntityName);

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress());
            }

            auditLogRepository.save(auditLog);
            log.info("[AUDIT_ADMIN] {} par {} - Cible: {} - {}",
                    actionType, username, targetEntityName, details);

        } catch (Exception e) {
            log.error("Erreur journalisation admin: {}", e.getMessage());
        }
    }

    @Override
    public Page<AuditLogResponse> getAuditLogs(Long userId, AuditLog.AuditActionType actionType,
                                               String entityType, Long entityId,
                                               LocalDateTime startDate, LocalDateTime endDate,
                                               Pageable pageable) {
        log.info("[AUDIT_QUERY] Consultation logs - Filtres: userId={}, actionType={}",
                userId, actionType);

        Page<AuditLog> logs = auditLogRepository.findWithFilters(
                userId, actionType, entityType, entityId, startDate, endDate, pageable);

        return logs.map(AuditLogResponse::fromEntity);
    }

    @Override
    public String getAuditStatistics() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("=== STATISTIQUES AUDIT ===\n");

            List<Object[]> actionsByType = auditLogRepository.countActionsByType();
            stats.append("Actions par type:\n");
            for (Object[] row : actionsByType) {
                stats.append("  - ").append(row[0]).append(": ").append(row[1]).append("\n");
            }

            List<Object[]> actionsByUser = auditLogRepository.countActionsByUser();
            stats.append("\nActivité par utilisateur (top 5):\n");
            int count = 0;
            for (Object[] row : actionsByUser) {
                if (count++ >= 5) break;
                stats.append("  - ").append(row[0]).append(": ").append(row[1]).append("\n");
            }

            long totalLogs = auditLogRepository.count();
            stats.append("\nTotal des logs: ").append(totalLogs);

            return stats.toString();

        } catch (Exception e) {
            log.error("Erreur génération statistiques: {}", e.getMessage());
            return "Erreur lors de la génération des statistiques";
        }
    }

    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            long countBefore = auditLogRepository.count();

            auditLogRepository.deleteByCreatedAtBefore(cutoffDate);

            long countAfter = auditLogRepository.count();
            long deletedCount = countBefore - countAfter;

            log.info("[AUDIT_CLEANUP] Suppression de {} logs antérieurs à {}",
                    deletedCount, cutoffDate);

        } catch (Exception e) {
            log.error("Erreur nettoyage logs: {}", e.getMessage());
        }
    }

    // ========== MÉTHODE UTILITAIRE ==========
    private String getClientIpAddress() {
        if (request == null) {
            return "N/A";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}