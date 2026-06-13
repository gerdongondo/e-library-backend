package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.entity.Borrow;
import com.luv2code.springbootlibrary.repository.BorrowRepository; // à créer si nécessaire
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private BorrowRepository borrowRepository; // à adapter selon votre modèle d'emprunt

    // ========== MÉTHODES EXISTANTES (recopiées telles quelles) ==========
    public Page<Book> advancedSearch(String title, String author,
                                     String category, String keyword,
                                     Pageable pageable) {
        title = cleanParameter(title);
        author = cleanParameter(author);
        category = cleanParameter(category);
        keyword = cleanParameter(keyword);
        return bookRepository.advancedSearch(title, author, category, keyword, pageable);
    }

    public Page<Book> searchByTitleAuthorCategory(String title, String author,
                                                  String category, Pageable pageable) {
        title = cleanParameter(title);
        author = cleanParameter(author);
        category = cleanParameter(category);
        return bookRepository.searchByTitleAuthorCategory(title, author, category, pageable);
    }

    public Page<Book> searchByTitle(String title, Pageable pageable) {
        title = cleanParameter(title);
        return bookRepository.findByTitleContaining(title, pageable);
    }

    public Page<Book> searchByCategory(String category, Pageable pageable) {
        category = cleanParameter(category);
        return bookRepository.findByCategory(category, pageable);
    }

    private String cleanParameter(String param) {
        if (param == null || param.trim().isEmpty()) {
            return null;
        }
        return param.trim();
    }

    // ========== NOUVELLES MÉTHODES POUR LA GESTION DES PDF ==========

    /**
     * Attache un fichier PDF à un livre (remplace l'ancien PDF s'il existe).
     * Utilisé par l'admin.
     */
    public void attachPdfToBook(Long bookId, MultipartFile pdfFile) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + bookId));
        // Supprimer l'ancien PDF s'il existe
        if (book.getPdfUrl() != null && !book.getPdfUrl().isEmpty()) {
            fileStorageService.deletePdf(book.getPdfUrl());
        }
        String pdfName = fileStorageService.storePdf(pdfFile);
        book.setPdfUrl(pdfName);
        bookRepository.save(book);
    }

    /**
     * Supprime le PDF associé à un livre.
     * Utilisé par l'admin.
     */
    public void deletePdfFromBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + bookId));
        if (book.getPdfUrl() != null && !book.getPdfUrl().isEmpty()) {
            fileStorageService.deletePdf(book.getPdfUrl());
            book.setPdfUrl(null);
            bookRepository.save(book);
        }
    }

    /**
     * Récupère le chemin du fichier PDF après vérification des droits.
     * Seul l'admin ou l'utilisateur ayant emprunté le livre peut y accéder.
     */
    public Path getPdfFile(Long bookId, Long userId, boolean isAdmin) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
        if (book.getPdfUrl() == null || book.getPdfUrl().isEmpty()) {
            throw new RuntimeException("Aucun PDF associé à ce livre");
        }
        if (!isAdmin && !hasUserBorrowedBook(userId, bookId)) {
            throw new SecurityException("Vous n'avez pas emprunté ce livre");
        }
        return fileStorageService.loadPdf(book.getPdfUrl());
    }

    /**
     * Vérifie si un utilisateur a actuellement emprunté un livre.
     * À adapter selon votre modèle d'emprunt (Borrow, Loan, etc.)
     */
    private boolean hasUserBorrowedBook(Long userId, Long bookId) {
        return borrowRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, Borrow.BorrowStatus.ACTIVE);
    }
}