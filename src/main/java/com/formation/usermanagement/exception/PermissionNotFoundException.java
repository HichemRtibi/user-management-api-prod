package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UNE PERMISSION N'EST PAS TROUVÉE
 *
 * Utilisée dans :
 * - ajouterPermission(Long userId, String permissionName)
 * - retirerPermission(Long userId, String permissionName)
 *
 * ⚠️ @ResponseStatus(HttpStatus.NOT_FOUND) : Retourne un code HTTP 404
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends RuntimeException {

    /**
     * Constructeur avec le nom de la permission
     */
    public PermissionNotFoundException(String permissionName) {
        super("Permission " + permissionName + " non trouvée en base de données");
    }

    /**
     * Constructeur avec message personnalisé
     */
    public PermissionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}