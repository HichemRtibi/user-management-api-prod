package com.formation.usermanagement.dto.permision;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO POUR LA PERMISSION
 *
 * Utilisé pour retourner les informations d'une permission.
 *
 * Exemple de réponse JSON :
 * {
 *   "id": 1,
 *   "category": "USER",
 *   "name": "USER_READ",
 *   "description": "Consulter les utilisateurs"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Réponse d'une permission",
        example = """
        {
          "id": 1,
          "category": "USER",
          "name": "USER_READ",
          "description": "Permet de consulter les utilisateurs"
        }
        """
)
public class PermissionDTO {
    @Schema(
            description = "Identifiant technique de la permission",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;
    @Schema(
            description = "Catégorie de la permission",
            example = "USER"
    )
    private String category;

    @Schema(
            description = "Nom complet de la permission",
            example = "USER_READ"
    )
    private String name;
    @Schema(
            description = "Description lisible de la permission",
            example = "Permet de consulter la liste des utilisateurs"
    )
    private String description;
}