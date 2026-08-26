package com.formation.usermanagement.dto.auth;

import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO POUR LA RÉPONSE DE CONNEXION
 *
 * Utilisé pour retourner le token JWT et les informations
 * de l'utilisateur après une authentification réussie.
 *
 * Exemple de réponse JSON :
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "type": "Bearer",
 *   "utilisateur": {
 *     "id": 1,
 *     "prenom": "Jean",
 *     "nom": "Dupont",
 *     "email": "jean.dupont@email.com",
 *     "roles": ["ROLE_ADMIN"],
 *     "permissions": ["USER_READ", "USER_WRITE", "USER_DELETE"]
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    /**
     * Token JWT pour l'authentification
     */
    private String token;

    /**
     * Type de token (généralement "Bearer")
     */
    @Builder.Default
    private String type = "Bearer";

    /**
     * Informations de l'utilisateur authentifié
     */
    private UtilisateurResponseDTO utilisateur;

    /**
     * Constructeur simplifié pour créer rapidement une réponse
     *
     * @param token Le token JWT
     * @param utilisateur Les informations de l'utilisateur
     */
    public LoginResponseDTO(String token, UtilisateurResponseDTO utilisateur) {
        this.token = token;
        this.type = "Bearer";
        this.utilisateur = utilisateur;
    }
}