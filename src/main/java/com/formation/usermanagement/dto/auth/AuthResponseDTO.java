package com.formation.usermanagement.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO POUR LES RÉPONSES D'AUTHENTIFICATION (Succès ou Erreur)
 *
 * Utilisé pour uniformiser les réponses de l'API d'authentification.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    /**
     * Indique si l'opération a réussi
     */
    private boolean success;

    /**
     * Message d'information ou d'erreur
     */
    private String message;

    /**
     * Timestamp de la réponse
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Les données de réponse (token, utilisateur, etc.)
     * Peut être null en cas d'erreur
     */
    private Object data;

    /**
     * Constructeur pour une réponse de succès
     */
    public static AuthResponseDTO success(String message, Object data) {
        return AuthResponseDTO.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * Constructeur pour une réponse d'erreur
     */
    public static AuthResponseDTO error(String message) {
        return AuthResponseDTO.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
    }
}