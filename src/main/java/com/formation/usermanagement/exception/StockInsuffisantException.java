package com.formation.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ================================================================
 * EXCEPTION LANCÉE QUAND LE STOCK EST INSUFFISANT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette exception est levée lorsqu'on tente de réduire le stock
 * en dessous de 0 ou d'acheter plus que le stock disponible.
 *
 * 🔴 UTILISÉE DANS :
 * - Product.reduceQuantity() (méthode utilitaire)
 *
 * 📊 RÉPONSE HTTP : 400 Bad Request
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockInsuffisantException extends RuntimeException {

    public StockInsuffisantException(Long productId, Integer requested, Integer available) {
        super("Stock insuffisant pour le produit ID " + productId +
                ". Demande: " + requested + ", Disponible: " + available);
    }

    public StockInsuffisantException(String message) {
        super(message);
    }

    public StockInsuffisantException(String message, Throwable cause) {
        super(message, cause);
    }
}