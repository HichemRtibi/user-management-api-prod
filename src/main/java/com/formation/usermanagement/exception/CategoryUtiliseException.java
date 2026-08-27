package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ================================================================
 * EXCEPTION LANCÉE QUAND UNE CATÉGORIE EST UTILISÉE PAR DES PRODUITS
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette exception est levée lorsqu'on tente de supprimer une catégorie
 * qui contient encore des produits.
 *
 * 🔴 UTILISÉE DANS :
 * - CategoryService.deleteCategory()
 *
 * 📊 RÉPONSE HTTP : 409 Conflict
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class CategoryUtiliseException extends RuntimeException {

    public CategoryUtiliseException(String name, long productCount) {
        super("La catégorie '" + name + "' contient " + productCount + " produit(s) et ne peut pas être supprimée");
    }

    public CategoryUtiliseException(String message, Throwable cause) {
        super(message, cause);
    }
}