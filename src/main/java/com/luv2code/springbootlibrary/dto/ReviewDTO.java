package com.luv2code.springbootlibrary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Long id;

    @NotNull(message = "Rating est obligatoire")
    @Min(value = 1, message = "La note doit être comprise entre 1 et 5")
    @Max(value = 5, message = "La note doit être comprise entre 1 et 5")
    private Integer rating;

    @NotBlank(message = "La description ne peut pas être vide")
    private String reviewDescription;

    private LocalDateTime date;
    private Long bookId;
    private Long userId;
    private String userEmail; // optionnel, pour affichage
}