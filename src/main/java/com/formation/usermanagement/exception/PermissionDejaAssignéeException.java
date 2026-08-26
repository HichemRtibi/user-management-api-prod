package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissionDejaAssignéeException extends RuntimeException {

    public PermissionDejaAssignéeException(String email, String permissionName) {
        super("L'utilisateur " + email + " a déjà la permission " + permissionName);
    }

    public PermissionDejaAssignéeException(String message) {
        super(message);
    }
}