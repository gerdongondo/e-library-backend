package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dto.*;
import com.luv2code.springbootlibrary.entity.PasswordResetToken;
import com.luv2code.springbootlibrary.entity.Role;
import com.luv2code.springbootlibrary.entity.User;
import com.luv2code.springbootlibrary.exception.UserAlreadyExistsException;
import com.luv2code.springbootlibrary.repository.PasswordResetTokenRepository;
import com.luv2code.springbootlibrary.repository.UserRepository;
import com.luv2code.springbootlibrary.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.luv2code.springbootlibrary.exception.UserAlreadyExistsException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Value("${password.reset.token.expiration:15}")
    private int tokenExpirationMinutes;

    @Value("${password.reset.token.length:32}")
    private int tokenLength;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private HttpServletRequest httpServletRequest; // AJOUTER CE CHAMP





    // INSCRIPTION (EXISTANT - NE PAS MODIFIER)
    @Override
    public RegisterResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé !");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(Role.ROLE_STUDENT); // rôle par défaut

        // Sauvegarder en DB
        user = userRepository.save(user);

        // Préparer la réponse
        RegisterResponse response = new RegisterResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRole(user.getRole().name());

        return response;
    }

    // CONNEXION (NOUVELLE MÉTHODE)
    @Override
    public LoginResponse login(LoginRequest request) {

        try {
        // 1. Authentifier l'utilisateur avec Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Définir l'authentification dans le contexte
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Récupérer l'utilisateur depuis la base de données
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé après authentification"));

        // 4. Générer le token JWT
        String token = jwtUtils.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

            // CORRECTION : Utiliser HttpServletRequest injecté
            String ipAddress = getClientIpAddress();
            String userAgent = httpServletRequest.getHeader("User-Agent");

        auditLogService.logLogin(request.getEmail(), true, ipAddress, userAgent, null);

        // 5. Retourner la réponse
        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    } catch (Exception e) {
            // En cas d'échec
            String ipAddress = getClientIpAddress();
            String userAgent = httpServletRequest.getHeader("User-Agent");

            auditLogService.logLogin(request.getEmail(), false, ipAddress, userAgent, e.getMessage());
            throw e;
        }
    }

    // Méthode utilitaire pour récupérer l'adresse IP
    private String getClientIpAddress() {
        if (httpServletRequest == null) {
            return "0.0.0.0";
        }

        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }

    // ========== MÉTHODE 1 : FORGOT PASSWORD ==========

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        // Utiliser System.out.println() au lieu de log pour rester cohérent avec votre code existant
        System.out.println("\n=== [DEBUG] Demande de réinitialisation pour l'email: " + request.getEmail() + " ===");

        // 1. Vérifier si l'email existe dans la base
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        // 2. Message générique pour la sécurité (même si l'email n'existe pas)
        String message = "Si l'email existe, un lien de réinitialisation a été envoyé";

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("[DEBUG] Utilisateur trouvé: ID=" + user.getId() + ", Email=" + user.getEmail());

            try {
                // 3. Générer un token sécurisé
                String token = generateSecureToken();
                System.out.println("[DEBUG] Token généré pour " + user.getEmail() + ": " + token);

                // 4. Supprimer les anciens tokens pour cet utilisateur
                tokenRepository.findByUserId(user.getId()).ifPresent(oldToken -> {
                    tokenRepository.delete(oldToken);
                    System.out.println("[DEBUG] Ancien token supprimé pour l'utilisateur ID=" + user.getId());
                });

                // 5. Créer et sauvegarder le nouveau token
                PasswordResetToken resetToken = new PasswordResetToken(
                        token, user, tokenExpirationMinutes
                );
                tokenRepository.save(resetToken);

                System.out.println("[DEBUG] Token de réinitialisation créé pour " + user.getEmail() +
                        " (expire dans " + tokenExpirationMinutes + " minutes)");

                // 6. Simuler l'envoi d'email (pour démonstration)
                simulateEmailSending(user.getEmail(), token);

            } catch (Exception e) {
                System.out.println("[ERROR] Erreur lors de la génération du token pour " +
                        request.getEmail() + ": " + e.getMessage());
                // On ne révèle pas l'erreur à l'utilisateur pour des raisons de sécurité
            }
        } else {
            System.out.println("[WARN] Tentative de réinitialisation pour un email inexistant: " +
                    request.getEmail());
        }

        // 7. Retourner une réponse générique
        return new ForgotPasswordResponse(message, true);
    }

    // ========== MÉTHODE 2 : RESET PASSWORD ==========

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        System.out.println("\n=== [DEBUG] Tentative de réinitialisation avec token: " +
                maskToken(request.getToken()) + " ===");

        try {
            // 1. Rechercher le token
            PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                    .orElseThrow(() -> {
                        System.out.println("[ERROR] Token non trouvé: " + maskToken(request.getToken()));
                        return new RuntimeException("Token de réinitialisation invalide");
                    });

            System.out.println("[DEBUG] Token trouvé pour l'utilisateur ID=" + resetToken.getUser().getId());

            // 2. Vérifier si le token a déjà été utilisé
            if (resetToken.isUsed()) {
                System.out.println("[ERROR] Token déjà utilisé: " + maskToken(request.getToken()));
                throw new RuntimeException("Ce lien de réinitialisation a déjà été utilisé");
            }

            // 3. Vérifier si le token est expiré
            if (resetToken.isExpired()) {
                System.out.println("[ERROR] Token expiré: " + maskToken(request.getToken()));
                throw new RuntimeException("Le lien de réinitialisation a expiré");
            }

            // 4. Vérifier la force du nouveau mot de passe
            if (request.getNewPassword().length() < 6) {
                throw new RuntimeException("Le mot de passe doit contenir au moins 6 caractères");
            }

            // 5. Récupérer l'utilisateur
            User user = resetToken.getUser();
            System.out.println("[DEBUG] Mise à jour du mot de passe pour l'utilisateur ID=" +
                    user.getId() + ", Email=" + user.getEmail());

            // 6. Encoder le nouveau mot de passe avec BCrypt
            String encodedPassword = passwordEncoder.encode(request.getNewPassword());
            user.setPassword(encodedPassword);

            // 7. Sauvegarder l'utilisateur
            userRepository.save(user);
            System.out.println("[INFO] Mot de passe mis à jour pour l'utilisateur ID=" + user.getId());

            // 8. Marquer le token comme utilisé
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            System.out.println("[DEBUG] Token marqué comme utilisé: " + maskToken(request.getToken()));

            // 9. Supprimer tous les tokens expirés (nettoyage)
            cleanExpiredTokens();

            // 10. Retourner une réponse positive
            return new ResetPasswordResponse(
                    "Mot de passe réinitialisé avec succès. Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.",
                    true
            );

        } catch (RuntimeException e) {
            System.out.println("[ERROR] Erreur lors de la réinitialisation: " + e.getMessage());
            return new ResetPasswordResponse(e.getMessage(), false);
        }
    }

    // ========== MÉTHODES UTILITAIRES PRIVÉES ==========

    /**
     * Génère un token sécurisé aléatoire
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[tokenLength];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Masque partiellement le token pour les logs (sécurité)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    /**
     * Simule l'envoi d'un email (pour démonstration académique)
     */
    private void simulateEmailSending(String email, String token) {
        // Dans un vrai projet, vous utiliseriez Spring Mail ici
        System.out.println("\n=== [SIMULATION EMAIL] ===");
        System.out.println("To: " + email);
        System.out.println("Subject: Réinitialisation de votre mot de passe");
        System.out.println("Message: Cliquez sur le lien pour réinitialiser votre mot de passe:");
        System.out.println("http://localhost:3000/reset-password?token=" + token);
        System.out.println("=== [FIN SIMULATION] ===\n");

        // Pour votre mémoire, vous pouvez expliquer:
        // - En production: utiliser Spring Boot Starter Mail
        // - Configurer un serveur SMTP (Gmail, SendGrid, etc.)
        // - Template HTML pour l'email
        // - Gestion des erreurs d'envoi
    }

    /**
     * Nettoie les tokens expirés de la base de données
     */
    private void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiryDateBefore(now);
        System.out.println("[DEBUG] Nettoyage des tokens expirés effectué");
    }



}