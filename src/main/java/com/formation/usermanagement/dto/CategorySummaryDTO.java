package com.formation.usermanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ================================================================
 * DTO RÉSUMÉ POUR UNE CATÉGORIE (Version légère)
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce DTO est une version légère de CategoryResponseDTO.
 * Il est utilisé pour les relations (ex: dans ProductResponseDTO).
 *
 * 📋 EXEMPLE DE RÉPONSE JSON :
 * {
 *   "id": 1,
 *   "name": "Électronique"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Résumé d'une catégorie (version légère)")
public class CategorySummaryDTO {

    @Schema(description = "Identifiant de la catégorie",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nom de la catégorie",
            example = "Électronique")
    private String name;

    /**
     * ================================================================
     * DTO POUR LA CRÉATION/MODIFICATION D'UNE CATÉGORIE
     * ================================================================
     *
     * 📖 EXPLICATION :
     *
     * Ce DTO est utilisé pour créer ou modifier une catégorie.
     * Il contient uniquement les champs nécessaires à la création/modification.
     *
     * 📋 EXEMPLE DE REQUÊTE JSON :
     * {
     *   "name": "Électronique",
     *   "description": "Appareils électroniques"
     * }
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Requête de création/modification d'une catégorie")
    public static class CategoryRequestDTO {

        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
        @Schema(description = "Nom de la catégorie",
                example = "Électronique",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Size(max = 200, message = "La description ne peut pas dépasser 200 caractères")
        @Schema(description = "Description de la catégorie",
                example = "Appareils électroniques")
        private String description;
    }
}