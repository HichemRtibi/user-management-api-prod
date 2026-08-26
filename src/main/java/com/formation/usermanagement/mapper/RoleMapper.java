package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleListDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MAPPER MANUEL POUR L'ENTITÉ ROLE
 *
 * Convertit entre l'entité Role et ses DTOs.
 */
public class RoleMapper {

    // ============================================================
    // 1. CONVERSION DTO → ENTITÉ
    // ============================================================

    /**
     * Convertit un RoleRequestDTO en entité Role.
     *
     * ⚠️ Les permissions ne sont pas gérées ici.
     * Elles seront ajoutées séparément.
     *
     * @param dto Le DTO de requête
     * @return L'entité Role
     */
    public static Role toEntity(RoleRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Role(
                dto.getName(),
                dto.getDescription()
        );
    }

    /**
     * Met à jour une entité Role existante avec les données d'un DTO.
     *
     * @param dto Le DTO de requête
     * @param role L'entité à mettre à jour
     */
    public static void updateEntity(RoleRequestDTO dto, Role role) {
        if (dto == null || role == null) {
            return;
        }

        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
    }

    // ============================================================
    // 2. CONVERSION ENTITÉ → DTO
    // ============================================================

    /**
     * Convertit une entité Role en RoleDTO (complet avec permissions).
     *
     * @param role L'entité Role
     * @return Le DTO Role complet
     */
    public static RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(extractPermissionDTOs(role))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    /**
     * Convertit une entité Role en RoleListDTO (version légère).
     *
     * @param role L'entité Role
     * @return Le DTO léger Role
     */
    public static RoleListDTO toListDTO(Role role) {
        if (role == null) {
            return null;
        }

        return RoleListDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissionNames(extractPermissionNames(role))
                .build();
    }

    // ============================================================
    // 3. MÉTHODES UTILITAIRES PRIVÉES
    // ============================================================

    /**
     * Extrait les permissions d'un rôle en PermissionDTO.
     */
    private static Set<PermissionDTO> extractPermissionDTOs(Role role) {
        if (role.getPermissions() == null) {
            return Set.of();
        }

        return role.getPermissions().stream()
                .map(PermissionMapper::toDTO)
                .collect(Collectors.toSet());
    }

    /**
     * Extrait uniquement les noms des permissions d'un rôle.
     */
    private static Set<String> extractPermissionNames(Role role) {
        if (role.getPermissions() == null) {
            return Set.of();
        }

        return role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}