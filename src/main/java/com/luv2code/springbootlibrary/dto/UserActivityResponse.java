package com.luv2code.springbootlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}