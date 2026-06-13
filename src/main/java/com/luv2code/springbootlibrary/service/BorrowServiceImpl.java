package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.BorrowRequest;
import com.luv2code.springbootlibrary.dto.BorrowResponse;
import com.luv2code.springbootlibrary.entity.*;
import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.repository.BorrowRepository;
import com.luv2code.springbootlibrary.repository.UserActivityLogRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BorrowServiceImpl implements BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserActivityLogRepository activityLogRepository;

    @Autowired
    private HttpServletRequest request;

    @Value("${borrow.duration.days:14}")
    private int defaultBorrowDuration;

    @Value("${borrow.max.concurrent:5}")
    private int maxConcurrentBorrows;

    @Autowired
    private AuditLogService auditLogService;


    @Override
    @Transactional
    public BorrowResponse borrowBook(BorrowRequest borrowRequest) {
        System.out.println("\n=== [BORROW] Nouvel emprunt demandé ===");

        // 1. Récupérer l'utilisateur connecté
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        System.out.println("[BORROW] Utilisateur: " + user.getEmail());

        // 2. Vérifier le livre
        Book book = bookRepository.findById(borrowRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        System.out.println("[BORROW] Livre: " + book.getTitle() + " (ID: " + book.getId() + ")");

        // 3. Vérifier la disponibilité
        if (!isBookAvailable(book.getId())) {
            throw new RuntimeException("Ce livre n'est pas disponible pour le moment");
        }

        // 4. Vérifier la limite d'emprunts
        if (hasUserReachedBorrowLimit()) {
            throw new RuntimeException("Vous avez atteint le nombre maximum d'emprunts simultanés");
        }

        // 5. Vérifier si l'utilisateur a déjà emprunté ce livre
        boolean alreadyBorrowed = borrowRepository.findByBookIdAndStatus(book.getId(), Borrow.BorrowStatus.ACTIVE)
                .map(borrow -> borrow.getUser().getId().equals(user.getId()))
                .orElse(false);

        if (alreadyBorrowed) {
            throw new RuntimeException("Vous avez déjà emprunté ce livre");
        }

        // 6. Créer l'emprunt
        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDateTime.now());

        int duration = borrowRequest.getDurationDays() != null ?
                borrowRequest.getDurationDays() : defaultBorrowDuration;
        borrow.setDueDate(LocalDateTime.now().plusDays(duration));

        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        borrow.setNotes(borrowRequest.getNotes());

        // 7. Sauvegarder
        Borrow savedBorrow = borrowRepository.save(borrow);


        // Audit (nouveau)
        auditLogService.logBorrowBook(
                user.getId(),
                book.getId(),
                book.getTitle()
        );



        // 8. Mettre à jour la disponibilité du livre
        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        bookRepository.save(book);

        System.out.println("[BORROW] Emprunt créé avec succès. ID: " + savedBorrow.getId());

        // 9. Log l'action
        logUserAction("BORROW_BOOK",
                "Emprunt du livre: " + book.getTitle() + " (ID: " + book.getId() + ")");

        return mapToBorrowResponse(savedBorrow);
    }

    @Override
    @Transactional
    public BorrowResponse returnBook(Long bookId) {
        System.out.println("\n=== [BORROW] Retour de livre demandé ===");

        // 1. Récupérer l'utilisateur connecté
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Trouver l'emprunt actif pour ce livre et cet utilisateur
        Borrow borrow = borrowRepository.findByBookIdAndStatus(bookId, Borrow.BorrowStatus.ACTIVE)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Aucun emprunt actif trouvé pour ce livre"));

        System.out.println("[BORROW] Retour du livre: " + borrow.getBook().getTitle() +
                " par " + user.getEmail());

        // CORRECTION 1 : Déclarer penalty ici pour toute la méthode
        Double penalty = null; // 'Double' pour accepter null

        // 3. Mettre à jour l'emprunt
        borrow.setReturnDate(LocalDateTime.now());
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);

        // 4. Appliquer une pénalité si en retard
        if (borrow.isOverdue()) {
             penalty = borrow.calculatePenalty();
            borrow.setPenalty(penalty);
            System.out.println("[BORROW] Pénalité appliquée: " + penalty + "€");
        }

        Borrow updatedBorrow = borrowRepository.save(borrow);

        // Audit (nouveau)

        // CORRECTION : Récupérer le livre depuis borrow.getBook()
        Book book = borrow.getBook();

        auditLogService.logReturnBook(
                user.getId(),
                book.getId(),
                book.getTitle(),
                penalty
        );


        // 5. Mettre à jour la disponibilité du livre

        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);

        System.out.println("[BORROW] Livre retourné avec succès");

        // 6. Log l'action
        logUserAction("RETURN_BOOK",
                "Retour du livre: " + book.getTitle() + " (ID: " + book.getId() + ")");

        return mapToBorrowResponse(updatedBorrow);
    }

    @Override
    public List<BorrowResponse> getUserBorrows() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Borrow> borrows = borrowRepository.findByUserId(user.getId());

        return borrows.stream()
                .map(this::mapToBorrowResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowResponse> getUserActiveBorrows() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Borrow> activeBorrows = borrowRepository.findByUserIdAndStatus(
                user.getId(), Borrow.BorrowStatus.ACTIVE);

        return activeBorrows.stream()
                .map(this::mapToBorrowResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBookAvailable(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
        return book.getCopiesAvailable() > 0;
    }

    @Override
    public boolean hasUserReachedBorrowLimit() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Borrow> activeBorrows = borrowRepository.findByUserIdAndStatus(
                user.getId(), Borrow.BorrowStatus.ACTIVE);

        return activeBorrows.size() >= maxConcurrentBorrows;
    }

    @Override
    public void logUserAction(String action, String details) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String ipAddress = request.getRemoteAddr();

        UserActivityLog log = new UserActivityLog(user, action, details, ipAddress);
        activityLogRepository.save(log);

        System.out.println("[USER_LOG] " + action + " - " + details + " (User: " + user.getEmail() + ")");
    }

    private BorrowResponse mapToBorrowResponse(Borrow borrow) {
        return new BorrowResponse(
                borrow.getId(),
                borrow.getUser().getId(),
                borrow.getUser().getEmail(),
                borrow.getUser().getFirstName() + " " + borrow.getUser().getLastName(),
                borrow.getBook().getId(),
                borrow.getBook().getTitle(),
                borrow.getBook().getAuthor(),
                borrow.getBorrowDate(),
                borrow.getDueDate(),
                borrow.getReturnDate(),
                borrow.getStatus().name(),
                borrow.getPenalty(),
                borrow.getNotes(),
                borrow.isOverdue(),
                borrow.getDaysOverdue()
        );
    }
}