package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ================================================================
 * EXCEPTION LANCÉE QUAND UN PRODUIT N'EST PAS TROUVÉ
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette exception est levée lorsqu'un produit n'existe pas en base de données.
 *
 * 🔴 UTILISÉE DANS :
 * - ProductService.getProduct()
 * - ProductService.updateProduct()
 * - ProductService.deleteProduct()
 * - ProductService.updateStock()
 *
 * 📊 RÉPONSE HTTP : 404 Not Found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Produit avec l'ID " + id + " non trouvé");
    }

    public ProductNotFoundException(String message) {
        super("Produit " + message + " non trouvé");
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}