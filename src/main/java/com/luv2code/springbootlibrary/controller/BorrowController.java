package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.BorrowRequest;
import com.luv2code.springbootlibrary.dto.BorrowResponse;
import com.luv2code.springbootlibrary.service.BorrowService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    // ========== EMPRUNTS POUR UTILISATEURS ==========

    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {

        System.out.println("\n=== [BORROW CONTROLLER] Nouvel emprunt demandé ===");
        System.out.println("[BORROW] Book ID: " + request.getBookId());

        BorrowResponse response = borrowService.borrowBook(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long bookId) {

        System.out.println("\n=== [BORROW CONTROLLER] Retour de livre demandé ===");
        System.out.println("[BORROW] Book ID: " + bookId);

        BorrowResponse response = borrowService.returnBook(bookId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me/borrows")
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<BorrowResponse>> getUserBorrows() {

        System.out.println("\n=== [BORROW CONTROLLER] Historique des emprunts ===");

        List<BorrowResponse> borrows = borrowService.getUserBorrows();
        return ResponseEntity.ok(borrows);
    }

    @GetMapping("/users/me/borrows/active")
    @PreAuthorize("hasAnyRole('STUDENT', 'LIBRARIAN', 'ADMIN')")
    public ResponseEntity<List<BorrowResponse>> getUserActiveBorrows() {

        System.out.println("\n=== [BORROW CONTROLLER] Emprunts actifs ===");

        List<BorrowResponse> activeBorrows = borrowService.getUserActiveBorrows();
        return ResponseEntity.ok(activeBorrows);
    }

    @GetMapping("/books/{bookId}/availability")
    public ResponseEntity<Boolean> checkBookAvailability(@PathVariable Long bookId) {

        System.out.println("\n=== [BORROW CONTROLLER] Vérification disponibilité ===");
        System.out.println("[BORROW] Book ID: " + bookId);

        boolean available = borrowService.isBookAvailable(bookId);
        return ResponseEntity.ok(available);
    }
}