package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UN RÔLE N'EST PAS TROUVÉ
 *
 * Utilisée dans :
 * - assignerRole(Long userId, String roleName)
 * - retirerRole(Long userId, String roleName)
 *
 * ⚠️ @ResponseStatus(HttpStatus.NOT_FOUND) : Retourne un code HTTP 404
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RoleNotFoundException extends RuntimeException {

    /**
     * Constructeur avec le nom du rôle
     */
    public RoleNotFoundException(String roleName) {
        super("Rôle " + roleName + " non trouvé en base de données");
    }

    /**
     * Constructeur avec message personnalisé
     */
    public RoleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}