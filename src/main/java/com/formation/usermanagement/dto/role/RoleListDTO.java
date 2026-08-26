package com.formation.usermanagement.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO LÉGER POUR LE RÔLE
 *
 * Utilisé pour les listes de rôles (sans les permissions complètes).
 *
 * Exemple de réponse JSON :
 * {
 *   "id": 1,
 *   "name": "ROLE_ADMIN",
 *   "description": "Administrateur avec toutes les permissions",
 *   "permissionNames": ["USER_READ", "USER_WRITE", "USER_DELETE"]
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleListDTO {

    private Long id;
    private String name;
    private String description;
    private Set<String> permissionNames;  // Uniquement les noms des permissions
}