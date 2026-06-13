package com.luv2code.springbootlibrary.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String type = "Bearer";  // Valeur par défaut
    private Long id;
    private String email;
    private String role;

    public LoginResponse(String token, Long id, String email, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }
}