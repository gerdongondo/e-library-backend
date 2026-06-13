package com.luv2code.springbootlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean active;
    private LocalDateTime lastLogin;
    private Long totalBorrows;
    private Long activeBorrows;
}