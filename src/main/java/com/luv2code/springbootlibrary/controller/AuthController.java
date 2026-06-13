package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.*;
import com.luv2code.springbootlibrary.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // INSCRIPTION (EXISTANT - NE PAS MODIFIER)
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // CONNEXION (NOUVEAU)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // DÉCONNEXION (NOUVEAU)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logoutUser() {
        // Pour JWT stateless, la déconnexion se fait côté client
        // Le client doit simplement supprimer le token

        Map<String, String> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        response.put("instruction", "Veuillez supprimer le token JWT côté client");

        return ResponseEntity.ok(response);
    }

    // TEST ENDPOINT (optionnel)
    @GetMapping("/test")
    public String test() {
        return "AuthController is working!";
    }


    // ========== NOUVEL ENDPOINT 1: FORGOT PASSWORD ==========

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        System.out.println("\n=== [DEBUG] POST /api/auth/forgot-password appelé pour l'email: " +
                maskEmail(request.getEmail()) + " ===");

        try {
            ForgotPasswordResponse response = authService.forgotPassword(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("[ERROR] Erreur dans forgot-password: " + e.getMessage());
            // Pour la sécurité, on retourne toujours un message générique
            return ResponseEntity.ok(new ForgotPasswordResponse(
                    "Si l'email existe, un lien de réinitialisation a été envoyé",
                    true
            ));
        }
    }

    // ========== NOUVEL ENDPOINT 2: RESET PASSWORD ==========

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        System.out.println("\n=== [DEBUG] POST /api/auth/reset-password appelé avec token: " +
                maskToken(request.getToken()) + " ===");

        try {
            ResetPasswordResponse response = authService.resetPassword(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Erreur dans reset-password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResetPasswordResponse(
                            "Erreur lors de la réinitialisation du mot de passe",
                            false
                    ));
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***";
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, Math.min(3, atIndex));
        return username + "***@" + email.substring(atIndex + 1);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}