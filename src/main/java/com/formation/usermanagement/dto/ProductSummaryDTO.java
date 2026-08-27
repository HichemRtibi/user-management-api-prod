package com.formation.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ================================================================
 * DTO RÉSUMÉ POUR UN PRODUIT (Version légère)
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce DTO est une version légère de ProductResponseDTO.
 * Il est utilisé pour les listes et les relations.
 *
 * 📋 EXEMPLE DE RÉPONSE JSON :
 * {
 *   "id": 1,
 *   "name": "iPhone 15",
 *   "price": 999.99,
 *   "quantity": 10,
 *   "imageUrl": "https://example.com/iphone.jpg"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Résumé d'un produit (version légère)")
public class ProductSummaryDTO {

    @Schema(description = "Identifiant du produit",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nom du produit",
            example = "iPhone 15")
    private String name;

    @Schema(description = "Prix du produit",
            example = "999.99")
    private BigDecimal price;

    @Schema(description = "Quantité en stock",
            example = "10")
    private Integer quantity;

    @Schema(description = "URL de l'image",
            example = "https://example.com/iphone.jpg")
    private String imageUrl;

    @Schema(description = "ID de la catégorie",
            example = "1")
    private Long categoryId;

    @Schema(description = "Nom de la catégorie",
            example = "Électronique")
    private String categoryName;
}