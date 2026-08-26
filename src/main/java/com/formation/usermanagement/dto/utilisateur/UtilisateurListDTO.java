package com.formation.usermanagement.dto.utilisateur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO LÉGER POUR LA LISTE DES UTILISATEURS
 *
 * Version simplifiée de UtilisateurResponseDTO, utilisée
 * uniquement pour les listes paginées.
 *
 * ⚠️ Pourquoi un DTO séparé ?
 * - Optimisation des performances (moins de données transférées)
 * - Le frontend n'a pas besoin de tous les détails dans une liste
 * - Réduction de la charge réseau
 *
 * Différences avec UtilisateurResponseDTO :
 * - Pas de : compteNonVerrouille, compteNonExpire, credentialsNonExpire
 * - Pas de : derniereConnexion, updatedAt
 * - Permissions : non incluses (seulement les rôles)
 *
 * Exemple de réponse JSON pour une liste :
 * [
 *   {
 *     "id": 1,
 *     "prenom": "Jean",
 *     "nom": "Dupont",
 *     "email": "jean.dupont@email.com",
 *     "enabled": true,
 *     "roles": ["ROLE_ADMIN"],
 *     "createdAt": "2026-08-20T09:00:00"
 *   },
 *   {
 *     "id": 2,
 *     "prenom": "Marie",
 *     "nom": "Martin",
 *     "email": "marie.martin@email.com",
 *     "enabled": true,
 *     "roles": ["ROLE_USER"],
 *     "createdAt": "2026-08-20T10:00:00"
 *   }
 * ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UtilisateurListDTO {

    // ============================================================
    // IDENTIFIANT
    // ============================================================

    /**
     * ID technique de l'utilisateur
     */
    private Long id;

    // ============================================================
    // INFORMATIONS PERSONNELLES
    // ============================================================

    /**
     * Prénom de l'utilisateur
     */
    private String prenom;

    /**
     * Nom de l'utilisateur
     */
    private String nom;

    /**
     * Email de l'utilisateur
     */
    private String email;

    // ============================================================
    // GESTION DES ÉTATS (uniquement enabled)
    // ============================================================

    /**
     * Compte activé ou désactivé
     * C'est le seul état utile dans une liste
     */
    private boolean enabled;

    // ============================================================
    // RÔLES (uniquement les noms)
    // ============================================================

    /**
     * Liste des noms des rôles de l'utilisateur
     * Exemple : ["ROLE_ADMIN", "ROLE_USER"]
     */
    private Set<String> roles;

    // ============================================================
    // DATE DE CRÉATION (pour l'affichage)
    // ============================================================

    /**
     * Date de création du compte
     * Affichée dans la liste pour l'ordre chronologique
     */
    private LocalDateTime createdAt;

    // ============================================================
    // MÉTHODE UTILITAIRE
    // ============================================================

    /**
     * Retourne le nom complet de l'utilisateur
     *
     * @return "Prénom Nom"
     */
    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }
}