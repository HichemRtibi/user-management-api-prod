package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.permision.PermissionRequestDTO;

import com.formation.usermanagement.entity.Permission;

/**
 * MAPPER MANUEL POUR L'ENTITÉ PERMISSION
 *
 * Convertit entre l'entité Permission et ses DTOs.
 *
 * Méthodes disponibles :
 * - toEntity(PermissionRequestDTO) : DTO → Entité (création)
 * - toDTO(Permission) : Entité → DTO (réponse)
 */
public class PermissionMapper {

    /**
     * Convertit un PermissionRequestDTO en entité Permission.
     *
     * Utilisé pour la création d'une nouvelle permission.
     *
     * @param dto Le DTO de requête
     * @return L'entité Permission
     */
    public static Permission toEntity(PermissionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return new Permission(
                dto.getCategory(),
                dto.getName(),
                dto.getDescription()
        );
    }

    /**
     * Convertit une entité Permission en PermissionDTO.
     *
     * Utilisé pour les réponses (GET).
     *
     * @param permission L'entité Permission
     * @return Le DTO Permission
     */
    public static PermissionDTO toDTO(Permission permission) {
        if (permission == null) {
            return null;
        }

        return PermissionDTO.builder()
                .id(permission.getId())
                .category(permission.getCategory())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}