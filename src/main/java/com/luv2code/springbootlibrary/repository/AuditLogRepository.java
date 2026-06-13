package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Trouver par type d'action
    Page<AuditLog> findByActionType(AuditLog.AuditActionType actionType, Pageable pageable);

    // Trouver par utilisateur
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // Trouver par utilisateur et type d'action
    Page<AuditLog> findByUserIdAndActionType(Long userId, AuditLog.AuditActionType actionType, Pageable pageable);

    // Recherche avancée avec filtres multiples
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:userId IS NULL OR a.userId = :userId) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:entityId IS NULL OR a.entityId = :entityId) AND " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate)")
    Page<AuditLog> findWithFilters(
            @Param("userId") Long userId,
            @Param("actionType") AuditLog.AuditActionType actionType,
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Statistiques : nombre d'actions par type
    @Query("SELECT a.actionType, COUNT(a) FROM AuditLog a GROUP BY a.actionType ORDER BY COUNT(a) DESC")
    List<Object[]> countActionsByType();

    // Statistiques : activité par utilisateur
    @Query("SELECT a.userEmail, COUNT(a) FROM AuditLog a GROUP BY a.userEmail ORDER BY COUNT(a) DESC")
    List<Object[]> countActionsByUser();

    // Nettoyer les vieux logs (plus de 6 mois)
    void deleteByCreatedAtBefore(LocalDateTime date);
}