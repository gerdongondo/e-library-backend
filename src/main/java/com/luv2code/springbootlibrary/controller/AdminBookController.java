package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.service.BookAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    private final BookAdminService bookAdminService;

    public AdminBookController(BookAdminService bookAdminService) {
        this.bookAdminService = bookAdminService;
    }

    // ---------------- AJOUTER UN LIVRE AVEC PDF ET IMAGE ----------------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> createBook(
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Integer numberOfPages,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,   // ← required=false,
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile // <-- ajout image
    ) {
        Book created = bookAdminService.createBookWithPdfUpload(
                title, author, description, category, price, numberOfPages, pdfFile, imgFile
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ---------------- MODIFIER UN LIVRE AVEC PDF ET IMAGE ----------------
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String author,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) Integer numberOfPages,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "imgFile", required = false) MultipartFile imgFile // <-- ajout image
    ) {
        Book updated = bookAdminService.updateBookDetails(
                id, title, author, description, category, price, numberOfPages, pdfFile, imgFile
        );
        return ResponseEntity.ok(updated);
    }

    // ---------------- SUPPRIMER UN LIVRE ET SES FICHIERS ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id) {
        bookAdminService.deleteBookById(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Livre supprimé avec succès"));
    }

    // ---------------- SUPPRIMER UNIQUEMENT LE PDF ----------------
    @DeleteMapping("/{id}/pdf")
    public ResponseEntity<Map<String, String>> deleteBookPdf(@PathVariable Long id) {
        bookAdminService.deleteBookPdfFile(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "PDF supprimé avec succès"));
    }


    // ------- upload d’image seule  ---------------
    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id,
                                         @RequestParam("imgFile") MultipartFile imgFile) {
        bookAdminService.attachImageOnly(id, imgFile);
        return ResponseEntity.ok().body("Image ajoutée");
    }

    // ------- upload d’un PDF seule ---------------
    @PostMapping("/{id}/pdf")
    public ResponseEntity<?> uploadPdf(@PathVariable Long id,
                                       @RequestParam("pdfFile") MultipartFile pdfFile) {
        bookAdminService.attachPdfOnly(id, pdfFile);
        return ResponseEntity.ok().body("PDF ajouté");
    }


    //-----   pour lire les livres sur page admin    ----------
    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(Pageable pageable) {
        Page<Book> books = bookAdminService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    // ---------------- GESTION DES EXCEPTIONS ----------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", ex.getMessage()));
    }
}
