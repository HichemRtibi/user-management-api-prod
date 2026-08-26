package com.formation.usermanagement.dto.role;


import com.formation.usermanagement.dto.permision.PermissionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Réponse d'un rôle avec ses permissions",
        example = """
        {
          "id": 1,
          "name": "ROLE_ADMIN",
          "description": "Administrateur système",
          "permissions": [
            { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." }
          ],
          "createdAt": "2026-08-25T10:00:00",
          "updatedAt": "2026-08-25T10:30:00"
        }
        """
)
public class RoleDTO {

    @Schema(
            description = "Identifiant du rôle",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Nom du rôle",
            example = "ROLE_ADMIN"
    )
    private String name;

    @Schema(
            description = "Description du rôle",
            example = "Administrateur système"
    )
    private String description;

    @Schema(
            description = "Liste des permissions du rôle"
    )
    private Set<PermissionDTO> permissions;

    @Schema(
            description = "Date de création",
            example = "2026-08-25T10:00:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date de dernière modification",
            example = "2026-08-25T10:30:00",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;
}