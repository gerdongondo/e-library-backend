package com.luv2code.springbootlibrary.config;

import com.luv2code.springbootlibrary.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("=== JWT FILTER EXECUTING ===");
        log.info("URL: {}", request.getRequestURI());

        String headerAuth = request.getHeader(header);
        log.info("Authorization header: {}", headerAuth);
        log.info("Recherche du prefix: '{} '", tokenPrefix); // Montre ce qu'il cherche

        // === CORRECTION ICI ===
        if (headerAuth != null && !headerAuth.trim().isEmpty()) {
            String token = null;

            // Vérifier plusieurs formats possibles
            if (headerAuth.startsWith(tokenPrefix + " ")) {
                // Format attendu: "Bearer eyJhbGciOiJ..."
                token = headerAuth.substring(tokenPrefix.length() + 1);
                log.info("✅ Token trouvé avec prefix '{} '", tokenPrefix);
            }
            else if (headerAuth.startsWith("Bearer ")) {
                // Fallback: "Bearer " (en dur)
                token = headerAuth.substring(7);
                log.info("✅ Token trouvé avec 'Bearer ' (fallback)");
            }
            else if (headerAuth.startsWith(tokenPrefix)) {
                // Cas où il n'y a pas d'espace: "BearereyJhbGciOiJ..."
                token = headerAuth.substring(tokenPrefix.length());
                log.info("✅ Token trouvé avec '{}' sans espace", tokenPrefix);
            }
            else {
                // Aucun prefix, prendre tout le header
                token = headerAuth.trim();
                log.info("✅ Token pris SANS prefix (format brut)");
            }

            if (token != null && !token.isEmpty()) {
                log.info("Token extrait (premiers 30): {}...",
                        token.substring(0, Math.min(30, token.length())));

                try {
                    String username = jwtUtils.extractUsername(token);
                    log.info("Username extrait du token: {}", username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        log.info("Chargement UserDetails pour: {}", username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtUtils.validateToken(token, userDetails)) {
                            log.info("Token VALIDE pour: {}", username);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.info("✅ Authentication définie pour: {}", username);
                        } else {
                            log.warn("Token INVALIDE pour: {}", username);
                        }
                    }
                } catch (Exception e) {
                    log.error("Erreur dans JWT filter: {}", e.getMessage());
                    log.error("Exception type: {}", e.getClass().getName());
                    // Ne pas throw, continuer la chaine
                }
            } else {
                log.warn("Token null ou vide après extraction");
            }
        } else {
            log.info("Pas de token JWT trouvé dans la requête");
        }
        // === FIN CORRECTION ===

        filterChain.doFilter(request, response);
    }
}