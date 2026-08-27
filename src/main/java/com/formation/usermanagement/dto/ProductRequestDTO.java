package com.formation.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ================================================================
 * DTO POUR LA CRÉATION/MODIFICATION D'UN PRODUIT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce DTO est utilisé pour créer ou modifier un produit.
 * Il contient les champs nécessaires à la création/modification.
 *
 * 📋 EXEMPLE DE REQUÊTE JSON :
 * {
 *   "name": "iPhone 15",
 *   "description": "Le dernier iPhone avec puce A16",
 *   "price": 999.99,
 *   "quantity": 10,
 *   "imageUrl": "https://example.com/iphone.jpg",
 *   "categoryId": 1
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création/modification d'un produit")
public class ProductRequestDTO {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Schema(description = "Nom du produit",
            example = "iPhone 15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Schema(description = "Description du produit",
            example = "Le dernier iPhone avec puce A16")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Schema(description = "Prix du produit",
            example = "999.99",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    @Schema(description = "Quantité en stock",
            example = "10",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "URL de l'image",
            example = "https://example.com/iphone.jpg")
    private String imageUrl;

    @NotNull(message = "La catégorie est obligatoire")
    @Schema(description = "ID de la catégorie",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;
}