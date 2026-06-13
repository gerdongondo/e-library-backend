package com.luv2code.springbootlibrary.dto;

import com.luv2code.springbootlibrary.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleUpdateRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    private String reason; // Optionnel: raison du changement
}