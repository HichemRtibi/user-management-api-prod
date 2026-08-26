package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UN UTILISATEUR N'EST PAS TROUVÉ
 *
 * Utilisée dans :
 * - getUtilisateur(Long id)
 * - updateUtilisateur(Long id, ...)
 * - supprimerUtilisateur(Long id)
 * - DesactiverUtilisateur(Long id)
 * - etc.
 *
 * ⚠️ @ResponseStatus(HttpStatus.NOT_FOUND) : Retourne un code HTTP 404
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UtilisateurNotFoundException extends RuntimeException {

    /**
     * Constructeur avec l'ID de l'utilisateur
     */
    public UtilisateurNotFoundException(Long id) {
        super("Utilisateur avec l'ID " + id + " non trouvé");
    }

    /**
     * Constructeur avec l'email de l'utilisateur
     */
    public UtilisateurNotFoundException(String email) {
        super("Utilisateur avec l'email " + email + " non trouvé");
    }

    /**
     * Constructeur avec message personnalisé
     */
    public UtilisateurNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}