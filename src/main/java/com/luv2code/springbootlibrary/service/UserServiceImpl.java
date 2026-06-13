package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.UpdateProfileRequest;
import com.luv2code.springbootlibrary.dto.UpdateProfileResponse;
import com.luv2code.springbootlibrary.dto.UserProfileResponse;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        // Remplacer System.out.println() par log.debug() pour être plus professionnel
        log.debug("\n=== [DEBUG] UserServiceImpl.getCurrentUserProfile() ===");

        // 1. Récupérer l'authentification
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("[DEBUG] Authentication: {}", authentication);
        if (authentication != null) {
            log.debug("[DEBUG] Is authenticated: {}", authentication.isAuthenticated());
            log.debug("[DEBUG] Name: {}", authentication.getName());
            log.debug("[DEBUG] Principal: {}", authentication.getPrincipal());
            log.debug("[DEBUG] Authorities: {}", authentication.getAuthorities());
        } else {
            log.debug("[DEBUG] Authentication is NULL!");
        }

        // 2. Vérifier que l'utilisateur est authentifié
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("[DEBUG] ERREUR: Utilisateur non authentifié");
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // 3. Extraire le username (email) depuis l'authentification
        String email = authentication.getName();
        log.debug("[DEBUG] Email extrait: {}", email);

        // 4. Récupérer l'utilisateur depuis la base de données
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[DEBUG] ERREUR: Utilisateur non trouvé avec email: {}", email);
                    return new RuntimeException("Utilisateur non trouvé");
                });

        // LOG 3
        log.debug("[DEBUG] Utilisateur trouvé en DB:");
        log.debug("[DEBUG]   ID: {}", user.getId());
        log.debug("[DEBUG]   Email: {}", user.getEmail());
        log.debug("[DEBUG]   Nom: {} {}", user.getFirstName(), user.getLastName());
        log.debug("[DEBUG]   Role: {}", user.getRole());

        // 5. Construire et retourner la réponse
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public UpdateProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        log.info("Mise à jour du profil utilisateur");

        // 1. Récupérer l'utilisateur actuellement connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.debug("[DEBUG] Authentication: {}", authentication);
        if (authentication != null) {
            log.debug("[DEBUG] Is authenticated: {}", authentication.isAuthenticated());
            log.debug("[DEBUG] Name: {}", authentication.getName());
            log.debug("[DEBUG] Principal: {}", authentication.getPrincipal());
            log.debug("[DEBUG] Authorities: {}", authentication.getAuthorities());
        } else {
            log.debug("[DEBUG] Authentication is NULL!");
        }

        // 2. Vérifier que l'utilisateur est authentifié
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("[DEBUG] ERREUR: Utilisateur non authentifié");
            throw new RuntimeException("Utilisateur non authentifié");
        }

        // 3. Extraire le username (email) depuis l'authentification
        String email = authentication.getName();
        log.debug("[DEBUG] Email extrait: {}", email);

        // 4. Récupérer l'utilisateur depuis la base de données
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[DEBUG] ERREUR: Utilisateur non trouvé avec email: {}", email);
                    return new RuntimeException("Utilisateur non trouvé");
                });

        log.debug("[DEBUG] Utilisateur avant mise à jour: ID={}, Email={}, Nom={} {}",
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());

        // 5. Mettre à jour uniquement les champs autorisés
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // 6. Sauvegarder les modifications
        User updatedUser = userRepository.save(user);

        log.debug("[DEBUG] Utilisateur après mise à jour: ID={}, Email={}, Nom={} {}",
                updatedUser.getId(), updatedUser.getEmail(),
                updatedUser.getFirstName(), updatedUser.getLastName());

        // 7. Retourner la réponse
        return new UpdateProfileResponse(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getRole().name(),
                "Profil mis à jour avec succès"
        );
    }
}