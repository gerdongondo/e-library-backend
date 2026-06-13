package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    // Gestion des utilisateurs
    UserProfileResponse updateUserRole(Long userId, UserRoleUpdateRequest request);
    UserProfileResponse toggleUserActiveStatus(Long userId, boolean active);
    Page<UserListResponse> getAllUsers(Pageable pageable);
    Page<UserActivityResponse> getUserActivity(Long userId, Pageable pageable);

    // Gestion des emprunts
    BorrowResponse extendBorrow(Long borrowId, int additionalDays);
    BorrowResponse cancelBorrow(Long borrowId, String reason);
    Page<BorrowResponse> getAllBorrows(Long userId, Long bookId, String status, Pageable pageable);
    BorrowStatsResponse getBorrowStats();

    // Log d'activité
    void logAdminAction(String action, String details);
}