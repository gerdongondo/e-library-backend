package com.luv2code.springbootlibrary.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ========== EXISTANT (inchangé) ==========
    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(
            UserAlreadyExistsException ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", "409");
        response.put("message", "L'utilisateur existe déjà");

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // ========== GESTIONNAIRES POUR REVIEW ET AUTRES ==========

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(
            EntityNotFoundException ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", "404");
        response.put("message", "Ressource non trouvée");

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Map<String, String>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", "409");
        response.put("message", "Conflit avec l'état actuel");

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", "403");
        response.put("message", "Vous n'êtes pas autorisé à effectuer cette action");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ========== NOUVEAU : Gestion des SecurityException pour les PDF ==========
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Map<String, String>> handleSecurityException(
            SecurityException ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        response.put("status", "403");
        response.put("message", "Accès interdit");

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, Object> response = new HashMap<>();
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        response.put("error", "Validation échouée");
        response.put("status", "400");
        response.put("message", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, String>> handleGenericException(
            Exception ex, WebRequest request) {

        Map<String, String> response = new HashMap<>();
        response.put("error", "Erreur interne du serveur");
        response.put("status", "500");
        response.put("message", ex.getMessage() != null ? ex.getMessage() : "Une erreur inattendue s'est produite");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}