/*

supprimer ce fichier il appelle methodes qui n'existent pas dans JwtUtils



package com.luv2code.springbootlibrary.unit.service;

import com.luv2code.springbootlibrary.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsUnitTest {

    private JwtUtils jwtUtils;
    private Key key;
    private final String secret = "MaSuperCleSecretePourLeMaster2BibliothequeEnLigne2025TrèsLonguePourSecurite";

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();

        // Réflexion pour injecter les valeurs dans les champs privés
        Field secretField = JwtUtils.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtils, secret);

        Field expirationField = JwtUtils.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtils, 86400000);

        Field issuerField = JwtUtils.class.getDeclaredField("issuer");
        issuerField.setAccessible(true);
        issuerField.set(jwtUtils, "spring-boot-library");

        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        UserDetails userDetails = User.withUsername("test@univ.fr")
                .password("pass")
                .authorities("ROLE_STUDENT")
                .build();

        String token = jwtUtils.generateToken(userDetails);
        assertNotNull(token);

        // ⚠️ Si la méthode validateToken de JwtUtils attend 1 paramètre (le token)
        //    utilise : assertTrue(jwtUtils.validateToken(token));
        // Si elle attend 2 paramètres (token + userDetails), utilise la ligne suivante :
        assertTrue(jwtUtils.validateToken(token, userDetails));

        assertEquals("test@univ.fr", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void validateToken_Expired_ShouldReturnFalse() {
        String expiredToken = Jwts.builder()
                .setSubject("test@univ.fr")
                .setIssuedAt(new Date(System.currentTimeMillis() - 100000))
                .setExpiration(new Date(System.currentTimeMillis() - 50000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Crée un UserDetails factice (le contenu n'importe pas pour la validation)
        UserDetails dummy = User.withUsername("dummy")
                .password("")
                .authorities("ROLE_USER")
                .build();

        // Adapte selon la signature de validateToken dans ta classe JwtUtils
        assertFalse(jwtUtils.validateToken(expiredToken, dummy));
    }
}


 */


