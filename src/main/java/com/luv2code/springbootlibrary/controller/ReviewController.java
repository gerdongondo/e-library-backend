package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.ReviewDTO;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.UserRepository;
import com.luv2code.springbootlibrary.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository; // Injection du repository utilisateur

    /**
     * Ajouter un avis (authentifié)
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long userId = extractUserId(currentUser);
        ReviewDTO createdReview = reviewService.addReview(reviewDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    /**
     * Modifier son propre avis (authentifié)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long userId = extractUserId(currentUser);
        ReviewDTO updatedReview = reviewService.updateReview(id, reviewDTO, userId);
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * Supprimer son propre avis (authentifié)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {

        Long userId = extractUserId(currentUser);
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer tous les avis d'un livre (public) – endpoint conservé
     */
    @GetMapping("/search/findByBookId")
    public ResponseEntity<Page<ReviewDTO>> findByBookId(
            @RequestParam Long bookId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewDTO> reviews = reviewService.getReviewsByBookId(bookId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Extrait l'ID utilisateur à partir de l'email contenu dans UserDetails.
     * Cette méthode évite de caster UserDetails en une classe spécifique.
     */
    private Long extractUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé avec l'email : " + email));
    }
}