package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.*;
import com.luv2code.springbootlibrary.entity.*;
import com.luv2code.springbootlibrary.repository.BorrowRepository;
import com.luv2code.springbootlibrary.repository.UserActivityLogRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserActivityLogRepository activityLogRepository;

    @Autowired
    private HttpServletRequest request; // Pour obtenir l'IP

    @Autowired
    private AuditLogService auditLogService;



    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long userId, UserRoleUpdateRequest request) {
        System.out.println("\n=== [ADMIN] Mise à jour du rôle utilisateur ID=" + userId + " ===");

        // 1. Récupérer l'admin actuel
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        // 2. Récupérer l'utilisateur cible
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        System.out.println("[ADMIN] Admin: " + admin.getEmail() + " modifie le rôle de: " +
                targetUser.getEmail() + " de " + targetUser.getRole() + " à " + request.getRole());

        // 3. Sauvegarder l'ancien rôle pour le log
        Role oldRole = targetUser.getRole();

        // 4. Mettre à jour le rôle
        targetUser.setRole(request.getRole());
        userRepository.save(targetUser);

        // ===== AUDIT (NOUVEAU) =====
        auditLogService.logAdminAction(
                AuditLog.AuditActionType.ADMIN_UPDATE_USER_ROLE,
                targetUser.getId(),
                targetUser.getEmail(),
                "Rôle changé de " + oldRole + " à " + request.getRole()
        );


        // 5. Log l'action
        String details = "Rôle changé de " + oldRole + " à " + request.getRole() +
                ". Raison: " + (request.getReason() != null ? request.getReason() : "Non spécifiée");
        logAdminAction("UPDATE_USER_ROLE", details);

        // 6. Retourner la réponse
        return new UserProfileResponse(
                targetUser.getId(),
                targetUser.getEmail(),
                targetUser.getFirstName(),
                targetUser.getLastName(),
                targetUser.getRole().name()
        );
    }

    @Override
    @Transactional
    public UserProfileResponse toggleUserActiveStatus(Long userId, boolean active) {
        System.out.println("\n=== [ADMIN] Changement statut utilisateur ID=" + userId + " à active=" + active + " ===");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Note: Ajoutez un champ 'active' dans votre entité User si ce n'est pas fait
        // Pour l'instant, on simule avec un commentaire

        // Mise à jour du champ 'active'
        user.setActive(active);
        userRepository.save(user);
        System.out.println("[ADMIN] Statut de " + user.getEmail() + " changé à: " +
                (active ? "ACTIF" : "INACTIF"));

        logAdminAction("TOGGLE_USER_STATUS",
                "Utilisateur " + user.getEmail() + " marqué comme " + (active ? "actif" : "inactif"));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    @Override
    public Page<UserListResponse> getAllUsers(Pageable pageable) {
        System.out.println("\n=== [ADMIN] Liste de tous les utilisateurs ===");

        Page<User> users = userRepository.findAll(pageable);

        return users.map(user -> {
            // Compter les emprunts de l'utilisateur
            Long totalBorrows = borrowRepository.countByUserId(user.getId());
            List<Borrow> activeBorrows = borrowRepository.findByUserIdAndStatus(
                    user.getId(), Borrow.BorrowStatus.ACTIVE);

            // Correction : utiliser user.isActive() au lieu de true
            boolean isActive = user.isActive(); // suppose que le champ existe

            return new UserListResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name(),
                    user.isActive(),   // ✅ au lieu de true // À remplacer par user.isActive() si vous ajoutez ce champ
                    LocalDateTime.now().minusDays(1), // Simulé - à remplacer par lastLogin

                    totalBorrows,
                    (long) activeBorrows.size()
            );
        });
    }

    @Override
    public Page<UserActivityResponse> getUserActivity(Long userId, Pageable pageable) {
        System.out.println("\n=== [ADMIN] Activité de l'utilisateur ID=" + userId + " ===");

        Page<UserActivityLog> activities = activityLogRepository.findByUserId(userId, pageable);

        return activities.map(log -> new UserActivityResponse(
                log.getId(),
                log.getUser().getId(),
                log.getUser().getEmail(),
                log.getAction(),
                log.getDetails(),
                log.getIpAddress(),
                log.getTimestamp()
        ));
    }

    @Override
    @Transactional
    public BorrowResponse extendBorrow(Long borrowId, int additionalDays) {
        System.out.println("\n=== [ADMIN] Prolongation d'emprunt ID=" + borrowId + " de " + additionalDays + " jours ===");

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        if (borrow.getStatus() != Borrow.BorrowStatus.ACTIVE) {
            throw new RuntimeException("Seuls les emprunts actifs peuvent être prolongés");
        }

        // Prolonger la date d'échéance
        borrow.setDueDate(borrow.getDueDate().plusDays(additionalDays));
        borrow.setStatus(Borrow.BorrowStatus.EXTENDED);
        borrow.setNotes((borrow.getNotes() != null ? borrow.getNotes() + " " : "") +
                "Prolongé de " + additionalDays + " jours le " + LocalDateTime.now());

        Borrow updatedBorrow = borrowRepository.save(borrow);

        logAdminAction("EXTEND_BORROW",
                "Emprunt ID=" + borrowId + " prolongé de " + additionalDays + " jours. " +
                        "Nouvelle date: " + updatedBorrow.getDueDate());

        return mapToBorrowResponse(updatedBorrow);
    }

    @Override
    @Transactional
    public BorrowResponse cancelBorrow(Long borrowId, String reason) {
        System.out.println("\n=== [ADMIN] Annulation d'emprunt ID=" + borrowId + " ===");

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        borrow.setStatus(Borrow.BorrowStatus.CANCELLED);
        borrow.setNotes((borrow.getNotes() != null ? borrow.getNotes() + " " : "") +
                "Annulé. Raison: " + reason + " Date: " + LocalDateTime.now());

        Borrow updatedBorrow = borrowRepository.save(borrow);

        logAdminAction("CANCEL_BORROW",
                "Emprunt ID=" + borrowId + " annulé. Raison: " + reason);

        return mapToBorrowResponse(updatedBorrow);
    }

    @Override
    public Page<BorrowResponse> getAllBorrows(Long userId, Long bookId, String status, Pageable pageable) {
        System.out.println("\n=== [ADMIN] Liste des emprunts avec filtres ===");
        System.out.println("[ADMIN] Filtres: userId=" + userId + ", bookId=" + bookId + ", status=" + status);

        Borrow.BorrowStatus borrowStatus = null;
        if (status != null) {
            try {
                borrowStatus = Borrow.BorrowStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Statut invalide: " + status);
            }
        }

        Page<Borrow> borrows = borrowRepository.findWithFilters(userId, bookId, borrowStatus, pageable);

        return borrows.map(this::mapToBorrowResponse);
    }

    @Override
    public BorrowStatsResponse getBorrowStats() {
        System.out.println("\n=== [ADMIN] Statistiques des emprunts ===");

        long totalBorrows = borrowRepository.count();
        List<Borrow> activeBorrows = borrowRepository.findByStatus(Borrow.BorrowStatus.ACTIVE, null).getContent();
        List<Borrow> overdueBorrows = borrowRepository.findOverdueBorrows(LocalDateTime.now());

        // Calculer les pénalités totales
        double totalPenalties = overdueBorrows.stream()
                .mapToDouble(Borrow::calculatePenalty)
                .sum();

        long totalUsers = userRepository.count();
        // Pour totalBooks, vous auriez besoin d'un BookRepository

        return new BorrowStatsResponse(
                totalBorrows,
                (long) activeBorrows.size(),
                (long) overdueBorrows.size(),
                totalUsers,
                0L, // totalBooks - à remplacer
                totalPenalties
        );
    }

    @Override
    public void logAdminAction(String action, String details) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        String ipAddress = request.getRemoteAddr();

        UserActivityLog log = new UserActivityLog(admin, "ADMIN_" + action, details, ipAddress);
        activityLogRepository.save(log);

        System.out.println("[ADMIN_LOG] " + action + " - " + details + " (IP: " + ipAddress + ")");
    }

    private BorrowResponse mapToBorrowResponse(Borrow borrow) {
        return new BorrowResponse(
                borrow.getId(),
                borrow.getUser().getId(),
                borrow.getUser().getEmail(),
                borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
                borrow.getBook().getId(),
                borrow.getBook().getTitle(),
                borrow.getBook().getAuthor(),
                borrow.getBorrowDate(),
                borrow.getDueDate(),
                borrow.getReturnDate(),
                borrow.getStatus().name(),
                borrow.getPenalty(),
                borrow.getNotes(),
                borrow.isOverdue(),
                borrow.getDaysOverdue()
        );
    }
}