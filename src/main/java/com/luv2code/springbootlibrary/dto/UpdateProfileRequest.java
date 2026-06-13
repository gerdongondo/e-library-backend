package com.luv2code.springbootlibrary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    private String lastName;

    // Constructeur par défaut (nécessaire pour Jackson)
    public UpdateProfileRequest() {}

    // Constructeur avec paramètres (optionnel)
    public UpdateProfileRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}