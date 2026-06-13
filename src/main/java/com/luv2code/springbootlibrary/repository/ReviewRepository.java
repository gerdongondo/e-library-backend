package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Vérifier si l'utilisateur a déjà donné un avis pour un livre
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Récupérer l'avis d'un utilisateur pour un livre (pour modification/suppression)
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);

    // Récupérer tous les avis d'un livre (avec pagination)
    Page<Review> findByBookId(Long bookId, Pageable pageable);

    // Compter les avis d'un livre
    long countByBookId(Long bookId);

    // Calculer la moyenne des notes d'un livre
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);
}