package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.*;
import com.luv2code.springbootlibrary.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Seuls les admins peuvent accéder à tous ces endpoints
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ========== GESTION DES UTILISATEURS ==========

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserProfileResponse> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request) {

        System.out.println("\n=== [ADMIN CONTROLLER] Mise à jour rôle utilisateur ID=" + userId + " ===");

        UserProfileResponse response = adminService.updateUserRole(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<UserProfileResponse> toggleUserActive(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        System.out.println("\n=== [ADMIN CONTROLLER] Changement statut utilisateur ID=" + userId + " ===");

        UserProfileResponse response = adminService.toggleUserActiveStatus(userId, active);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserListResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        System.out.println("\n=== [ADMIN CONTROLLER] Liste des utilisateurs ===");
        System.out.println("[ADMIN] Page: " + page + ", Size: " + size + ", Sort: " + sortBy + " " + sortDirection);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserListResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<Page<UserActivityResponse>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("\n=== [ADMIN CONTROLLER] Activité utilisateur ID=" + userId + " ===");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<UserActivityResponse> activity = adminService.getUserActivity(userId, pageable);
        return ResponseEntity.ok(activity);
    }

    // ========== GESTION DES EMPRUNTS ==========

    @GetMapping("/borrows")
    public ResponseEntity<Page<BorrowResponse>> getAllBorrows(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("\n=== [ADMIN CONTROLLER] Liste des emprunts ===");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));
        Page<BorrowResponse> borrows = adminService.getAllBorrows(userId, bookId, status, pageable);
        return ResponseEntity.ok(borrows);
    }

    @PutMapping("/borrows/{borrowId}/extend")
    public ResponseEntity<BorrowResponse> extendBorrow(
            @PathVariable Long borrowId,
            @RequestParam int additionalDays) {

        System.out.println("\n=== [ADMIN CONTROLLER] Prolongation emprunt ID=" + borrowId + " ===");

        if (additionalDays <= 0) {
            throw new RuntimeException("Le nombre de jours supplémentaires doit être positif");
        }

        BorrowResponse response = adminService.extendBorrow(borrowId, additionalDays);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/borrows/{borrowId}/cancel")
    public ResponseEntity<BorrowResponse> cancelBorrow(
            @PathVariable Long borrowId,
            @RequestParam String reason) {

        System.out.println("\n=== [ADMIN CONTROLLER] Annulation emprunt ID=" + borrowId + " ===");

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("La raison de l'annulation est obligatoire");
        }

        BorrowResponse response = adminService.cancelBorrow(borrowId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/borrows")
    public ResponseEntity<BorrowStatsResponse> getBorrowStats() {

        System.out.println("\n=== [ADMIN CONTROLLER] Statistiques des emprunts ===");

        BorrowStatsResponse stats = adminService.getBorrowStats();
        return ResponseEntity.ok(stats);
    }
}