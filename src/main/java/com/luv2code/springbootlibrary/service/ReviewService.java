package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dto.ReviewDTO;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Review;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.ReviewRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Ajouter un avis (l'utilisateur ne peut en avoir qu'un par livre)
     */
    @Transactional
    public ReviewDTO addReview(ReviewDTO reviewDTO, Long userId) {
        // Vérifier si l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Vérifier si le livre existe
        Book book = bookRepository.findById(reviewDTO.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Livre non trouvé"));

        // Vérifier que l'utilisateur n'a pas déjà un avis pour ce livre
        if (reviewRepository.existsByUserIdAndBookId(userId, reviewDTO.getBookId())) {
            throw new IllegalStateException("Vous avez déjà donné un avis pour ce livre");
        }

        // Créer et sauvegarder l'avis
        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setReviewDescription(reviewDTO.getReviewDescription());
        review.setBook(book);
        review.setUser(user);

        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    /**
     * Modifier son propre avis
     */
    @Transactional
    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Avis non trouvé"));

        // Vérifier que l'avis appartient bien à l'utilisateur connecté
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet avis");
        }

        // Mettre à jour les champs
        review.setRating(reviewDTO.getRating());
        review.setReviewDescription(reviewDTO.getReviewDescription());

        Review updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    /**
     * Supprimer son propre avis
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Avis non trouvé"));

        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cet avis");
        }

        reviewRepository.delete(review);
    }

    /**
     * Récupérer tous les avis d'un livre (public)
     */
    public Page<ReviewDTO> getReviewsByBookId(Long bookId, Pageable pageable) {
        // Vérifier que le livre existe (optionnel)
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException("Livre non trouvé");
        }
        Page<Review> reviews = reviewRepository.findByBookId(bookId, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Convertir une entité Review en DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setReviewDescription(review.getReviewDescription());
        dto.setDate(review.getDate());
        dto.setBookId(review.getBook().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserEmail(review.getUser().getEmail());
        return dto;
    }
}