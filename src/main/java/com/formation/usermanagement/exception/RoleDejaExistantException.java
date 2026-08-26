// RoleDejaExistantException.java
package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoleDejaExistantException extends RuntimeException {
    public RoleDejaExistantException(String name) {
        super("Le rôle " + name + " existe déjà");
    }
    public RoleDejaExistantException(String message, Throwable cause) {
        super(message, cause);
    }
}