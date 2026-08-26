package com.formation.usermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================
 * API RESPONSE DTO - WRAPPER STANDARD POUR TOUTES LES RÉPONSES
 * ============================================================
 *
 * 🎯 OBJECTIF : Uniformiser toutes les réponses de l'API
 *
 * 📋 STRUCTURE DE LA RÉPONSE :
 * {
 *   "success": true,
 *   "message": "Opération réussie",
 *   "timestamp": "2026-08-22T10:00:00",
 *   "data": { ... },         // Les données (peut être null)
 *   "errors": null           // Les erreurs (pour les validations)
 * }
 *
 * 📋 STRUCTURE EN CAS D'ERREUR :
 * {
 *   "success": false,
 *   "message": "Validation échouée",
 *   "timestamp": "2026-08-22T10:00:00",
 *   "data": null,
 *   "errors": {
 *     "email": "L'email est obligatoire",
 *     "motDePasse": "Le mot de passe est obligatoire"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {

    /**
     * Indique si l'opération a réussi
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Message d'information
     */
    private String message;

    /**
     * Timestamp de la réponse
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Les données de la réponse
     */
    private T data;

    /**
     * Les erreurs (pour les validations)
     */
    private Object errors;

    // ============================================================
    // MÉTHODES STATIQUES POUR LA CRÉATION
    // ============================================================

    /**
     * Crée une réponse de succès avec des données
     */
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message("Opération réussie")
                .data(data)
                .build();
    }

    /**
     * Crée une réponse de succès avec un message personnalisé
     */
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Crée une réponse de succès sans données (ex: suppression)
     */
    public static <T> ApiResponseDTO<T> success(String message) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Crée une réponse d'erreur
     */
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * Crée une réponse d'erreur avec des détails
     */
    public static <T> ApiResponseDTO<T> error(String message, Object errors) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}