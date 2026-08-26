package com.formation.usermanagement.dto.role;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Requête de création/modification d'un rôle",
        example = """
        {
          "name": "ROLE_MANAGER",
          "description": "Manager avec des permissions limitées",
          "permissionIds": [1, 2, 3]
        }
        """
)
public class RoleRequestDTO {

    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(max = 50, message = "Le nom du rôle ne peut pas dépasser 50 caractères")
    @Schema(
            description = "Nom du rôle (doit commencer par ROLE_)",
            example = "ROLE_MANAGER",
            pattern = "^ROLE_[A-Z_]+$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Size(max = 100, message = "La description ne peut pas dépasser 100 caractères")
    @Schema(
            description = "Description lisible du rôle (optionnelle)",
            example = "Manager avec des permissions limitées",
            maxLength = 100
    )
    private String description;

    @Schema(
            description = "Liste des IDs des permissions à associer au rôle",
            example = "[1, 2, 3]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Set<Long> permissionIds;
}