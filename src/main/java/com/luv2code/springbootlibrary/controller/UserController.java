package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.dto.UpdateProfileRequest;
import com.luv2code.springbootlibrary.dto.UpdateProfileResponse;
import com.luv2code.springbootlibrary.dto.UserProfileResponse;
import com.luv2code.springbootlibrary.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")  // ⚠️ Vérifiez le chemin
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        System.out.println("=== UserController.getMyProfile() appelé ===");
        try {
            UserProfileResponse profile = userService.getCurrentUserProfile();
            System.out.println("Profile récupéré: " + profile.getEmail());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.out.println("ERROR dans getMyProfile: " + e.getMessage());
            e.printStackTrace();
            throw e; // Laisser Spring gérer l'exception
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UpdateProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        log.info("=== UserController.updateMyProfile() appelé ===");
        log.info("Données reçues: firstName={}, lastName={}",
                request.getFirstName(), request.getLastName());

        try {
            UpdateProfileResponse response = userService.updateCurrentUserProfile(request);
            log.info("Profil mis à jour pour l'utilisateur: {}", response.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("ERROR dans updateMyProfile: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @GetMapping("/me-simple")
    public ResponseEntity<?> getMyProfileSimple() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return ResponseEntity.status(401).body("Non authentifié");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", auth.isAuthenticated());
        response.put("name", auth.getName());
        response.put("principal", auth.getPrincipal().getClass().getSimpleName());
        response.put("authorities", auth.getAuthorities());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint";
    }

}