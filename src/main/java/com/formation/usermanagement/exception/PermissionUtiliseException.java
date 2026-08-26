package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissionUtiliseException extends RuntimeException {
    public PermissionUtiliseException(String name, long count) {
        super("La permission " + name + " est utilisée par " + count + " rôle(s) et ne peut pas être supprimée");
    }
}