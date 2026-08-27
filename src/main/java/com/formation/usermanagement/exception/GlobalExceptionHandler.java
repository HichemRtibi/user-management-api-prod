package com.formation.usermanagement.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ============================================================
    // EXCEPTIONS EXISTANTES
    // ============================================================

    @ExceptionHandler(UtilisateurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUtilisateurNotFound(UtilisateurNotFoundException ex) {
        log.warn("Utilisateur non trouvé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailDejaExistantException.class)
    public ResponseEntity<ErrorResponse> handleEmailDejaExistant(EmailDejaExistantException ex) {
        log.warn("Email déjà existant : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ============================================================
    // ✅ AJOUTER CES HANDLERS POUR L'AUTHENTIFICATION
    // ============================================================

    /**
     * Gère l'exception UsernameNotFoundException (email non trouvé)
     * → 401 Unauthorized
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Utilisateur non trouvé : {}", ex.getMessage());
        return buildErrorResponse("Identifiants incorrects", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère l'exception BadCredentialsException (mauvais mot de passe)
     * → 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Identifiants incorrects : {}", ex.getMessage());
        return buildErrorResponse("Identifiants incorrects", HttpStatus.UNAUTHORIZED);
    }

    // ============================================================
    // AUTRES EXCEPTIONS
    // ============================================================

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        log.warn("Rôle non trouvé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RoleDejaExistantException.class)
    public ResponseEntity<ErrorResponse> handleRoleDejaExistant(RoleDejaExistantException ex) {
        log.warn("Rôle déjà existant : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RoleUtiliseException.class)
    public ResponseEntity<ErrorResponse> handleRoleUtilise(RoleUtiliseException ex) {
        log.warn("Rôle utilisé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(PermissionNotFoundException ex) {
        log.warn("Permission non trouvée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PermissionDejaExistantException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDejaExistant(PermissionDejaExistantException ex) {
        log.warn("Permission déjà existante : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionUtiliseException.class)
    public ResponseEntity<ErrorResponse> handlePermissionUtilise(PermissionUtiliseException ex) {
        log.warn("Permission utilisée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionDejaAssignéeException.class)
    public ResponseEntity<ErrorResponse> handlePermissionDejaAssignée(PermissionDejaAssignéeException ex) {
        log.warn("Permission déjà assignée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PermissionNonAssignéeException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNonAssignée(PermissionNonAssignéeException ex) {
        log.warn("Permission non assignée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UtilisateurEtatInvalideException.class)
    public ResponseEntity<ErrorResponse> handleUtilisateurEtatInvalide(UtilisateurEtatInvalideException ex) {
        log.warn("État invalide : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoleDejaAssignéException.class)
    public ResponseEntity<ErrorResponse> handleRoleDejaAssigné(RoleDejaAssignéException ex) {
        log.warn("Rôle déjà assigné : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ============================================================
    // NOUVELLES EXCEPTIONS CATEGORY
    // ============================================================

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        log.warn("Catégorie non trouvée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryDejaExistantException.class)
    public ResponseEntity<ErrorResponse> handleCategoryDejaExistant(CategoryDejaExistantException ex) {
        log.warn("Catégorie déjà existante : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CategoryUtiliseException.class)
    public ResponseEntity<ErrorResponse> handleCategoryUtilise(CategoryUtiliseException ex) {
        log.warn("Catégorie utilisée : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ============================================================
    // NOUVELLES EXCEPTIONS PRODUCT
    // ============================================================

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Produit non trouvé : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StockInsuffisantException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuffisant(StockInsuffisantException ex) {
        log.warn("Stock insuffisant : {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // EXCEPTIONS DE VALIDATION
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Erreur de validation : {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));

        return buildErrorResponse("Validation échouée : " + message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Contrainte violée : {}", ex.getMessage());

        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        return buildErrorResponse("Validation échouée : " + message, HttpStatus.BAD_REQUEST);
    }

    // ============================================================
    // EXCEPTIONS GÉNÉRIQUES
    // ============================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        return buildErrorResponse("Une erreur interne est survenue", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        return buildErrorResponse("Une erreur interne est survenue", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ============================================================
    // MÉTHODE UTILITAIRE
    // ============================================================

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, status);
    }

    // ============================================================
    // CLASSE INTERNE
    // ============================================================

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private LocalDateTime timestamp;
    }
}