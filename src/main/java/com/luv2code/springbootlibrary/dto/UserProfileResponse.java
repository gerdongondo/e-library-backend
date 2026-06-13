package com.luv2code.springbootlibrary.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    // Constructeur pour faciliter l'instanciation
    public UserProfileResponse(Long id, String email, String firstName, String lastName, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}