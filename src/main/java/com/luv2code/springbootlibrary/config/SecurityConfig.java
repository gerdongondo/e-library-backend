package com.luv2code.springbootlibrary.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Autowired
    @Lazy // ⚠️ AJOUT IMPORTANT
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private UserDetailsService userDetailsService;


    // PasswordEncoder déjà configuré
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationProvider pour utiliser notre UserDetailsService
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Configuration CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
                "https://react-library-frontend-orcin.vercel.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Configuration principale de sécurité
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF pour les API REST
                .csrf(csrf -> csrf.disable())

                // Configurer CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Gestion des sessions stateless (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll() // Accessible sans auth

                        // ⚠️ AJOUTER CES DEUX LIGNES ⚠️
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()

                        // 🔵 Catégories publiques
                        .requestMatchers("/api/categories/**").permitAll()

                        // 🔴 Gestion catégories admin uniquement
                        .requestMatchers("/api/admin/categories/**").hasRole("ADMIN")

                        // Endpoints GET publics pour les livres et reviews
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()

                        // NOUVEAU : Endpoints utilisateurs nécessitant authentification
                        .requestMatchers("/api/users/test").permitAll()
                        .requestMatchers("/api/users/**").authenticated()

                        // Nouveaux endpoints publics
                        .requestMatchers("/api/books/{bookId}/availability").permitAll()

                        // 🔓 NOUVEAU : recherche avancée publique
                        .requestMatchers(HttpMethod.GET, "/api/search/**").permitAll()

                        // Endpoints pour utilisateurs authentifiés
                        .requestMatchers("/api/borrow").authenticated()
                        .requestMatchers("/api/return/**").authenticated()
                        .requestMatchers("/api/users/me/**").authenticated()

                        // Endpoints pour image et pdf
                        .requestMatchers(HttpMethod.GET, "/api/books/*/image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/*/pdf").permitAll()


                        // Endpoints Admin uniquement
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Tout le reste nécessite authentification
                        .anyRequest().authenticated()
                )

                // Ajouter le filtre JWT avant le filtre d'authentification
                //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                // ⚠️ IMPORTANT : Positionnez le filtre AVANT AnonymousAuthenticationFilter
               .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }
}