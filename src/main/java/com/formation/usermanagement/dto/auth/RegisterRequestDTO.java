package com.formation.usermanagement.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO POUR L'INSCRIPTION D'UN NOUVEL UTILISATEUR
 *
 * Utilisé pour créer un nouveau compte.
 *
 * Exemple de requête JSON :
 * {
 *   "prenom": "Jean",
 *   "nom": "Dupont",
 *   "email": "jean.dupont@email.com",
 *   "motDePasse": "Password123@"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    /**
     * Prénom de l'utilisateur
     */
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres")
    private String prenom;

    /**
     * Nom de l'utilisateur
     */
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres")
    private String nom;

    /**
     * Email de l'utilisateur
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;

    /**
     * Mot de passe de l'utilisateur
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String motDePasse;

    /**
     * Confirmation du mot de passe
     */
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmationMotDePasse;
}