package com.luv2code.springbootlibrary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String message;

    // Constructeur sans le message (pour compatibilité)
    public UpdateProfileResponse(Long id, String email, String firstName,
                                 String lastName, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.message = "Profil mis à jour avec succès";
    }
}