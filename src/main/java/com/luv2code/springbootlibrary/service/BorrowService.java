package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.BorrowRequest;
import com.luv2code.springbootlibrary.dto.BorrowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowService {

    // Pour les étudiants
    BorrowResponse borrowBook(BorrowRequest request);
    BorrowResponse returnBook(Long bookId);
    List<BorrowResponse> getUserBorrows();
    List<BorrowResponse> getUserActiveBorrows();

    // Vérifications
    boolean isBookAvailable(Long bookId);
    boolean hasUserReachedBorrowLimit();

    // Log d'activité
    void logUserAction(String action, String details);
}