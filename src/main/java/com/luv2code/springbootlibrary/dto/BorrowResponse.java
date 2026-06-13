package com.luv2code.springbootlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private String status;
    private Double penalty;
    private String notes;
    private boolean overdue;
    private Long daysOverdue;
}