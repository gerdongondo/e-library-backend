package com.luv2code.springbootlibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow")
@Data
@NoArgsConstructor
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime borrowDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    @Column
    private Double penalty;

    @Column(length = 500)
    private String notes;

    public enum BorrowStatus {
        ACTIVE, RETURNED, OVERDUE, CANCELLED, EXTENDED
    }

    // Vérifie si l'emprunt est en retard
    public boolean isOverdue() {
        return status == BorrowStatus.ACTIVE &&
                LocalDateTime.now().isAfter(dueDate);
    }

    // Calculer le nombre de jours de retard
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
    }

    // Calculer la pénalité
    public double calculatePenalty() {
        if (!isOverdue()) return 0.0;
        return getDaysOverdue() * 0.50; // 0.50€ par jour
    }
}