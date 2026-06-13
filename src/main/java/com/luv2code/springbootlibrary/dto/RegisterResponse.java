package com.luv2code.springbootlibrary.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
