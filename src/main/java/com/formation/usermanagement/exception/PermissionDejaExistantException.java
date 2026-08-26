package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PermissionDejaExistantException extends RuntimeException {
    public PermissionDejaExistantException(String name) {
        super("La permission " + name + " existe déjà");
    }
    public PermissionDejaExistantException(String category, String name) {
        super("La permission " + category + "_" + name + " existe déjà");
    }
}