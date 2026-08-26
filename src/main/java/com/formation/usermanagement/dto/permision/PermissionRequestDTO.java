package com.formation.usermanagement.dto.permision;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO POUR LA CRÉATION/MODIFICATION D'UNE PERMISSION
 *
 * Utilisé pour créer ou modifier une permission.
 *
 * Exemple de requête JSON :
 * {
 *   "category": "USER",
 *   "name": "USER_READ",
 *   "description": "Permet de consulter les utilisateurs"
 * }
 *
 * ⚠️ Règles de nommage :
 * - Format : {CATEGORIE}_{ACTION}
 * - Exemples : USER_READ, USER_WRITE, ROLE_ASSIGN
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDTO {

    /**
     * Catégorie de la permission
     * Exemples : "USER", "ROLE", "PERMISSION", "PRODUCT"
     *
     * Validation :
     * - @NotBlank : Ne peut pas être vide
     * - @Size(max = 50) : Maximum 50 caractères
     * - @Pattern : Uniquement des lettres majuscules et underscores
     */
    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "La catégorie ne doit contenir que des lettres majuscules et des underscores"
    )
    @Schema(
            description = "Catégorie de la permission",
            example = "USER",
            allowableValues = {"USER", "ROLE", "PERMISSION", "PRODUCT", "AUTH", "DASHBOARD"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String category;

    /**
     * Nom complet de la permission
     * Format : {CATEGORIE}_{ACTION}
     * Exemples : "USER_READ", "USER_WRITE", "ROLE_ASSIGN"
     *
     * Validation :
     * - @NotBlank : Ne peut pas être vide
     * - @Size(max = 50) : Maximum 50 caractères
     * - @Pattern : Uniquement des lettres majuscules et underscores
     */
    @NotBlank(message = "Le nom de la permission est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Le nom ne doit contenir que des lettres majuscules et des underscores"
    )
    @Schema(
            description = "Nom complet de la permission (format: CATEGORIE_ACTION)",
            example = "USER_READ",
            pattern = "^[A-Z_]+$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    /**
     * Description lisible de la permission
     * Exemple : "Permet de consulter la liste des utilisateurs"
     */
    @Size(max = 100, message = "La description ne peut pas dépasser 100 caractères")
    @Schema(
            description = "Description lisible de la permission (optionnelle)",
            example = "Permet de consulter la liste des utilisateurs",
            maxLength = 100
    )
    private String description;
}