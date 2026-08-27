package com.formation.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * DTO POUR LA RÉPONSE D'UNE CATÉGORIE
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce DTO est utilisé pour retourner les informations d'une catégorie.
 * Il contient tous les champs que le client peut voir.
 *
 * 📋 EXEMPLE DE RÉPONSE JSON :
 * {
 *   "id": 1,
 *   "name": "Électronique",
 *   "description": "Appareils électroniques",
 *   "productCount": 5,
 *   "products": [...],
 *   "createdAt": "2026-08-27T10:00:00",
 *   "updatedAt": "2026-08-27T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse d'une catégorie")
public class CategoryResponseDTO {

    @Schema(description = "Identifiant de la catégorie",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nom de la catégorie",
            example = "Électronique")
    private String name;

    @Schema(description = "Description de la catégorie",
            example = "Appareils électroniques")
    private String description;

    @Schema(description = "Nombre de produits dans la catégorie",
            example = "5")
    private Integer productCount;

    @Schema(description = "Liste des produits (optionnel)")
    private List<ProductSummaryDTO> products;

    @Schema(description = "Date de création",
            example = "2026-08-27T10:00:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification",
            example = "2026-08-27T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}