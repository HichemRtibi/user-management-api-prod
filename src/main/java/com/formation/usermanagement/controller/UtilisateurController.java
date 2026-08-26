package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.service.UtilisateurService;
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

/**
 * ================================================================
 * CONTROLLER POUR LA GESTION DES UTILISATEURS
 * ================================================================
 *
 * 🎯 OBJECTIF : Gérer les endpoints REST pour les utilisateurs
 *
 * 📋 ENDPOINTS DISPONIBLES :
 *
 * 1. CRUD :
 *    - GET    /api/utilisateurs                     → Liste paginée
 *    - GET    /api/utilisateurs/all                 → Liste complète (optionnel)
 *    - GET    /api/utilisateurs/{id}                → Détail d'un utilisateur
 *    - GET    /api/utilisateurs/email/{email}       → Détail par email
 *    - POST   /api/utilisateurs                     → Création
 *    - PUT    /api/utilisateurs/{id}                → Modification
 *    - DELETE /api/utilisateurs/{id}                → Suppression
 *
 * 2. GESTION DES ÉTATS :
 *    - PATCH  /api/utilisateurs/{id}/activer        → Activer
 *    - PATCH  /api/utilisateurs/{id}/desactiver     → Désactiver
 *    - PATCH  /api/utilisateurs/{id}/verrouiller    → Verrouiller
 *    - PATCH  /api/utilisateurs/{id}/deverrouiller  → Déverrouiller
 *    - PATCH  /api/utilisateurs/{id}/expirer        → Expirer
 *    - PATCH  /api/utilisateurs/{id}/renouveler     → Renouveler
 *
 * 3. GESTION DES RÔLES :
 *    - POST   /api/utilisateurs/{id}/roles/{roleName}    → Assigner un rôle
 *    - DELETE /api/utilisateurs/{id}/roles/{roleName}    → Retirer un rôle
 *
 * 4. GESTION DES PERMISSIONS (Directes) :
 *    - POST   /api/utilisateurs/{id}/permissions/{permissionName}   → Ajouter une permission
 *    - DELETE /api/utilisateurs/{id}/permissions/{permissionName}   → Retirer une permission
 *
 * 5. RECHERCHE :
 *    - GET    /api/utilisateurs/search?keyword=xxx    → Recherche par mot-clé
 *    - GET    /api/utilisateurs/role/{roleName}       → Par rôle
 *
 * 🔐 SÉCURITÉ (à activer après JWT) :
 *    - @PreAuthorize("hasAuthority('USER_READ')")
 *    - @PreAuthorize("hasAuthority('USER_WRITE')")
 *    - @PreAuthorize("hasAuthority('USER_DELETE')")
 *    - @PreAuthorize("hasAuthority('USER_ACTIVATE')")
 *
 * 📝 EXEMPLE D'UTILISATION AVEC POSTMAN :
 *
 * 1. Créer un utilisateur :
 *    POST http://localhost:8080/api/utilisateurs
 *    Body: {
 *      "prenom": "Jean",
 *      "nom": "Dupont",
 *      "email": "jean.dupont@email.com",
 *      "motDePasse": "Password123@"
 *    }
 *
 * 2. Récupérer tous les utilisateurs :
 *    GET http://localhost:8080/api/utilisateurs?page=0&size=10&sort=nom,asc
 *
 * 3. Activer un utilisateur :
 *    PATCH http://localhost:8080/api/utilisateurs/1/activer
 *
 * 4. Assigner un rôle :
 *    POST http://localhost:8080/api/utilisateurs/1/roles/ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // ================================================================
    // 1. LISTE PAGINÉE DES UTILISATEURS
    // ================================================================

    /**
     * GET /api/utilisateurs
     *
     * 🎯 OBJECTIF : Récupérer une liste paginée de tous les utilisateurs
     *
     * 📊 PARAMÈTRES DE PAGINATION (optionnels) :
     * - page : Numéro de la page (défaut: 0)
     * - size : Nombre d'éléments par page (défaut: 10)
     * - sort : Critère de tri (défaut: nom,asc)
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * GET /api/utilisateurs?page=0&size=5&sort=nom,asc
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "prenom": "Jean",
     *       "nom": "Dupont",
     *       "email": "jean.dupont@email.com",
     *       "enabled": true,
     *       "roles": ["ROLE_USER"],
     *       "createdAt": "2026-08-25T09:00:00"
     *     },
     *     {
     *       "id": 2,
     *       "prenom": "Marie",
     *       "nom": "Martin",
     *       "email": "marie.martin@email.com",
     *       "enabled": true,
     *       "roles": ["ROLE_ADMIN"],
     *       "createdAt": "2026-08-25T09:30:00"
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
     * 🔴 ERREURS POSSIBLES :
     * - 401 : Non authentifié
     * - 403 : Accès refusé (pas de permission USER_READ)
     */
    @GetMapping
     @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<PageResponseDTO<UtilisateurResponseDTO>> getAllUtilisateurs(
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des utilisateurs - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        PageResponseDTO<UtilisateurResponseDTO> response = utilisateurService.getAllUtilisateurs(pageable);

        log.info("✅ {} utilisateurs récupérés sur {}",
                response.getNumberOfElements(),
                response.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 2. RÉCUPÉRATION D'UN UTILISATEUR PAR ID
    // ================================================================

    /**
     * GET /api/utilisateurs/{id}
     *
     * 🎯 OBJECTIF : Récupérer un utilisateur par son ID
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * GET /api/utilisateurs/1
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "id": 1,
     *   "prenom": "Jean",
     *   "nom": "Dupont",
     *   "email": "jean.dupont@email.com",
     *   "enabled": true,
     *   "compteNonVerrouille": true,
     *   "compteNonExpire": true,
     *   "credentialsNonExpire": true,
     *   "roles": ["ROLE_USER"],
     *   "permissions": ["USER_READ"],
     *   "derniereConnexion": "2026-08-25T10:00:00",
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     *
     * 🔴 ERREUR (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Utilisateur avec l'ID 999 non trouvé"
     * }
     */
    @GetMapping("/{id}")
     @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateur(@PathVariable Long id) {
        log.info("🔍 Récupération de l'utilisateur ID : {}", id);
        long start = System.currentTimeMillis();


        UtilisateurResponseDTO response = utilisateurService.getUtilisateur(id);
        long duration = System.currentTimeMillis() - start;
        log.info("⏱️ Temps d'exécution : {} ms", duration);

        if (duration < 50) {
            log.info("📦 Cache HIT pour l'ID : {}", id);
        } else {
            log.info("🔴 Cache MISS pour l'ID : {}", id);
        }

        log.info("✅ Utilisateur trouvé : {} {}", response.getPrenom(), response.getNom());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 3. RÉCUPÉRATION D'UN UTILISATEUR PAR EMAIL
    // ================================================================

    /**
     * GET /api/utilisateurs/email/{email}
     *
     * 🎯 OBJECTIF : Récupérer un utilisateur par son email
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * GET /api/utilisateurs/email/jean.dupont@email.com
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "id": 1,
     *   "prenom": "Jean",
     *   "nom": "Dupont",
     *   "email": "jean.dupont@email.com",
     *   "enabled": true,
     *   "roles": ["ROLE_USER"],
     *   "permissions": ["USER_READ"],
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     *
     * 🔴 ERREUR (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Utilisateur avec l'email inconnu@email.com non trouvé"
     * }
     */
    @GetMapping("/email/{email}")
     @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurByEmail(@PathVariable String email) {
        log.info("🔍 Récupération de l'utilisateur par email : {}", email);

        UtilisateurResponseDTO response = utilisateurService.getUtilisateurByEmail(email);

        log.info("✅ Utilisateur trouvé : {} {}", response.getPrenom(), response.getNom());

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 4. CRÉATION D'UN UTILISATEUR
    // ================================================================

    /**
     * POST /api/utilisateurs
     *
     * 🎯 OBJECTIF : Créer un nouvel utilisateur
     *
     * 📋 CORPS DE LA REQUÊTE (JSON) :
     * {
     *   "prenom": "Jean",
     *   "nom": "Dupont",
     *   "email": "jean.dupont@email.com",
     *   "motDePasse": "Password123@"
     * }
     *
     * 📝 EXEMPLE DE RÉPONSE (201 Created) :
     * {
     *   "id": 1,
     *   "prenom": "Jean",
     *   "nom": "Dupont",
     *   "email": "jean.dupont@email.com",
     *   "enabled": true,
     *   "compteNonVerrouille": true,
     *   "compteNonExpire": true,
     *   "credentialsNonExpire": true,
     *   "roles": ["ROLE_USER"],
     *   "permissions": [],
     *   "derniereConnexion": null,
     *   "createdAt": "2026-08-25T10:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     *
     * 🔴 ERREUR (400 Bad Request) - Validation :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Validation échouée : email: L'email est obligatoire, prenom: Le prénom est obligatoire",
     *   "errors": {
     *     "email": "L'email est obligatoire",
     *     "prenom": "Le prénom est obligatoire"
     *   }
     * }
     *
     * 🔴 ERREUR (409 Conflict) - Email déjà existant :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "L'email jean.dupont@email.com est déjà utilisé par un autre utilisateur"
     * }
     */
    @PostMapping
    // @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<UtilisateurResponseDTO> createUtilisateur(
            @Valid @RequestBody UtilisateurRequestDTO dto) {

        log.info("=== CRÉATION UTILISATEUR ===");
        log.info("📧 Email : {}", dto.getEmail());
        log.info("👤 Prénom : {}", dto.getPrenom());
        log.info("👤 Nom : {}", dto.getNom());

        UtilisateurResponseDTO response = utilisateurService.creerUtilisateur(dto);

        log.info("✅ Utilisateur créé avec ID : {}", response.getId());
        log.info("=== FIN CRÉATION UTILISATEUR ===");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ================================================================
    // 5. MISE À JOUR D'UN UTILISATEUR
    // ================================================================

    /**
     * PUT /api/utilisateurs/{id}
     *
     * 🎯 OBJECTIF : Mettre à jour un utilisateur existant
     *
     * 📋 CORPS DE LA REQUÊTE (JSON) :
     * {
     *   "prenom": "Jean-Pierre",
     *   "nom": "Dupont",
     *   "email": "jeanpierre.dupont@email.com",
     *   "motDePasse": "NewPassword123@"
     * }
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "id": 1,
     *   "prenom": "Jean-Pierre",
     *   "nom": "Dupont",
     *   "email": "jeanpierre.dupont@email.com",
     *   "enabled": true,
     *   "roles": ["ROLE_USER"],
     *   "permissions": [],
     *   "createdAt": "2026-08-25T09:00:00",
     *   "updatedAt": "2026-08-25T10:00:00"
     * }
     *
     * 🔴 ERREUR (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Utilisateur avec l'ID 999 non trouvé"
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public ResponseEntity<UtilisateurResponseDTO> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurRequestDTO dto) {

        log.info("=== MISE À JOUR UTILISATEUR ID : {} ===", id);
        log.info("📧 Nouvel email : {}", dto.getEmail());
        log.info("👤 Nouveau prénom : {}", dto.getPrenom());

        UtilisateurResponseDTO response = utilisateurService.updateUtilisateur(id, dto);

        log.info("✅ Utilisateur mis à jour : {}", response.getEmail());
        log.info("=== FIN MISE À JOUR UTILISATEUR ===");

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 6. SUPPRESSION D'UN UTILISATEUR
    // ================================================================

    /**
     * DELETE /api/utilisateurs/{id}
     *
     * 🎯 OBJECTIF : Supprimer un utilisateur
     *
     * ✅ Succès : 204 No Content (réponse vide)
     *
     * 🔴 ERREUR (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Utilisateur avec l'ID 999 non trouvé"
     * }
     */
    @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        log.info("🗑️ Suppression de l'utilisateur ID : {}", id);

        utilisateurService.supprimerUtilisateur(id);

        log.info("✅ Utilisateur supprimé avec succès");

        return ResponseEntity.noContent().build();
    }

    // ================================================================
    // 7. GESTION DES ÉTATS - ACTIVER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/activer
     *
     * 🎯 OBJECTIF : Activer un utilisateur (enabled = true)
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 ERREUR (400 Bad Request) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "L'utilisateur jean.dupont@email.com ne peut pas être activé (état invalide)"
     * }
     */
    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAuthority('USER_ACTIVATE')")
    public ResponseEntity<Void> activerUtilisateur(@PathVariable Long id) {
        log.info("🔓 Activation de l'utilisateur ID : {}", id);

        utilisateurService.activerUtilisateur(id);

        log.info("✅ Utilisateur activé avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 8. GESTION DES ÉTATS - DÉSACTIVER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/desactiver
     *
     * 🎯 OBJECTIF : Désactiver un utilisateur (enabled = false)
     *
     * ✅ Succès : 200 OK (réponse vide)
     */
    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public ResponseEntity<Void> desactiverUtilisateur(@PathVariable Long id) {
        log.info("🔒 Désactivation de l'utilisateur ID : {}", id);

        utilisateurService.desactiverUtilisateur(id);

        log.info("✅ Utilisateur désactivé avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 9. GESTION DES ÉTATS - VERROUILLER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/verrouiller
     *
     * 🎯 OBJECTIF : Verrouiller un utilisateur (compteNonVerrouille = false)
     *
     * ✅ Succès : 200 OK (réponse vide)
     */
    @PatchMapping("/{id}/verrouiller")
    @PreAuthorize("hasAuthority('USER_LOCK')")
    public ResponseEntity<Void> verrouillerUtilisateur(@PathVariable Long id) {
        log.info("🔒 Verrouillage de l'utilisateur ID : {}", id);

        utilisateurService.verrouillerUtilisateur(id);

        log.info("✅ Utilisateur verrouillé avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 10. GESTION DES ÉTATS - DÉVERROUILLER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/deverrouiller
     *
     * 🎯 OBJECTIF : Déverrouiller un utilisateur (compteNonVerrouille = true)
     *
     * ✅ Succès : 200 OK (réponse vide)
     */
    @PatchMapping("/{id}/deverrouiller")
     @PreAuthorize("hasAuthority('USER_UNLOCK')")
    public ResponseEntity<Void> deverrouillerUtilisateur(@PathVariable Long id) {
        log.info("🔓 Déverrouillage de l'utilisateur ID : {}", id);

        utilisateurService.deverrouillerUtilisateur(id);

        log.info("✅ Utilisateur déverrouillé avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 11. GESTION DES ÉTATS - EXPIRER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/expirer
     *
     * 🎯 OBJECTIF : Expirer un utilisateur (compteNonExpire = false)
     *
     * ✅ Succès : 200 OK (réponse vide)
     */
    @PatchMapping("/{id}/expirer")
     @PreAuthorize("hasAuthority('USER_EXPIRE')")
    public ResponseEntity<Void> expirerUtilisateur(@PathVariable Long id) {
        log.info("⏰ Expiration de l'utilisateur ID : {}", id);

        utilisateurService.expirerUtilisateur(id);

        log.info("✅ Utilisateur expiré avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 12. GESTION DES ÉTATS - RENOUVELER
    // ================================================================

    /**
     * PATCH /api/utilisateurs/{id}/renouveler
     *
     * 🎯 OBJECTIF : Renouveler un utilisateur (compteNonExpire = true)
     *
     * ✅ Succès : 200 OK (réponse vide)
     */
    @PatchMapping("/{id}/renouveler")
     @PreAuthorize("hasAuthority('USER_RENEW')")
    public ResponseEntity<Void> renouvelerUtilisateur(@PathVariable Long id) {
        log.info("🔄 Renouvellement de l'utilisateur ID : {}", id);

        utilisateurService.renouvelerUtilisateur(id);

        log.info("✅ Utilisateur renouvelé avec succès");

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 13. GESTION DES RÔLES - ASSIGNER
    // ================================================================

    /**
     * POST /api/utilisateurs/{id}/roles/{roleName}
     *
     * 🎯 OBJECTIF : Assigner un rôle à un utilisateur
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * POST /api/utilisateurs/1/roles/ROLE_ADMIN
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 ERREUR (409 Conflict) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "L'utilisateur jean.dupont@email.com a déjà le rôle ROLE_ADMIN"
     * }
     */
    @PostMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<Void> assignerRole(
            @PathVariable Long id,
            @PathVariable String roleName) {

        log.info("📋 Assignation du rôle {} à l'utilisateur ID : {}", roleName, id);

        utilisateurService.assignerRole(id, roleName);

        log.info("✅ Rôle {} assigné avec succès", roleName);

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 14. GESTION DES RÔLES - RETIRER
    // ================================================================

    /**
     * DELETE /api/utilisateurs/{id}/roles/{roleName}
     *
     * 🎯 OBJECTIF : Retirer un rôle d'un utilisateur
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * DELETE /api/utilisateurs/1/roles/ROLE_ADMIN
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 ERREUR (404 Not Found) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Rôle ROLE_ADMIN non trouvé en base de données"
     * }
     */
    @DeleteMapping("/{id}/roles/{roleName}")
     @PreAuthorize("hasAuthority('ROLE_REMOVE')")
    public ResponseEntity<Void> retirerRole(
            @PathVariable Long id,
            @PathVariable String roleName) {

        log.info("🗑️ Retrait du rôle {} de l'utilisateur ID : {}", roleName, id);

        utilisateurService.retirerRole(id, roleName);

        log.info("✅ Rôle {} retiré avec succès", roleName);

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 15. GESTION DES PERMISSIONS DIRECTES - AJOUTER
    // ================================================================

    /**
     * POST /api/utilisateurs/{id}/permissions/{permissionName}
     *
     * 🎯 OBJECTIF : Ajouter une permission directement à un utilisateur
     * (Sans passer par un rôle)
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * POST /api/utilisateurs/1/permissions/USER_DELETE
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 ERREUR (409 Conflict) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "L'utilisateur jean.dupont@email.com a déjà la permission USER_DELETE"
     * }
     */
    @PostMapping("/{id}/permissions/{permissionName}")
     @PreAuthorize("hasAuthority('PERMISSION_ASSIGN')")
    public ResponseEntity<Void> ajouterPermission(
            @PathVariable Long id,
            @PathVariable String permissionName) {

        log.info("📋 Ajout de la permission {} à l'utilisateur ID : {}", permissionName, id);

        utilisateurService.ajouterPermission(id, permissionName);

        log.info("✅ Permission {} ajoutée avec succès", permissionName);

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 16. GESTION DES PERMISSIONS DIRECTES - RETIRER
    // ================================================================

    /**
     * DELETE /api/utilisateurs/{id}/permissions/{permissionName}
     *
     * 🎯 OBJECTIF : Retirer une permission directe d'un utilisateur
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * DELETE /api/utilisateurs/1/permissions/USER_DELETE
     *
     * ✅ Succès : 200 OK (réponse vide)
     *
     * 🔴 ERREUR (400 Bad Request) :
     * {
     *   "timestamp": "2026-08-25T10:00:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "L'utilisateur jean.dupont@email.com n'a pas la permission USER_DELETE"
     * }
     */
    @DeleteMapping("/{id}/permissions/{permissionName}")
     @PreAuthorize("hasAuthority('PERMISSION_REMOVE')")
    public ResponseEntity<Void> retirerPermission(
            @PathVariable Long id,
            @PathVariable String permissionName) {

        log.info("🗑️ Retrait de la permission {} de l'utilisateur ID : {}", permissionName, id);

        utilisateurService.retirerPermission(id, permissionName);

        log.info("✅ Permission {} retirée avec succès", permissionName);

        return ResponseEntity.ok().build();
    }

    // ================================================================
    // 17. RECHERCHE AVANCÉE - PAR MOT-CLÉ
    // ================================================================

    /**
     * GET /api/utilisateurs/search?keyword=xxx
     *
     * 🎯 OBJECTIF : Rechercher des utilisateurs par mot-clé (prénom ou nom)
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * GET /api/utilisateurs/search?keyword=Jean
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "prenom": "Jean",
     *       "nom": "Dupont",
     *       "email": "jean.dupont@email.com",
     *       "enabled": true,
     *       "roles": ["ROLE_USER"],
     *       "createdAt": "2026-08-25T09:00:00"
     *     },
     *     {
     *       "id": 3,
     *       "prenom": "Jean",
     *       "nom": "Martin",
     *       "email": "jean.martin@email.com",
     *       "enabled": true,
     *       "roles": ["ROLE_ADMIN"],
     *       "createdAt": "2026-08-25T09:30:00"
     *     }
     *   ],
     *   "totalElements": 2,
     *   "totalPages": 1,
     *   "size": 10,
     *   "number": 0,
     *   "numberOfElements": 2,
     *   "first": true,
     *   "last": true,
     *   "empty": false
     * }
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<PageResponseDTO<UtilisateurResponseDTO>> rechercherUtilisateurs(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("🔍 Recherche d'utilisateurs - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        PageResponseDTO<UtilisateurResponseDTO> response =
                utilisateurService.rechercherUtilisateurs(keyword, pageable);

        log.info("✅ {} utilisateurs trouvés pour le mot-clé '{}'",
                response.getTotalElements(), keyword);

        return ResponseEntity.ok(response);
    }

    // ================================================================
    // 18. RECHERCHE AVANCÉE - PAR RÔLE
    // ================================================================

    /**
     * GET /api/utilisateurs/role/{roleName}
     *
     * 🎯 OBJECTIF : Récupérer tous les utilisateurs ayant un rôle spécifique
     *
     * 📋 EXEMPLE DE REQUÊTE :
     * GET /api/utilisateurs/role/ROLE_ADMIN
     *
     * 📝 EXEMPLE DE RÉPONSE (200 OK) :
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "prenom": "Jean",
     *       "nom": "Dupont",
     *       "email": "jean.dupont@email.com",
     *       "enabled": true,
     *       "roles": ["ROLE_ADMIN"],
     *       "createdAt": "2026-08-25T09:00:00"
     *     }
     *   ],
     *   "totalElements": 1,
     *   "totalPages": 1,
     *   "size": 10,
     *   "number": 0,
     *   "numberOfElements": 1,
     *   "first": true,
     *   "last": true,
     *   "empty": false
     * }
     */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<PageResponseDTO<UtilisateurResponseDTO>> getUtilisateursByRole(
            @PathVariable String roleName,
            @PageableDefault(size = 10, sort = "nom", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des utilisateurs avec le rôle : {}", roleName);

        PageResponseDTO<UtilisateurResponseDTO> response =
                utilisateurService.getUtilisateursByRole(roleName, pageable);

        log.info("✅ {} utilisateurs trouvés avec le rôle {}",
                response.getTotalElements(), roleName);

        return ResponseEntity.ok(response);
    }
}