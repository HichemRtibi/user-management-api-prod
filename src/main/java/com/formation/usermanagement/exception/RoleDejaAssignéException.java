package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION LANCÉE QUAND UN RÔLE EST DÉJÀ ASSIGNÉ À UN UTILISATEUR
 *
 * Utilisée dans :
 * - assignerRole(Long userId, String roleName)
 *
 * ⚠️ @ResponseStatus(HttpStatus.CONFLICT) : Retourne un code HTTP 409
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RoleDejaAssignéException extends RuntimeException {

    /**
     * Constructeur avec l'email et le nom du rôle
     */
    public RoleDejaAssignéException(String email, String roleName) {
        super("L'utilisateur " + email + " a déjà le rôle " + roleName);
    }

    /**
     * Constructeur avec message personnalisé
     */
    public RoleDejaAssignéException(String message) {
        super(message);
    }
}