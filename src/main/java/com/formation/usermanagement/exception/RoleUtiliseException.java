// RoleUtiliseException.java
package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoleUtiliseException extends RuntimeException {
    public RoleUtiliseException(String name, long count) {
        super("Le rôle " + name + " est utilisé par " + count + " utilisateur(s) et ne peut pas être supprimé");
    }
}