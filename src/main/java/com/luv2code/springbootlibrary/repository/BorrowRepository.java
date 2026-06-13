package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    // Emprunts d'un utilisateur
    List<Borrow> findByUserId(Long userId);

    // Emprunts actifs d'un utilisateur
    List<Borrow> findByUserIdAndStatus(Long userId, Borrow.BorrowStatus status);

    // Vérifier si un livre est actuellement emprunté
    Optional<Borrow> findByBookIdAndStatus(Long bookId, Borrow.BorrowStatus status);

    // Emprunts actifs (tous utilisateurs)
    Page<Borrow> findByStatus(Borrow.BorrowStatus status, Pageable pageable);

    // Emprunts en retard
    @Query("SELECT b FROM Borrow b WHERE b.status = 'ACTIVE' AND b.dueDate < :now")
    List<Borrow> findOverdueBorrows(@Param("now") LocalDateTime now);



    // Statistiques
    Long countByUserId(Long userId);
    Long countByBookId(Long bookId);

    // Recherche avec filtres pour Admin
    @Query("SELECT b FROM Borrow b WHERE " +
            "(:userId IS NULL OR b.user.id = :userId) AND " +
            "(:bookId IS NULL OR b.book.id = :bookId) AND " +
            "(:status IS NULL OR b.status = :status)")
    Page<Borrow> findWithFilters(@Param("userId") Long userId,
                                 @Param("bookId") Long bookId,
                                 @Param("status") Borrow.BorrowStatus status,
                                 Pageable pageable);


    // ========== NOUVELLE MÉTHODE POUR VÉRIFIER L'EMPRUNT ACTIF (utilisée par BookService) ==========
    /**
     * Vérifie si un utilisateur a actuellement emprunté un livre (emprunt actif).
     * Utilisé pour l'accès aux PDF.
     *
     * @param userId ID de l'utilisateur
     * @param bookId ID du livre
     * @param status Statut de l'emprunt (en général "ACTIVE")
     * @return true si l'emprunt existe et est actif
     */


    // ========== NOUVELLE MÉTHODE AVEC @Query (corrigée) ==========
    @Query("SELECT COUNT(b) > 0 FROM Borrow b WHERE b.user.id = :userId AND b.book.id = :bookId AND b.status = :status")
    boolean existsByUserIdAndBookIdAndStatus(@Param("userId") Long userId,
                                             @Param("bookId") Long bookId,
                                             @Param("status") Borrow.BorrowStatus status);

}