package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UN UTILISATEUR EST DANS UN ÉTAT INVALIDE
 *
 * Utilisée dans :
 * - desactiverUtilisateur() : si déjà désactivé
 * - activerUtilisateur() : si déjà activé
 * - verrouillerUtilisateur() : si déjà verrouillé
 * - expirerUtilisateur() : si déjà expiré
 *
 * ⚠️ @ResponseStatus(HttpStatus.BAD_REQUEST) : Retourne un code HTTP 400
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UtilisateurEtatInvalideException extends RuntimeException {

    /**
     * Constructeur avec message personnalisé
     */
    public UtilisateurEtatInvalideException(String operation, String email) {
        super("L'utilisateur " + email + " ne peut pas être " + operation + " (état invalide)");
    }

    /**
     * Constructeur avec message personnalisé
     */
    public UtilisateurEtatInvalideException(String message) {
        super(message);
    }
}