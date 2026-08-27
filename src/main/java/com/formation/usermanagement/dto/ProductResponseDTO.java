package com.formation.usermanagement.dto;

import com.formation.usermanagement.dto.CategorySummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ================================================================
 * DTO POUR LA RÉPONSE D'UN PRODUIT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce DTO est utilisé pour retourner les informations d'un produit.
 * Il contient tous les champs que le client peut voir.
 *
 * 📋 EXEMPLE DE RÉPONSE JSON :
 * {
 *   "id": 1,
 *   "name": "iPhone 15",
 *   "description": "Le dernier iPhone avec puce A16",
 *   "price": 999.99,
 *   "quantity": 10,
 *   "imageUrl": "https://example.com/iphone.jpg",
 *   "inStock": true,
 *   "category": {
 *     "id": 1,
 *     "name": "Électronique"
 *   },
 *   "createdAt": "2026-08-27T10:00:00",
 *   "updatedAt": "2026-08-27T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'un produit")
public class ProductResponseDTO {

    @Schema(description = "Identifiant du produit",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nom du produit",
            example = "iPhone 15")
    private String name;

    @Schema(description = "Description du produit",
            example = "Le dernier iPhone avec puce A16")
    private String description;

    @Schema(description = "Prix du produit",
            example = "999.99")
    private BigDecimal price;

    @Schema(description = "Quantité en stock",
            example = "10")
    private Integer quantity;

    @Schema(description = "URL de l'image",
            example = "https://example.com/iphone.jpg")
    private String imageUrl;

    @Schema(description = "Produit en stock",
            example = "true")
    private boolean inStock;

    @Schema(description = "Catégorie du produit")
    private CategorySummaryDTO category;

    @Schema(description = "Date de création",
            example = "2026-08-27T10:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification",
            example = "2026-08-27T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}