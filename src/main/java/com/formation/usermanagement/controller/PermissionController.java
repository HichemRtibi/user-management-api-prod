package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.permision.PermissionRequestDTO;
import com.formation.usermanagement.service.PermissionService;
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
 * ============================================================
 * CONTROLLER PERMISSION
 * ============================================================
 *
 * 🎯 OBJECTIF : Gérer les endpoints REST pour les permissions
 *
 * 📋 ENDPOINTS DISPONIBLES :
 * - GET    /api/permissions           → Liste paginée
 * - GET    /api/permissions/all       → Liste complète
 * - GET    /api/permissions/{id}      → Détail d'une permission
 * - GET    /api/permissions/category/{category} → Par catégorie
 * - POST   /api/permissions           → Création
 * - PUT    /api/permissions/{id}      → Modification
 * - DELETE /api/permissions/{id}      → Suppression
 *
 * 🔐 Sécurité : À ajouter après l'étape 12
 * - @PreAuthorize("hasAuthority('PERMISSION_READ')")
 * - @PreAuthorize("hasAuthority('PERMISSION_WRITE')")
 * - @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;

    // ============================================================
    // 1. LISTE PAGINÉE DES PERMISSIONS
    // ============================================================

    /**
     * GET /api/permissions
     *
     * 📊 Paramètres de pagination :
     * - page : 0 (défaut)
     * - size : 10 (défaut)
     * - sort : name,asc (défaut)
     *
     * 📝 Exemple de requête :
     * GET /api/permissions?page=0&size=5&sort=category,asc
     *
     * 📝 Exemple de réponse :
     * {
     *   "content": [...],
     *   "totalElements": 42,
     *   "totalPages": 5,
     *   "size": 10,
     *   "number": 0,
     *   "numberOfElements": 10,
     *   "first": true,
     *   "last": false,
     *   "empty": false
     * }
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    public ResponseEntity<PageResponseDTO<PermissionDTO>> getAllPermissions(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("📋 Récupération des permissions - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        PageResponseDTO<PermissionDTO> response = permissionService.getAllPermissions(pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 2. LISTE COMPLÈTE DES PERMISSIONS (Sans pagination)
    // ============================================================

    /**
     * GET /api/permissions/all
     *
     * Utile pour :
     * - Les formulaires de sélection (dropdown)
     * - Les interfaces d'administration
     * - Les exports
     *
     * 📝 Exemple de réponse :
     * [
     *   { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." },
     *   { "id": 2, "category": "USER", "name": "USER_WRITE", "description": "..." }
     * ]
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")

    public ResponseEntity<List<PermissionDTO>> getAllPermissionsList() {
        log.info("📋 Récupération de toutes les permissions (sans pagination)");
        List<PermissionDTO> response = permissionService.getAllPermissionsList();
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 3. RÉCUPÉRATION D'UNE PERMISSION PAR ID
    // ============================================================

    /**
     * GET /api/permissions/{id}
     *
     * 📝 Exemple de réponse :
     * {
     *   "id": 1,
     *   "category": "USER",
     *   "name": "USER_READ",
     *   "description": "Permet de consulter les utilisateurs"
     * }
     *
     * 🔴 Erreurs possibles :
     * - 404 : Permission non trouvée
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")

    public ResponseEntity<PermissionDTO> getPermission(@PathVariable Long id) {
        log.info("🔍 Récupération de la permission ID : {}", id);
        PermissionDTO response = permissionService.getPermission(id);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 4. RÉCUPÉRATION DES PERMISSIONS PAR CATÉGORIE
    // ============================================================

    /**
     * GET /api/permissions/category/{category}
     *
     * 📝 Exemple de requête :
     * GET /api/permissions/category/USER
     *
     * 📝 Exemple de réponse :
     * [
     *   { "id": 1, "category": "USER", "name": "USER_READ", "description": "..." },
     *   { "id": 2, "category": "USER", "name": "USER_WRITE", "description": "..." },
     *   { "id": 3, "category": "USER", "name": "USER_DELETE", "description": "..." }
     * ]
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")

    public ResponseEntity<List<PermissionDTO>> getPermissionsByCategory(@PathVariable String category) {
        log.info("📋 Récupération des permissions pour la catégorie : {}", category);
        List<PermissionDTO> response = permissionService.getPermissionsByCategory(category);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 5. CRÉATION D'UNE PERMISSION
    // ============================================================

    /**
     * POST /api/permissions
     *
     * 📝 Corps de la requête (JSON) :
     * {
     *   "category": "USER",
     *   "name": "USER_READ",
     *   "description": "Permet de consulter les utilisateurs"
     * }
     *
     * 📝 Exemple de réponse :
     * {
     *   "id": 1,
     *   "category": "USER",
     *   "name": "USER_READ",
     *   "description": "Permet de consulter les utilisateurs"
     * }
     *
     * 🔴 Erreurs possibles :
     * - 400 : Validation échouée
     * - 409 : Permission déjà existante
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")

    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionRequestDTO dtop) {
        log.info("=== CRÉATION PERMISSION ===");
        log.info("📝 Nom : {}", dtop.getName());
        log.info("📂 Catégorie : {}", dtop.getCategory());

        PermissionDTO response = permissionService.creerPermission(dtop);

        log.info("✅ Permission créée avec ID : {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // 6. MISE À JOUR D'UNE PERMISSION
    // ============================================================

    /**
     * PUT /api/permissions/{id}
     *
     * 📝 Corps de la requête (JSON) :
     * {
     *   "category": "USER",
     *   "name": "USER_READ_UPDATED",
     *   "description": "Description mise à jour"
     * }
     *
     * 📝 Exemple de réponse :
     * {
     *   "id": 1,
     *   "category": "USER",
     *   "name": "USER_READ_UPDATED",
     *   "description": "Description mise à jour"
     * }
     *
     * 🔴 Erreurs possibles :
     * - 400 : Validation échouée
     * - 404 : Permission non trouvée
     * - 409 : Nom déjà utilisé par une autre permission
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")

    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequestDTO dto) {
        log.info("=== MISE À JOUR PERMISSION ID : {} ===", id);
        log.info("📝 Nouveau nom : {}", dto.getName());

        PermissionDTO response = permissionService.updatePermission(id, dto);

        log.info("✅ Permission mise à jour : {}", response.getName());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 7. SUPPRESSION D'UNE PERMISSION
    // ============================================================

    /**
     * DELETE /api/permissions/{id}
     *
     * 🔴 Erreurs possibles :
     * - 404 : Permission non trouvée
     * - 409 : Permission utilisée par des rôles
     *
     * ✅ Succès : 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")

    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        log.info("🗑️ Suppression de la permission ID : {}", id);
        permissionService.supprimerPermission(id);
        log.info("✅ Permission supprimée");
        return ResponseEntity.noContent().build();
    }
}