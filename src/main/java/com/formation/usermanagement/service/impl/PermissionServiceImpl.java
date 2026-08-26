package com.formation.usermanagement.service.impl;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.permision.PermissionRequestDTO;

import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.exception.PermissionDejaExistantException;
import com.formation.usermanagement.exception.PermissionNotFoundException;
import com.formation.usermanagement.exception.PermissionUtiliseException;
import com.formation.usermanagement.mapper.PermissionMapper;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IMPLÉMENTATION DU SERVICE PERMISSION
 *
 * Cette classe contient TOUTE la logique métier pour la gestion des permissions.
 *
 * ⚠️ ANNOTATIONS IMPORTANTES :
 * - @Service : Déclare que cette classe est un bean Spring (Service)
 * - @Transactional : Gère les transactions (rollback automatique en cas d'erreur)
 * - @Slf4j : Active les logs (log.info, log.debug, log.warn, log.error)
 * - @RequiredArgsConstructor : Génère un constructeur avec tous les champs final
 *
 * Pourquoi @Transactional ?
 * - Les opérations de création/modification/suppression doivent être atomiques
 * - Si une erreur survient, la transaction est annulée (rollback)
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    // ============================================================
    // 1. DÉPENDANCES (Injection par constructeur)
    // ============================================================

    /**
     * 📌 permissionRepository : Accès à la BDD pour les permissions
     *
     * Méthodes disponibles :
     * - save() : Sauvegarder une permission
     * - findById() : Récupérer par ID
     * - findByName() : Récupérer par nom
     * - findByCategoryAndName() : Récupérer par catégorie et nom
     * - existsByName() : Vérifier l'existence par nom
     * - existsByCategoryAndName() : Vérifier l'existence par combinaison
     * - deleteById() : Supprimer
     * - findAll() : Récupérer toutes
     */
    private final PermissionRepository permissionRepository;

    /**
     * 📌 roleRepository : Accès à la BDD pour les rôles
     *
     * Utilisé pour vérifier si une permission est utilisée par des rôles
     * avant de la supprimer.
     */
    private final RoleRepository roleRepository;

    // ============================================================
    // 2. CRÉATION D'UNE PERMISSION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : creerPermission()
     * ============================================================
     *
     * 🎯 OBJECTIF : Créer une nouvelle permission
     *
     * 📋 ÉTAPES :
     *
     * 1. Vérifier que le nom n'existe pas déjà
     *    → Si oui → PermissionDejaExistantException
     *
     * 2. Vérifier que la combinaison (category, name) n'existe pas
     *    → Si oui → PermissionDejaExistantException
     *
     * 3. Convertir le DTO en entité (Mapper)
     *    → PermissionMapper.toEntity(dto)
     *
     * 4. Sauvegarder en base
     *    → permissionRepository.save(permission)
     *
     * 5. Convertir l'entité sauvegardée en DTO
     *    → PermissionMapper.toDTO(saved)
     *
     * 6. Retourner le DTO
     *
     * 🔴 EXCEPTIONS POSSIBLES :
     * - PermissionDejaExistantException : Le nom ou la combinaison existe déjà
     *
     * ✅ SUCCÈS : PermissionDTO
     *
     * 📊 EXEMPLE D'APPEL :
     * PermissionRequestDTO dto = PermissionRequestDTO.builder()
     *     .category("USER")
     *     .name("USER_READ")
     *     .description("Lire les utilisateurs")
     *     .build();
     *
     * PermissionDTO resultat = permissionService.creerPermission(dto);
     * // resultat.getId() → 1
     * // resultat.getName() → "USER_READ"
     */
    @Override
    @Transactional
    public PermissionDTO creerPermission(@Valid PermissionRequestDTO dto) {
        log.info("=== DÉBUT création permission ===");
        log.info("📝 Nom : {}", dto.getName());
        log.info("📂 Catégorie : {}", dto.getCategory());

        // ============================================================
        // ÉTAPE 1 : Vérifier l'unicité du nom
        // ============================================================
        log.debug("🔍 Vérification existence nom : {}", dto.getName());
        if (permissionRepository.existsByName(dto.getName())) {
            log.warn("❌ Permission {} existe déjà", dto.getName());
            throw new PermissionDejaExistantException(dto.getName());
        }
        log.info("✅ Nom disponible");

        // ============================================================
        // ÉTAPE 2 : Vérifier l'unicité de la combinaison category + name
        // ============================================================
        log.debug("🔍 Vérification combinaison : {}_{}", dto.getCategory(), dto.getName());
        if (permissionRepository.existsByCategoryAndName(dto.getCategory(), dto.getName())) {
            log.warn("❌ Permission {}_{} existe déjà", dto.getCategory(), dto.getName());
            throw new PermissionDejaExistantException(dto.getCategory(), dto.getName());
        }
        log.info("✅ Combinaison disponible");

        // ============================================================
        // ÉTAPE 3 : Convertir DTO → Entité
        // ============================================================
        // Pourquoi ?
        // → Le DTO est ce que le client envoie
        // → L'entité est ce qu'on stocke en base
        Permission permission = PermissionMapper.toEntity(dto);
        log.debug("📦 Entité créée : {} - {}", permission.getCategory(), permission.getName());

        // ============================================================
        // ÉTAPE 4 : Sauvegarder en base
        // ============================================================
        Permission saved = permissionRepository.save(permission);
        log.info("✅ Permission sauvegardée avec ID : {}", saved.getId());

        // ============================================================
        // ÉTAPE 5 & 6 : Convertir en DTO et retourner
        // ============================================================
        PermissionDTO response = PermissionMapper.toDTO(saved);
        log.info("=== FIN création permission (succès) ===");
        return response;
    }

    // ============================================================
    // 3. RÉCUPÉRATION D'UNE PERMISSION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : getPermission()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer une permission par son ID
     *
     * 📋 ÉTAPES :
     * 1. Chercher la permission en base (findById)
     * 2. Si non trouvée → PermissionNotFoundException
     * 3. Convertir en DTO
     * 4. Retourner le DTO
     *
     * 🔴 EXCEPTION : PermissionNotFoundException
     * ✅ SUCCÈS : PermissionDTO
     */
    @Override
    public PermissionDTO getPermission(Long id) {
        log.debug("🔍 Récupération de la permission avec ID : {}", id);

        // ============================================================
        // ÉTAPE 1 & 2 : Recherche et vérification d'existence
        // ============================================================
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec ID {} non trouvée", id);
                    return new PermissionNotFoundException("ID: " + id);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ============================================================
        // ÉTAPE 3 & 4 : Conversion et retour
        // ============================================================
        return PermissionMapper.toDTO(permission);
    }

    /**
     * ============================================================
     * MÉTHODE : getPermissionByName()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer une permission par son nom
     *
     * 🔴 EXCEPTION : PermissionNotFoundException
     * ✅ SUCCÈS : PermissionDTO
     */
    @Override
    public PermissionDTO getPermissionByName(String name) {
        log.debug("🔍 Récupération de la permission avec nom : {}", name);

        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec nom {} non trouvée", name);
                    return new PermissionNotFoundException(name);
                });

        return PermissionMapper.toDTO(permission);
    }

    // ============================================================
    // 4. LISTES DE PERMISSIONS
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : getAllPermissions()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer toutes les permissions avec pagination
     *
     * 📋 ÉTAPES :
     * 1. Appeler le repository avec les paramètres de pagination
     * 2. Mapper la page (Permission → PermissionDTO)
     * 3. Retourner un PageResponseDTO
     *
     * 📊 Paramètres de pagination (Pageable) :
     * - page : Numéro de la page (commence à 0)
     * - size : Nombre d'éléments par page (ex: 10, 20, 50)
     * - sort : Tri (ex: "name,asc")
     *
     * ✅ SUCCÈS : PageResponseDTO<PermissionDTO>
     */
    @Override
    public PageResponseDTO<PermissionDTO> getAllPermissions(Pageable pageable) {
        log.info("📋 Récupération des permissions - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Permission> page = permissionRepository.findAll(pageable);
        Page<PermissionDTO> mappedPage = page.map(PermissionMapper::toDTO);

        return new PageResponseDTO<>(mappedPage);
    }

    /**
     * ============================================================
     * MÉTHODE : getAllPermissionsList()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer toutes les permissions (sans pagination)
     *
     * Utile pour :
     * - Les formulaires de sélection
     * - Les interfaces d'administration
     * - Les exports
     *
     * ✅ SUCCÈS : List<PermissionDTO>
     */
    @Override
    public List<PermissionDTO> getAllPermissionsList() {
        log.debug("📋 Récupération de toutes les permissions (sans pagination)");

        List<Permission> permissions = permissionRepository.findAllByOrderByCategoryAscNameAsc();
        return permissions.stream()
                .map(PermissionMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * ============================================================
     * MÉTHODE : getPermissionsByCategory()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer les permissions d'une catégorie spécifique
     *
     * Exemple : getPermissionsByCategory("USER") → [USER_READ, USER_WRITE, ...]
     *
     * @param category La catégorie (ex: "USER", "ROLE")
     * @return Liste des permissions de cette catégorie
     */
    @Override
    public List<PermissionDTO> getPermissionsByCategory(String category) {
        log.debug("📋 Récupération des permissions pour la catégorie : {}", category);

        List<Permission> permissions = permissionRepository.findByCategoryOrderByNameAsc(category);
        return permissions.stream()
                .map(PermissionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 5. MISE À JOUR D'UNE PERMISSION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : updatePermission()
     * ============================================================
     *
     * 🎯 OBJECTIF : Mettre à jour une permission existante
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que la permission existe → PermissionNotFoundException
     * 2. Vérifier que le nom n'est pas utilisé par une autre permission
     * 3. Vérifier que la combinaison (category, name) n'est pas utilisée
     * 4. Mettre à jour les champs
     * 5. Sauvegarder
     * 6. Retourner le DTO
     *
     * 🔴 EXCEPTIONS :
     * - PermissionNotFoundException
     * - PermissionDejaExistantException
     *
     * ✅ SUCCÈS : PermissionDTO
     */
    @Override
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionRequestDTO dto) {
        log.info("=== DÉBUT mise à jour permission ID : {} ===", id);

        // ============================================================
        // ÉTAPE 1 : Vérifier que la permission existe
        // ============================================================
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec ID {} non trouvée", id);
                    return new PermissionNotFoundException("ID: " + id);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ============================================================
        // ÉTAPE 2 : Vérifier que le nom n'est pas utilisé par une autre permission
        // ============================================================
        permissionRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("❌ Le nom {} est déjà utilisé par une autre permission", dto.getName());
                        throw new PermissionDejaExistantException(dto.getName());
                    }
                });
        log.debug("✅ Nom disponible");

        // ============================================================
        // ÉTAPE 3 : Vérifier la combinaison category + name
        // ============================================================
        permissionRepository.findByCategoryAndName(dto.getCategory(), dto.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("❌ La combinaison {}_{} est déjà utilisée", dto.getCategory(), dto.getName());
                        throw new PermissionDejaExistantException(dto.getCategory(), dto.getName());
                    }
                });
        log.debug("✅ Combinaison disponible");

        // ============================================================
        // ÉTAPE 4 : Mettre à jour les champs
        // ============================================================
        permission.setCategory(dto.getCategory());
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        log.debug("📝 Permission mise à jour : {} - {}", permission.getCategory(), permission.getName());

        // ============================================================
        // ÉTAPE 5 : Sauvegarder
        // ============================================================
        Permission saved = permissionRepository.save(permission);
        log.info("✅ Permission {} mise à jour avec succès", saved.getName());

        // ============================================================
        // ÉTAPE 6 : Retourner le DTO
        // ============================================================
        log.info("=== FIN mise à jour permission (succès) ===");
        return PermissionMapper.toDTO(saved);
    }

    // ============================================================
    // 6. SUPPRESSION D'UNE PERMISSION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : supprimerPermission()
     * ============================================================
     *
     * 🎯 OBJECTIF : Supprimer une permission
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que la permission existe → PermissionNotFoundException
     * 2. Vérifier qu'elle n'est pas utilisée par des rôles
     *    → PermissionUtiliseException
     * 3. Supprimer
     *
     * Pourquoi vérifier l'utilisation ?
     * → Une permission utilisée par un rôle ne peut pas être supprimée
     * → Cela garantit l'intégrité des données
     * → Évite les rôles avec des permissions inexistantes
     *
     * 🔴 EXCEPTIONS :
     * - PermissionNotFoundException
     * - PermissionUtiliseException
     */
    @Override
    @Transactional
    public void supprimerPermission(Long id) {
        log.info("🗑️ Suppression de la permission avec ID : {}", id);

        // ============================================================
        // ÉTAPE 1 : Vérifier que la permission existe
        // ============================================================
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec ID {} non trouvée", id);
                    return new PermissionNotFoundException("ID: " + id);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ============================================================
        // ÉTAPE 2 : Vérifier que la permission n'est pas utilisée
        // ============================================================
        long count = roleRepository.countRolesByPermissionName(permission.getName());
        if (count > 0) {
            log.warn("❌ Permission {} utilisée par {} rôle(s)", permission.getName(), count);
            throw new PermissionUtiliseException(permission.getName(), count);
        }
        log.debug("✅ Permission non utilisée par des rôles");

        // ============================================================
        // ÉTAPE 3 : Supprimer
        // ============================================================
        permissionRepository.deleteById(id);
        log.info("✅ Permission {} supprimée avec succès", permission.getName());
    }

    // ============================================================
    // 7. MÉTHODES DE VALIDATION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : existeParNom()
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier si une permission existe par son nom
     *
     * Utilisé par :
     * - Le Controller pour vérifier avant la création
     * - L'assignation de permissions à un rôle
     *
     * ✅ RETOUR : boolean (true si la permission existe)
     */
    @Override
    public boolean existeParNom(String name) {
        return permissionRepository.existsByName(name);
    }

    /**
     * ============================================================
     * MÉTHODE : existeParCategoryEtNom()
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier si une combinaison category + name existe
     *
     * Utilisé pour la validation avant la création.
     *
     * ✅ RETOUR : boolean
     */
    @Override
    public boolean existeParCategoryEtNom(String category, String name) {
        return permissionRepository.existsByCategoryAndName(category, name);
    }
}