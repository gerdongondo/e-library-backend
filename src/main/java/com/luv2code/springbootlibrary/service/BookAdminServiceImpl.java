package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class BookAdminServiceImpl implements BookAdminService {

    @Autowired
    private BookRepository bookRepository;

    // ---------------- CREATE BOOK ----------------
    @Override
    @Transactional
    public Book createBookWithPdfUpload(
            String title,
            String author,
            String description,
            String category,
            Double price,
            Integer numberOfPages,
            MultipartFile pdfFile,
            MultipartFile imgFile
    ) {
        // ✅ MODIFICATION : Suppression de la vérification obligatoire du PDF
        // Le PDF est maintenant optionnel
        // if (pdfFile == null || pdfFile.isEmpty()) {
        //     throw new IllegalArgumentException("Le fichier PDF est requis.");
        // }

        // ✅ MODIFICATION : On ne valide le PDF que s'il est présent
        if (pdfFile != null && !pdfFile.isEmpty()) {
            String contentType = pdfFile.getContentType();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                String originalName = pdfFile.getOriginalFilename();
                if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
                    throw new IllegalArgumentException("Le fichier doit être un PDF.");
                }
            }
        }

        try {
            // Créer le dossier uploads/books si inexistant
            Path uploadDir = Paths.get("uploads", "books");
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Créer le Book et remplir les champs
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setDescription(description);
            book.setCategory(category);
            book.setPrice(price);
            book.setNumberOfPages(numberOfPages);

            // ✅ MODIFICATION : Gestion du PDF seulement s'il est fourni
            if (pdfFile != null && !pdfFile.isEmpty()) {
                // Générer un nom unique pour le PDF
                String originalFilename = StringUtils.cleanPath(pdfFile.getOriginalFilename());
                String extension = ".pdf";
                int extIndex = originalFilename.lastIndexOf('.');
                if (extIndex >= 0) {
                    extension = originalFilename.substring(extIndex);
                }
                String filename = UUID.randomUUID().toString() + extension;
                Path targetPath = uploadDir.resolve(filename).normalize();

                // Copier le PDF sur le serveur
                try (InputStream in = pdfFile.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                book.setPdfUrl("uploads/books/" + filename);
            } else {
                // ✅ Aucun PDF -> on laisse null
                book.setPdfUrl(null);
            }

            // ---------------- GESTION IMAGE ----------------
            if (imgFile != null && !imgFile.isEmpty()) {
                Path imgUploadDir = Paths.get("uploads", "images");
                if (Files.notExists(imgUploadDir)) {
                    Files.createDirectories(imgUploadDir);
                }

                String originalImgFilename = StringUtils.cleanPath(imgFile.getOriginalFilename());
                String imgExtension = "";
                int imgExtIndex = originalImgFilename.lastIndexOf('.');
                if (imgExtIndex >= 0) {
                    imgExtension = originalImgFilename.substring(imgExtIndex);
                }

                String imgFilename = UUID.randomUUID().toString() + imgExtension;
                Path targetImgPath = imgUploadDir.resolve(imgFilename).normalize();

                try (InputStream in = imgFile.getInputStream()) {
                    Files.copy(in, targetImgPath, StandardCopyOption.REPLACE_EXISTING);
                }

                book.setImg("uploads/images/" + imgFilename);
            }

            // Sauvegarder le Book
            return bookRepository.save(book);

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier PDF ou de l'image.", ex);
        }
    }

    // ---------------- UPDATE BOOK ----------------
    @Override
    @Transactional
    public Book updateBookDetails(
            Long bookId,
            String title,
            String author,
            String description,
            String category,
            Double price,
            Integer numberOfPages,
            MultipartFile pdfFile,
            MultipartFile imgFile
    ) {
        // Récupérer le livre existant
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec id: " + bookId));

        // Mettre à jour les champs
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setCategory(category);
        book.setPrice(price);
        book.setNumberOfPages(numberOfPages);

        try {
            // ---------------- PDF ----------------
            // ✅ Modification : déjà optionnel, on ne change que si un nouveau fichier est fourni
            if (pdfFile != null && !pdfFile.isEmpty()) {
                String contentType = pdfFile.getContentType();
                if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                    String originalName = pdfFile.getOriginalFilename();
                    if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
                        throw new IllegalArgumentException("Le fichier doit être un PDF.");
                    }
                }

                // Supprimer l'ancien PDF
                if (book.getPdfUrl() != null) {
                    Path oldPdf = Paths.get(book.getPdfUrl());
                    if (Files.exists(oldPdf)) {
                        Files.delete(oldPdf);
                    }
                }

                Path uploadDir = Paths.get("uploads", "books");
                if (Files.notExists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String originalFilename = StringUtils.cleanPath(pdfFile.getOriginalFilename());
                String extension = ".pdf";
                int extIndex = originalFilename.lastIndexOf('.');
                if (extIndex >= 0) {
                    extension = originalFilename.substring(extIndex);
                }

                String filename = UUID.randomUUID().toString() + extension;
                Path targetPath = uploadDir.resolve(filename).normalize();

                try (InputStream in = pdfFile.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                book.setPdfUrl("uploads/books/" + filename);
            }

            // ---------------- IMAGE ----------------
            if (imgFile != null && !imgFile.isEmpty()) {
                // Supprimer l'ancien fichier image
                if (book.getImg() != null) {
                    Path oldImg = Paths.get(book.getImg());
                    if (Files.exists(oldImg)) {
                        Files.delete(oldImg);
                    }
                }

                Path imgUploadDir = Paths.get("uploads", "images");
                if (Files.notExists(imgUploadDir)) {
                    Files.createDirectories(imgUploadDir);
                }

                String originalImgFilename = StringUtils.cleanPath(imgFile.getOriginalFilename());
                String imgExtension = "";
                int imgExtIndex = originalImgFilename.lastIndexOf('.');
                if (imgExtIndex >= 0) {
                    imgExtension = originalImgFilename.substring(imgExtIndex);
                }

                String imgFilename = UUID.randomUUID().toString() + imgExtension;
                Path targetImgPath = imgUploadDir.resolve(imgFilename).normalize();

                try (InputStream in = imgFile.getInputStream()) {
                    Files.copy(in, targetImgPath, StandardCopyOption.REPLACE_EXISTING);
                }

                book.setImg("uploads/images/" + imgFilename);
            }

            return bookRepository.save(book);

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la mise à jour du PDF ou de l'image.", ex);
        }
    }

    // ---------------- DELETE BOOK ----------------
    @Override
    @Transactional
    public void deleteBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec id: " + bookId));

        try {
            // Supprimer PDF
            if (book.getPdfUrl() != null) {
                Path pdfPath = Paths.get(book.getPdfUrl());
                if (Files.exists(pdfPath)) {
                    Files.delete(pdfPath);
                }
            }

            // Supprimer IMAGE
            if (book.getImg() != null) {
                Path imgPath = Paths.get(book.getImg());
                if (Files.exists(imgPath)) {
                    Files.delete(imgPath);
                }
            }

            // Supprimer le livre
            bookRepository.delete(book);

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la suppression du fichier PDF ou de l'image.", ex);
        }
    }


    //----- ajout image a un livre -----

    @Override
    public void attachImageOnly(Long bookId, MultipartFile imgFile) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + bookId));
        try {
            // 1. Créer le dossier uploads/images
            Path imgUploadDir = Paths.get("uploads", "images");
            if (Files.notExists(imgUploadDir)) {
                Files.createDirectories(imgUploadDir);
            }

            // 2. Générer un nom unique pour l'image
            String originalFilename = StringUtils.cleanPath(imgFile.getOriginalFilename());
            String extension = "";
            int extIndex = originalFilename.lastIndexOf('.');
            if (extIndex >= 0) {
                extension = originalFilename.substring(extIndex);
            }
            String imgFilename = UUID.randomUUID().toString() + extension;
            Path targetImgPath = imgUploadDir.resolve(imgFilename).normalize();

            // 3. Copier le fichier
            try (InputStream in = imgFile.getInputStream()) {
                Files.copy(in, targetImgPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 4. Supprimer l'ancienne image si elle existe
            if (book.getImg() != null) {
                Path oldImg = Paths.get(book.getImg());
                if (Files.exists(oldImg)) {
                    Files.delete(oldImg);
                }
            }

            // 5. Enregistrer le nouveau chemin
            book.setImg("uploads/images/" + imgFilename);
            bookRepository.save(book);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage de l'image", e);
        }
    }


    @Override
    @Transactional
    public void attachPdfOnly(Long bookId, MultipartFile pdfFile) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + bookId));
        try {
            Path uploadDir = Paths.get("uploads", "books");
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = StringUtils.cleanPath(pdfFile.getOriginalFilename());
            String extension = "";
            int extIndex = originalFilename.lastIndexOf('.');
            if (extIndex >= 0) {
                extension = originalFilename.substring(extIndex);
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadDir.resolve(filename).normalize();
            pdfFile.transferTo(targetPath);
            // Supprimer l'ancien PDF si existant
            if (book.getPdfUrl() != null) {
                Path oldPdf = Paths.get(book.getPdfUrl());
                if (Files.exists(oldPdf)) {
                    Files.delete(oldPdf);
                }
            }
            book.setPdfUrl("uploads/books/" + filename);
            bookRepository.save(book);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage du PDF", e);
        }
    }


    //----- pour lire livre sur page admin------
    @Override
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }


    // ---------------- DELETE PDF ONLY ----------------
    @Override
    @Transactional
    public void deleteBookPdfFile(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec id: " + bookId));

        try {
            if (book.getPdfUrl() != null) {
                Path pdfPath = Paths.get(book.getPdfUrl());
                if (Files.exists(pdfPath)) {
                    Files.delete(pdfPath);
                }
                book.setPdfUrl(null);
                bookRepository.save(book);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors de la suppression du PDF.", ex);
        }
    }
}