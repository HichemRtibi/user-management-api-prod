package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UN EMAIL EST DÉJÀ UTILISÉ
 *
 * Utilisée dans :
 * - creerUtilisateur(UtilisateurRequestDTO)
 * - updateUtilisateur(Long id, UtilisateurRequestDTO)
 *
 * ⚠️ @ResponseStatus(HttpStatus.CONFLICT) : Retourne un code HTTP 409
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class EmailDejaExistantException extends RuntimeException {

    /**
     * Constructeur avec l'email concerné
     */
    public EmailDejaExistantException(String email) {
        super("L'email " + email + " est déjà utilisé par un autre utilisateur");
    }

    /**
     * Constructeur avec message personnalisé
     */
    public EmailDejaExistantException(String message, Throwable cause) {
        super(message, cause);
    }
}