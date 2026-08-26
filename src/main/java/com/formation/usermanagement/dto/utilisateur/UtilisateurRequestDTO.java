package com.formation.usermanagement.dto.utilisateur;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        description = "Requête de création/modification d'un utilisateur",
        example = """
        {
          "prenom": "Jean",
          "nom": "Dupont",
          "email": "jean.dupont@email.com",
          "motDePasse": "Password123@"
        }
        """
)
public class UtilisateurRequestDTO {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres")
    @Schema(
            description = "Prénom de l'utilisateur",
            example = "Jean",
            minLength = 2,
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres")
    @Schema(
            description = "Nom de l'utilisateur",
            example = "Dupont",
            minLength = 2,
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @Schema(
            description = "Email de l'utilisateur (utilisé pour la connexion)",
            example = "jean.dupont@email.com",
            format = "email",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    @Schema(
            description = "Mot de passe (8 caractères min, une majuscule, une minuscule, un chiffre, un caractère spécial)",
            example = "Password123@",
            minLength = 8,
            pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String motDePasse;
}