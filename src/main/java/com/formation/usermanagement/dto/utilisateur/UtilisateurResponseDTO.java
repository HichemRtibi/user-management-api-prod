package com.formation.usermanagement.dto.utilisateur;

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
        description = "Réponse d'un utilisateur (toutes les informations)",
        example = """
        {
          "id": 1,
          "prenom": "Jean",
          "nom": "Dupont",
          "email": "jean.dupont@email.com",
          "enabled": true,
          "compteNonVerrouille": true,
          "compteNonExpire": true,
          "credentialsNonExpire": true,
          "roles": ["ROLE_USER"],
          "permissions": ["USER_READ"],
          "derniereConnexion": "2026-08-25T10:00:00",
          "createdAt": "2026-08-25T09:00:00",
          "updatedAt": "2026-08-25T10:30:00"
        }
        """
)
public class UtilisateurResponseDTO {

    @Schema(
            description = "Identifiant de l'utilisateur",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long id;

    @Schema(
            description = "Prénom de l'utilisateur",
            example = "Jean"
    )
    private String prenom;

    @Schema(
            description = "Nom de l'utilisateur",
            example = "Dupont"
    )
    private String nom;

    @Schema(
            description = "Email de l'utilisateur",
            example = "jean.dupont@email.com"
    )
    private String email;

    @Schema(
            description = "Compte activé ou désactivé",
            example = "true"
    )
    private boolean enabled;

    @Schema(
            description = "Compte verrouillé ou déverrouillé",
            example = "true"
    )
    private boolean compteNonVerrouille;

    @Schema(
            description = "Compte expiré ou non",
            example = "true"
    )
    private boolean compteNonExpire;

    @Schema(
            description = "Mot de passe expiré ou non",
            example = "true"
    )
    private boolean credentialsNonExpire;

    @Schema(
            description = "Liste des noms des rôles",
            example = "[\"ROLE_USER\"]"
    )
    private Set<String> roles;

    @Schema(
            description = "Liste des noms des permissions",
            example = "[\"USER_READ\"]"
    )
    private Set<String> permissions;

    @Schema(
            description = "Dernière date de connexion",
            example = "2026-08-25T10:00:00"
    )
    private LocalDateTime derniereConnexion;

    @Schema(
            description = "Date de création du compte",
            example = "2026-08-25T09:00:00",
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