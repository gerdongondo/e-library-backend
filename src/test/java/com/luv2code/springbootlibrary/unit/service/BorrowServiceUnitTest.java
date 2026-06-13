package com.luv2code.springbootlibrary.unit.service;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dto.BorrowRequest;
import com.luv2code.springbootlibrary.dto.BorrowResponse;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Borrow;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.BorrowRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import com.luv2code.springbootlibrary.service.AuditLogService;
import com.luv2code.springbootlibrary.service.BorrowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowServiceUnitTest {

    @Mock private BorrowRepository borrowRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private AuditLogService auditLogService;
    @InjectMocks private BorrowServiceImpl borrowService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("student@univ.fr");

        book = new Book();
        book.setId(10L);
        book.setTitle("Java Master");
        book.setCopiesAvailable(3);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("student@univ.fr");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void borrowBook_WhenValid_ShouldReturnBorrowResponse() {
        when(userRepository.findByEmail("student@univ.fr")).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));

        // ✅ Utilisation de la méthode EXISTANTE : countByUserId
        when(borrowRepository.countByUserId(1L)).thenReturn(0L);

        when(borrowRepository.findByBookIdAndStatus(eq(10L), any())).thenReturn(Optional.empty());

        BorrowRequest request = new BorrowRequest();
        request.setBookId(10L);
        request.setDurationDays(14);

        BorrowResponse response = borrowService.borrowBook(request);

        assertNotNull(response);
        assertEquals(10L, response.getBookId());
        verify(auditLogService).logBorrow(anyLong(), anyString(), anyLong(), anyString(), anyString());
    }

    @Test
    void borrowBook_WhenBookNotAvailable_ShouldThrowException() {
        book.setCopiesAvailable(0);
        when(userRepository.findByEmail("student@univ.fr")).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        when(borrowRepository.countByUserId(1L)).thenReturn(0L);
        when(borrowRepository.findByBookIdAndStatus(eq(10L), any())).thenReturn(Optional.empty());

        BorrowRequest request = new BorrowRequest();
        request.setBookId(10L);
        request.setDurationDays(14);

        assertThrows(RuntimeException.class, () -> borrowService.borrowBook(request));
    }
}