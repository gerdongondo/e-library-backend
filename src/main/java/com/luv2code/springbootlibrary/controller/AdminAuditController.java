package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.AuditLogResponse;
import com.luv2code.springbootlibrary.entity.AuditLog;
import com.luv2code.springbootlibrary.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit", description = "Gestion des journaux d'audit")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminAuditController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/logs")
    @Operation(summary = "Consulter les journaux d'audit",
            description = "Retourne les journaux d'audit avec pagination et filtres")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditLog.AuditActionType actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate) {

        System.out.println("\n=== [AUDIT] Consultation des journaux par l'admin ===");
        System.out.println("[AUDIT] Filtres: userId=" + userId + ", actionType=" + actionType +
                ", fromDate=" + fromDate + ", toDate=" + toDate);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<AuditLogResponse> logs = auditLogService.getAuditLogs(
                userId, actionType, entityType, entityId, fromDate, toDate, pageable);

        System.out.println("[AUDIT] " + logs.getTotalElements() + " logs trouvés");
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des journaux",
            description = "Retourne des statistiques sur les journaux d'audit")
    public ResponseEntity<Map<String, Object>> getAuditStatistics() {
        System.out.println("\n=== [AUDIT] Consultation des statistiques ===");

        String stats = auditLogService.getAuditStatistics();
        System.out.println("[AUDIT] Statistiques générées");

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", stats);
        response.put("generatedAt", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions/types")
    @Operation(summary = "Liste des types d'actions",
            description = "Retourne la liste des types d'actions auditées")
    public ResponseEntity<Map<String, Object>> getActionTypes() {
        System.out.println("\n=== [AUDIT] Consultation des types d'actions ===");

        Map<String, Object> response = new HashMap<>();
        response.put("actionTypes", AuditLog.AuditActionType.values());
        response.put("count", AuditLog.AuditActionType.values().length);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test/log")
    @Operation(summary = "Test de journalisation",
            description = "Endpoint de test pour la journalisation (développement uniquement)")
    public ResponseEntity<Map<String, String>> testLog() {
        System.out.println("\n=== [AUDIT] Test de journalisation ===");

        // Test de différentes actions
        auditLogService.logAction(AuditLog.AuditActionType.LOGIN_SUCCESS,
                "Test de journalisation depuis endpoint admin");

        auditLogService.logAction(AuditLog.AuditActionType.ADMIN_VIEW_LOGS,
                "BOOK", 1L, "Spring Boot in Action",
                "Test de journalisation avec entité");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Tests de journalisation exécutés avec succès");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}