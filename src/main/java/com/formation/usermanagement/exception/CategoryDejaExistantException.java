package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ================================================================
 * EXCEPTION LANCÉE QUAND UNE CATÉGORIE EXISTE DÉJÀ
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette exception est levée lorsqu'on tente de créer une catégorie
 * avec un nom déjà utilisé.
 *
 * 🔴 UTILISÉE DANS :
 * - CategoryService.creerCategory()
 * - CategoryService.updateCategory()
 *
 * 📊 RÉPONSE HTTP : 409 Conflict
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryDejaExistantException extends RuntimeException {

    public CategoryDejaExistantException(String name) {
        super("La catégorie '" + name + "' existe déjà");
    }

    public CategoryDejaExistantException(String message, Throwable cause) {
        super(message, cause);
    }
}