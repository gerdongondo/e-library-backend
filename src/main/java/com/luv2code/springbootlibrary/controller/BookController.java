package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.UserRepository;
import com.luv2code.springbootlibrary.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // ========== ENDPOINTS EXISTANTS (inchangés) ==========

    @GetMapping("/search")
    public ResponseEntity<Page<Book>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<Book> books = bookService.advancedSearch(title, author, category, keyword, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/combined")
    public ResponseEntity<Page<Book>> searchCombined(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<Book> books = bookService.searchByTitleAuthorCategory(title, author, category, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/title")
    public ResponseEntity<Page<Book>> searchByTitle(
            @RequestParam String title,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<Book> books = bookService.searchByTitle(title, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/category")
    public ResponseEntity<Page<Book>> searchByCategory(
            @RequestParam String category,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<Book> books = bookService.searchByCategory(category, pageable);
        return ResponseEntity.ok(books);
    }

    // ========== NOUVEAUX ENDPOINTS POUR LA GESTION DES PDF ==========

    /**
     * Upload d'un PDF pour un livre (Admin uniquement)
     * Exemple : POST /api/books/admin/4/pdf (ou /api/admin/books/4/pdf)
     * Note : l'URL suit la convention /api/books/admin/{bookId}/pdf pour rester cohérent
     */
    @PostMapping("/admin/{bookId}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadPdf(@PathVariable Long bookId,
                                       @RequestParam("pdf") MultipartFile pdfFile) {
        bookService.attachPdfToBook(bookId, pdfFile);
        return ResponseEntity.ok().body("PDF attaché avec succès");
    }

    /**
     * Suppression du PDF d'un livre (Admin uniquement)
     */
    @DeleteMapping("/admin/{bookId}/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePdf(@PathVariable Long bookId) {
        bookService.deletePdfFromBook(bookId);
        return ResponseEntity.ok().body("PDF supprimé");
    }

    /**
     * Lecture du PDF (inline). Seul l'utilisateur ayant emprunté le livre (ou admin) peut y accéder.
     */
    @GetMapping("/{bookId}/pdf")
    public ResponseEntity<?> getPdf(@PathVariable Long bookId,
                                    @AuthenticationPrincipal UserDetails currentUser) {
        try {
            Long userId = extractUserId(currentUser);
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
            if (book.getPdfUrl() == null || book.getPdfUrl().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            // Correction : résolution du chemin
            Path pdfPath = resolveFileSystemPath(book.getPdfUrl(), "uploads/books/pdfs", "uploads/books");
            if (!Files.exists(pdfPath) || !Files.isReadable(pdfPath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(pdfPath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("Accès interdit");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // lire l'image ajoutée au livre
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getBookImage(@PathVariable Long id) throws IOException {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        // Récupérer la valeur stockée dans la colonne img
        Object imgValue = book.getImg();

        if (imgValue == null) {
            return ResponseEntity.notFound().build();
        }

        // 📌 CAS 1 : L'image est stockée en MEDIUMBLOB (byte[])
        if (imgValue instanceof byte[]) {
            byte[] imageBytes = (byte[]) imgValue;
            if (imageBytes.length == 0) {
                return ResponseEntity.notFound().build();
            }
            String contentType = detectImageType(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        }

        // 📌 CAS 2 : L'image est stockée sous forme de chaîne (chemin de fichier)
        if (imgValue instanceof String) {
            String imgPath = (String) imgValue;
            if (imgPath.isBlank()) {
                return ResponseEntity.notFound().build();
            }

            // Traitement comme chemin de fichier
            Path path = resolveFileSystemPath(imgPath, "uploads/images");
            if (!Files.exists(path) || !Files.isReadable(path)) {
                String fileName = Paths.get(imgPath).getFileName().toString();
                Path fallback = Paths.get("uploads/images", fileName);
                if (Files.exists(fallback)) {
                    path = fallback;
                } else {
                    return ResponseEntity.notFound().build();
                }
            }

            byte[] image = Files.readAllBytes(path);

            String contentType = Files.probeContentType(path);
            if (contentType == null || !contentType.startsWith("image/")) {
                String fileName = path.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(image);
        }

        // Aucun format reconnu
        return ResponseEntity.notFound().build();
    }



    // Méthode utilitaire : vérifie si une chaîne est hexadécimale (0-9A-Fa-f) et longueur paire
    private boolean isHexString(String s) {
        if (s == null || s.isEmpty()) return false;
        if (s.length() % 2 != 0) return false;
        return s.matches("^[0-9A-Fa-f]+$");
    }



    // Méthode utilitaire pour extraire l'ID utilisateur à partir du UserDetails
    private Long extractUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé avec l'email : " + email));
    }


    /**
     * Résout un chemin stocké en base (peut être un simple nom de fichier ou un chemin relatif).
     * Essaie plusieurs dossiers de base pour retrouver le fichier.
     * @param storedPath la valeur du champ (ex: book.getPdfUrl() ou book.getImg())
     * @param baseDirs dossiers où chercher (ex: "uploads/books/pdfs", "uploads/books")
     * @return le Path absolu normalisé du fichier (existe ou pas)
     */
    private Path resolveFileSystemPath(String storedPath, String... baseDirs) {
        if (storedPath == null || storedPath.isBlank()) return null;

        // 1. Chemin exact tel qu'il est stocké (relatif ou absolu)
        Path direct = Paths.get(storedPath);
        if (Files.exists(direct)) return direct.toAbsolutePath().normalize();

        // 2. Extraire le nom du fichier (dernier segment)
        String fileName = Paths.get(storedPath).getFileName().toString();

        // 3. Essayer chaque dossier de base
        for (String baseDir : baseDirs) {
            Path candidate = Paths.get(baseDir, fileName);
            if (Files.exists(candidate)) return candidate.toAbsolutePath().normalize();

            // Essayer baseDir + storedPath complet
            Path fullCandidate = Paths.get(baseDir, storedPath);
            if (Files.exists(fullCandidate)) return fullCandidate.toAbsolutePath().normalize();
        }

        // 4. Fallback : retourne le chemin direct (même inexistant)
        return direct.toAbsolutePath().normalize();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    private String detectImageType(byte[] bytes) {
        if (bytes.length >= 4) {
            // JPEG
            if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
                return "image/jpeg";
            }
            // PNG
            if (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 &&
                    bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47) {
                return "image/png";
            }
            // GIF
            if (bytes[0] == (byte) 0x47 && bytes[1] == (byte) 0x49 &&
                    bytes[2] == (byte) 0x46) {
                return "image/gif";
            }
        }
        return "image/jpeg"; // fallback
    }

}