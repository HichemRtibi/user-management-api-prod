package com.formation.usermanagement.exception;

import com.formation.usermanagement.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ============================================================
 * GESTIONNAIRE CENTRALISÉ DES EXCEPTIONS (Version améliorée)
 * ============================================================
 *
 * 🎯 OBJECTIF : Uniformiser toutes les réponses d'erreur
 *
 * 📋 STRUCTURE DE LA RÉPONSE D'ERREUR :
 * {
 *   "success": false,
 *   "message": "Erreur de validation",
 *   "timestamp": "2026-08-22T10:00:00",
 *   "data": null,
 *   "errors": {
 *     "email": "L'email est obligatoire"
 *   }
 * }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ============================================================
    // 1. EXCEPTIONS MÉTIER
    // ============================================================

    @ExceptionHandler(UtilisateurNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUtilisateurNotFound(UtilisateurNotFoundException ex) {
        log.warn("Utilisateur non trouvé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailDejaExistantException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleEmailDejaExistant(EmailDejaExistantException ex) {
        log.warn("Email déjà existant : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRoleNotFound(RoleNotFoundException ex) {
        log.warn("Rôle non trouvé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleDejaExistantException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRoleDejaExistant(RoleDejaExistantException ex) {
        log.warn("Rôle déjà existant : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoleUtiliseException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRoleUtilise(RoleUtiliseException ex) {
        log.warn("Rôle utilisé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePermissionNotFound(PermissionNotFoundException ex) {
        log.warn("Permission non trouvée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PermissionDejaExistantException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePermissionDejaExistant(PermissionDejaExistantException ex) {
        log.warn("Permission déjà existante : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionUtiliseException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePermissionUtilise(PermissionUtiliseException ex) {
        log.warn("Permission utilisée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionDejaAssignéeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePermissionDejaAssignée(PermissionDejaAssignéeException ex) {
        log.warn("Permission déjà assignée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionNonAssignéeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handlePermissionNonAssignée(PermissionNonAssignéeException ex) {
        log.warn("Permission non assignée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UtilisateurEtatInvalideException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUtilisateurEtatInvalide(UtilisateurEtatInvalideException ex) {
        log.warn("État invalide : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleDejaAssignéException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRoleDejaAssigné(RoleDejaAssignéException ex) {
        log.warn("Rôle déjà assigné : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ============================================================
    // 2. EXCEPTIONS DE VALIDATION
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.warn("Erreur de validation : {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation échouée : " +
                errors.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.joining(", "));

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error(message, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleConstraintViolation(
            ConstraintViolationException ex) {
        log.warn("Contrainte violée : {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation échouée : " +
                errors.values().stream().collect(Collectors.joining(", "));

        ApiResponseDTO<Map<String, String>> response = ApiResponseDTO.error(message, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ============================================================
    // 3. EXCEPTIONS GÉNÉRIQUES
    // ============================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        return buildErrorResponse("Une erreur interne est survenue", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleException(Exception ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        return buildErrorResponse("Une erreur interne est survenue", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ============================================================
    // 4. MÉTHODE UTILITAIRE
    // ============================================================

    private ResponseEntity<ApiResponseDTO<Void>> buildErrorResponse(String message, HttpStatus status) {
        ApiResponseDTO<Void> response = ApiResponseDTO.error(message);
        return ResponseEntity.status(status).body(response);
    }
}