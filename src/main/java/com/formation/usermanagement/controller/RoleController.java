package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import com.formation.usermanagement.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ================================================================
 * CONTROLLER POUR LA GESTION DES RÔLES
 * ================================================================
 *
 * 🎯 OBJECTIF : Gérer les endpoints REST pour les rôles
 *
 * 📋 ENDPOINTS DISPONIBLES :
 * - GET    /api/roles                      → Liste paginée
 * - GET    /api/roles/all                  → Liste complète
 * - GET    /api/roles/{id}                 → Détail d'un rôle
 * - GET    /api/roles/name/{name}          → Détail par nom
 * - POST   /api/roles                      → Création
 * - PUT    /api/roles/{id}                 → Modification
 * - DELETE /api/roles/{id}                 → Suppression
 * - POST   /api/roles/{roleId}/permissions/{permissionId} → Ajouter permission
 * - DELETE /api/roles/{roleId}/permissions/{permissionId} → Retirer permission
 * - GET    /api/roles/{id}/count-users     → Compter les utilisateurs
 *
 * 🔐 SÉCURITÉ (à activer après JWT) :
 * - @PreAuthorize("hasAuthority('ROLE_READ')")
 * - @PreAuthorize("hasAuthority('ROLE_WRITE')")
 * - @PreAuthorize("hasAuthority('ROLE_DELETE')")
 * - @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    // ================================================================
    // 1. LISTE PAGINÉE DES RÔLES
    // ================================================================

    /**
     * GET /api/roles
     *
     * 📊 Paramètres de pagination :
     * - page : 0 (défaut)
     * - size : 10 (défaut)
     * - sort : name,asc (défaut)
     *
     * 📝 Exemple de requête :
     * GET /api/roles?page=0&size=5&sort=name,asc
     *
     * 📝 Exemple de réponse (200 OK) :
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "name": "ROLE_ADMIN",
     *       "description": "Administrateur système",
     *       "permissions": [
     *         { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." },
     *         { "id": 2, "category": "USER", "name": "USER_WRITE", "description": "..." }
     *       ],
     *       "createdAt": "2026-08-25T09:00:00",
     *       "updatedAt": "2026-08-25T09:30:00"
     *     }
     *   ],
     *   "totalElements": 10,
     *   "totalPages": 2,
     *   "size": 5,
     *   "number": 0,
     *   "numberOfElements": 5,
     *   "first": true,
     *   "last": false,
     *   "empty": false
     * }
     *
     * 🔴 Erreurs possibles :
     * - 401 : Non authentifié
     * - 403 : Accès refusé (pas de permission ROLE_READ)
     */
    @GetMapping
     @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<PageResponseDTO<RoleDTO>> getAllRoles(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des rôles - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        PageResponseDTO<RoleDTO> response = roleService.getAllRoles(pageable);

        log.info("✅ {} rôles récupérés sur {}",
                response.getNumberOfElements(),
                response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 2. LISTE COMPLÈTE DES RÔLES (Sans pagination)
    // ================================================================

    /**
     * GET /api/roles/all
     *
     * 🎯 OBJECTIF : Récupérer tous les rôles sans pagination
     *
     * 🔍 UTILISATION :
     * - Formulaires de sélection (dropdown)
     * - Exports de données
     * - Interfaces d'administration
     *
     * 📝 Exemple de réponse (200 OK) :
     * [
     *   {
     *     "id": 1,
     *     "name": "ROLE_ADMIN",
     *     "description": "Administrateur système",
     *     "permissions": [...]
     *   },
     *   {
     *     "id": 2,
     *     "name": "ROLE_USER",
     *     "description": "Utilisateur standard",
     *     "permissions": [...]
     *   }
     * ]
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<List<RoleDTO>> getAllRolesList() {
        log.info("📋 Récupération de tous les rôles (sans pagination)");

        List<RoleDTO> response = roleService.getAllRolesList();

        log.info("✅ {} rôles récupérés", response.size());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 3. RÉCUPÉRATION D'UN RÔLE PAR ID
    // ================================================================

    /**
     * GET /api/roles/{id}
     *
     * 🎯 OBJECTIF : Récupérer un rôle par son ID avec toutes ses permissions
     *
     * 📝 Exemple de réponse (200 OK) :
     * {
     *   "id": 1,
     *   "name": "ROLE_ADMIN",
     *   "description": "Administrateur système",
     *   "permissions": [
     *     { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." },
     *     { "id": 2, "category": "USER", "name": "USER_WRITE", "description": "..." }
     *   ],
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T09:30:00"
     * }
     *
     * 🔴 Erreur (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Rôle ID: 999 non trouvé en base de données"
     * }
     * (Géré par GlobalExceptionHandler)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleDTO> getRole(@PathVariable Long id) {
        log.info("🔍 Récupération du rôle ID : {}", id);

        RoleDTO response = roleService.getRole(id);

        log.info("✅ Rôle trouvé : {} - {}", response.getId(), response.getName());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 4. RÉCUPÉRATION D'UN RÔLE PAR NOM
    // ================================================================

    /**
     * GET /api/roles/name/{name}
     *
     * 📝 Exemple de requête :
     * GET /api/roles/name/ROLE_ADMIN
     *
     * 📝 Exemple de réponse (200 OK) :
     * {
     *   "id": 1,
     *   "name": "ROLE_ADMIN",
     *   "description": "Administrateur système",
     *   "permissions": [...],
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T09:30:00"
     * }
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String name) {
        log.info("🔍 Récupération du rôle par nom : {}", name);

        RoleDTO response = roleService.getRoleByName(name);

        log.info("✅ Rôle trouvé : {} - {}", response.getId(), response.getName());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 5. CRÉATION D'UN RÔLE
    // ================================================================

    /**
     * POST /api/roles
     *
     * 📋 Corps de la requête (JSON) :
     * {
     *   "name": "ROLE_MANAGER",
     *   "description": "Manager avec des permissions limitées",
     *   "permissionIds": [1, 2, 3]
     * }
     *
     * 📝 Exemple de réponse (201 Created) :
     * {
     *   "id": 4,
     *   "name": "ROLE_MANAGER",
     *   "description": "Manager avec des permissions limitées",
     *   "permissions": [
     *     { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." },
     *     { "id": 2, "category": "USER", "name": "USER_WRITE", "description": "..." },
     *     { "id": 3, "category": "USER", "name": "USER_DELETE", "description": "..." }
     *   ],
     *   "createdAt": "2026-08-25T10:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     *
     * 🔴 Erreur (409 Conflict) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "Le rôle ROLE_MANAGER existe déjà"
     * }
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleRequestDTO dto) {
        log.info("=== CRÉATION RÔLE ===");
        log.info("📝 Nom : {}", dto.getName());
        log.info("📋 Description : {}", dto.getDescription());
        log.info("🔢 Permissions : {}", dto.getPermissionIds());

        RoleDTO response = roleService.creerRole(dto);

        log.info("✅ Rôle créé avec ID : {}", response.getId());
        log.info("=== FIN CRÉATION RÔLE ===");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================================================================
    // 6. MISE À JOUR D'UN RÔLE
    // ================================================================

    /**
     * PUT /api/roles/{id}
     *
     * 📋 Corps de la requête (JSON) :
     * {
     *   "name": "ROLE_MANAGER_UPDATED",
     *   "description": "Manager avec permissions mises à jour",
     *   "permissionIds": [1, 2, 3, 4]
     * }
     *
     * 📝 Exemple de réponse (200 OK) :
     * {
     *   "id": 4,
     *   "name": "ROLE_MANAGER_UPDATED",
     *   "description": "Manager avec permissions mises à jour",
     *   "permissions": [...],
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")

    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO dto) {

        log.info("=== MISE À JOUR RÔLE ID : {} ===", id);
        log.info("📝 Nouveau nom : {}", dto.getName());
        log.info("🔢 Nouvelles permissions : {}", dto.getPermissionIds());

        RoleDTO response = roleService.updateRole(id, dto);

        log.info("✅ Rôle mis à jour : {}", response.getName());
        log.info("=== FIN MISE À JOUR RÔLE ===");

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 7. SUPPRESSION D'UN RÔLE
    // ================================================================

    /**
     * DELETE /api/roles/{id}
     *
     * ✅ Succès : 204 No Content (réponse vide)
     *
     * 🔴 Erreur (409 Conflict) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "Le rôle ROLE_MANAGER est utilisé par 5 utilisateur(s) et ne peut pas être supprimé"
     * }
     */
    @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("🗑️ Suppression du rôle ID : {}", id);

        roleService.supprimerRole(id);

        log.info("✅ Rôle supprimé avec succès");

        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // 8. AJOUTER UNE PERMISSION À UN RÔLE
    // ================================================================

    /**
     * POST /api/roles/{roleId}/permissions/{permissionId}
     *
     * 📝 Exemple de requête :
     * POST /api/roles/1/permissions/5
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 Erreur (409 Conflict) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "La permission USER_READ est déjà assignée au rôle ROLE_ADMIN"
     * }
     */
    @PostMapping("/{roleId}/permissions/{permissionId}")
     @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<Void> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        log.info("📋 Ajout de la permission ID {} au rôle ID {}", permissionId, roleId);

        roleService.ajouterPermission(roleId, permissionId);

        log.info("✅ Permission ajoutée au rôle");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 9. RETIRER UNE PERMISSION D'UN RÔLE
    // ================================================================

    /**
     * DELETE /api/roles/{roleId}/permissions/{permissionId}
     *
     * 📝 Exemple de requête :
     * DELETE /api/roles/1/permissions/5
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 Erreur (400 Bad Request) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "La permission USER_READ n'est pas assignée au rôle ROLE_ADMIN"
     * }
     */
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {

        log.info("🗑️ Retrait de la permission ID {} du rôle ID {}", permissionId, roleId);

        roleService.retirerPermission(roleId, permissionId);

        log.info("✅ Permission retirée du rôle");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 10. COMPTER LES UTILISATEURS D'UN RÔLE
    // ================================================================

    /**
     * GET /api/roles/{id}/count-users
     *
     * 📝 Exemple de réponse (200 OK) :
     * {
     *   "roleId": 1,
     *   "roleName": "ROLE_ADMIN",
     *   "userCount": 5
     * }
     */
    @GetMapping("/{id}/count-users")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ResponseEntity<RoleUserCountDTO> countUsersByRole(@PathVariable Long id) {
        log.info("📊 Comptage des utilisateurs pour le rôle ID : {}", id);

        RoleDTO role = roleService.getRole(id);
        long count = roleService.countUtilisateursByRole(id);

        log.info("✅ {} utilisateur(s) pour le rôle {}", count, role.getName());

        RoleUserCountDTO response = RoleUserCountDTO.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .userCount(count)
                .build();

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 11. CLASSE INTERNE POUR LE COMPTAGE
    // ================================================================

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoleUserCountDTO {
        private Long roleId;
        private String roleName;
        private long userCount;
    }
}