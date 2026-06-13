package com.luv2code.springbootlibrary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull(message = "L'ID du livre est obligatoire")
    private Long bookId;

    // Durée en jours (optionnel, sinon valeur par défaut)
    private Integer durationDays;

    private String notes; // Notes optionnelles
}