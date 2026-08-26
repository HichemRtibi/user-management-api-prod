package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.role.RoleListDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurListDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MAPPER MANUEL POUR L'ENTITÉ UTILISATEUR (Version Builder)
 *
 * ⚠️ MAPPING MANUEL : Nous écrivons chaque méthode de conversion
 * pour avoir un contrôle total sur le processus.
 *
 * Avantages du mapping manuel :
 * - 100% transparent (pas de magie noire)
 * - Facile à déboguer
 * - Contrôle total sur ce qui est converti
 * - Pas de dépendance à MapStruct
 *
 * ⚠️ Utilisation du pattern Builder :
 * - Les DTOs utilisent @Builder pour une création fluide
 * - toResponseDTO() et toListDTO() retournent des DTOs construits avec Builder
 *
 * Méthodes disponibles :
 * - toEntity(UtilisateurRequestDTO) : DTO → Entité (pour création)
 * - updateEntity(UtilisateurRequestDTO, Utilisateur) : DTO → Entité (pour mise à jour)
 * - toResponseDTO(Utilisateur) : Entité → DTO (réponse complète)
 * - toListDTO(Utilisateur) : Entité → DTO (version légère)
 */
public class UtilisateurMapper {

    // ============================================================
    // 1. CONVERSION DTO → ENTITÉ (Création)
    // ============================================================

    /**
     * Convertit un UtilisateurRequestDTO en entité Utilisateur.
     *
     * Utilisé pour la création d'un nouvel utilisateur.
     *
     * ⚠️ Le mot de passe est conservé pour être encodé ensuite.
     * ⚠️ Les rôles et permissions ne sont pas gérés ici.
     *
     * @param dto Le DTO de requête
     * @return L'entité Utilisateur
     */
    public static Utilisateur toEntity(UtilisateurRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setMotDePasse(dto.getMotDePasse());

        // ⚠️ Les champs d'état restent avec leurs valeurs par défaut :
        // - enabled = true
        // - compteNonVerrouille = true
        // - compteNonExpire = true
        // - credentialsNonExpire = true

        return utilisateur;
    }

    // ============================================================
    // 2. CONVERSION DTO → ENTITÉ (Mise à jour)
    // ============================================================

    /**
     * Met à jour une entité Utilisateur existante avec les données d'un DTO.
     *
     * Utilisé pour la modification d'un utilisateur existant.
     *
     * ⚠️ Le mot de passe n'est mis à jour que s'il est présent dans le DTO.
     * ⚠️ Les champs d'état ne sont pas modifiés ici (ils ont leurs propres méthodes).
     *
     * @param dto Le DTO de requête
     * @param utilisateur L'entité à mettre à jour
     */
    public static void updateEntity(UtilisateurRequestDTO dto, Utilisateur utilisateur) {
        if (dto == null || utilisateur == null) {
            return;
        }

        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setNom(dto.getNom());
        utilisateur.setEmail(dto.getEmail());

        // ⚠️ Ne mettre à jour le mot de passe que s'il est fourni
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(dto.getMotDePasse());
        }
    }

    // ============================================================
    // 3. CONVERSION ENTITÉ → DTO (Réponse complète avec Builder)
    // ============================================================

    /**
     * Convertit une entité Utilisateur en UtilisateurResponseDTO.
     *
     * ⚠️ Utilise le pattern Builder pour créer le DTO de manière fluide.
     *
     * Utilisé pour les réponses GET individuelles.
     *
     * ⚠️ Le mot de passe n'est JAMAIS inclus.
     * ⚠️ Les rôles et permissions sont extraits et convertis en Set de Strings.
     *
     * @param utilisateur L'entité Utilisateur
     * @return Le DTO de réponse complet
     */
    public static UtilisateurResponseDTO toResponseDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return UtilisateurResponseDTO.builder()
                // Informations personnelles
                .id(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())

                // Gestion des états
                .enabled(utilisateur.isEnabled())
                .compteNonVerrouille(utilisateur.isCompteNonVerrouille())
                .compteNonExpire(utilisateur.isCompteNonExpire())
                .credentialsNonExpire(utilisateur.isCredentialsNonExpire())

                // Rôles et permissions
                .roles(extractRoleNames(utilisateur))
                .permissions(utilisateur.getAllPermissions())

                // Informations de connexion
                .derniereConnexion(utilisateur.getDerniereConnexion())

                // Audit
                .createdAt(utilisateur.getCreatedAt())
                .updatedAt(utilisateur.getUpdatedAt())
                .build();
    }

    // ============================================================
    // 4. CONVERSION ENTITÉ → DTO (Version légère avec Builder)
    // ============================================================

    /**
     * Convertit une entité Utilisateur en UtilisateurListDTO (version légère).
     *
     * ⚠️ Utilise le pattern Builder pour créer le DTO de manière fluide.
     *
     * Utilisé pour les listes paginées.
     *
     * ⚠️ Contient moins de champs pour optimiser les performances.
     *
     * @param utilisateur L'entité Utilisateur
     * @return Le DTO léger pour les listes
     */
    public static UtilisateurListDTO toListDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        return UtilisateurListDTO.builder()
                .id(utilisateur.getId())
                .prenom(utilisateur.getPrenom())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .enabled(utilisateur.isEnabled())
                .roles(extractRoleNames(utilisateur))
                .createdAt(utilisateur.getCreatedAt())
                .build();
    }

    // ============================================================
    // 5. MÉTHODES UTILITAIRES PRIVÉES
    // ============================================================

    /**
     * Extrait les noms des rôles d'un utilisateur.
     *
     * @param utilisateur L'utilisateur
     * @return Set des noms des rôles
     */
    private static Set<String> extractRoleNames(Utilisateur utilisateur) {
        if (utilisateur.getRoles() == null) {
            return Set.of();
        }

        return utilisateur.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
    // Dans UtilisateurMapper.java, ajoute cette méthode si besoin :

    /**
     * Extrait les rôles d'un utilisateur en RoleListDTO.
     *
     * @param utilisateur L'utilisateur
     * @return Set de RoleListDTO
     */
    public static Set<RoleListDTO> extractRoleListDTOs(Utilisateur utilisateur) {
        if (utilisateur.getRoles() == null) {
            return Set.of();
        }

        return utilisateur.getRoles().stream()
                .map(RoleMapper::toListDTO)
                .collect(Collectors.toSet());
    }

}