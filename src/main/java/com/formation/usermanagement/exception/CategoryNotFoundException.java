package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ================================================================
 * EXCEPTION LANCÉE QUAND UNE CATÉGORIE N'EST PAS TROUVÉE
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette exception est levée lorsqu'une catégorie n'existe pas en base de données.
 *
 * 🔴 UTILISÉE DANS :
 * - CategoryService.getCategory()
 * - CategoryService.updateCategory()
 * - CategoryService.deleteCategory()
 * - ProductService.creerProduct() (vérification de la catégorie)
 *
 * 📊 RÉPONSE HTTP : 404 Not Found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Catégorie avec l'ID " + id + " non trouvée");
    }

    public CategoryNotFoundException(String message) {
        super("Catégorie " + message + " non trouvée");
    }

    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}