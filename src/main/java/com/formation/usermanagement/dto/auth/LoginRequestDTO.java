package com.formation.usermanagement.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO POUR LA REQUÊTE DE CONNEXION
 *
 * Utilisé pour authentifier un utilisateur.
 *
 * Exemple de requête JSON :
 * {
 *   "email": "jean.dupont@email.com",
 *   "motDePasse": "Password123@"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /**
     * Email de l'utilisateur
     *
     * Validation :
     * - @NotBlank : Ne peut pas être vide
     * - @Email : Doit être un email valide
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Mot de passe de l'utilisateur
     *
     * Validation :
     * - @NotBlank : Ne peut pas être vide
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}