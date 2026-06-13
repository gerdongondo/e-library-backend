package com.luv2code.springbootlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowStatsResponse {

    private Long totalBorrows;
    private Long activeBorrows;
    private Long overdueBorrows;
    private Long totalUsers;
    private Long totalBooks;
    private Double totalPenalties;
}