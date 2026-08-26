package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PermissionNonAssignéeException extends RuntimeException {

    public PermissionNonAssignéeException(String email, String permissionName) {
        super("L'utilisateur " + email + " n'a pas la permission " + permissionName);
    }

    public PermissionNonAssignéeException(String message) {
        super(message);
    }
}