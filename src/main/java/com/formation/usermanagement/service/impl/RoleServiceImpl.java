package com.formation.usermanagement.service.impl;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.exception.*;
import com.formation.usermanagement.mapper.RoleMapper;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * IMPLÉMENTATION DU SERVICE ROLE
 * ============================================================
 *
 * 🎯 OBJECTIF : Gérer toutes les opérations sur les rôles
 *
 * 📋 CE QUE FAIT CE SERVICE :
 * 1. CRUD complet sur les rôles
 * 2. Gestion des permissions associées aux rôles
 * 3. Validation des règles métier
 * 4. Logging des opérations
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    // ============================================================
    // 1. DÉPENDANCES (Injection par constructeur)
    // ============================================================

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // ============================================================
    // 2. CRÉATION D'UN RÔLE
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : creerRole()
     * ============================================================
     *
     * 🎯 OBJECTIF : Créer un nouveau rôle
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le nom n'existe pas déjà
     * 2. Convertir DTO → Entité
     * 3. Ajouter les permissions si présentes
     * 4. Sauvegarder en base
     * 5. Retourner le DTO
     */
    @Override
    @Transactional
    @CacheEvict(value = "roles",allEntries = true)
    public RoleDTO creerRole(RoleRequestDTO dto) {
        log.info("=== DÉBUT création rôle ===");
        log.info("📝 Nom du rôle : {}", dto.getName());

        // ÉTAPE 1 : Vérifier l'unicité du nom
        if (roleRepository.existsByName(dto.getName())) {
            log.warn("❌ Le rôle {} existe déjà", dto.getName());
            throw new RoleDejaExistantException(dto.getName());
        }
        log.info("✅ Nom disponible");

        // ÉTAPE 2 : Convertir DTO → Entité
        Role role = RoleMapper.toEntity(dto);
        log.debug("📦 Entité créée : {}", role.getName());

        // ÉTAPE 3 : Ajouter les permissions si présentes
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            log.debug("🔍 Ajout de {} permission(s)", dto.getPermissionIds().size());
            for (Long permissionId : dto.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> {
                            log.warn("❌ Permission ID {} non trouvée", permissionId);
                            return new PermissionNotFoundException("ID: " + permissionId);
                        });
                role.addPermission(permission);
                log.debug("✅ Permission ajoutée : {}", permission.getName());
            }
        }

        // ÉTAPE 4 : Sauvegarder en base
        Role saved = roleRepository.save(role);
        log.info("✅ Rôle sauvegardé avec ID : {}", saved.getId());

        // ÉTAPE 5 : Retourner le DTO
        log.info("=== FIN création rôle (succès) ===");
        return RoleMapper.toDTO(saved);
    }

    // ============================================================
    // 3. RÉCUPÉRATION D'UN RÔLE
    // ============================================================

    /**
     * Récupère un rôle par son ID
     */
    @Override
    @Cacheable(value = "roles",key = "#id")
    public RoleDTO getRole(Long id) {
        log.debug("🔍 Récupération du rôle avec ID : {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec ID {} non trouvé", id);
                    return new RoleNotFoundException("ID: " + id);
                });

        return RoleMapper.toDTO(role);
    }

    /**
     * Récupère un rôle par son nom
     */
    @Override
    @Cacheable(value = "roles",key = "#name")
    public RoleDTO getRoleByName(String name) {
        log.debug("🔍 Récupération du rôle avec nom : {}", name);

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec nom {} non trouvé", name);
                    return new RoleNotFoundException(name);
                });

        return RoleMapper.toDTO(role);
    }

    // ============================================================
    // 4. LISTES DE RÔLES
    // ============================================================

    /**
     * Récupère tous les rôles avec pagination
     */
    @Override
    public PageResponseDTO<RoleDTO> getAllRoles(Pageable pageable) {
        log.info("📋 Récupération des rôles - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Role> page = roleRepository.findAll(pageable);
        Page<RoleDTO> mappedPage = page.map(RoleMapper::toDTO);

        return new PageResponseDTO<>(mappedPage);
    }

    /**
     * Récupère tous les rôles (sans pagination)
     */
    @Override
    public List<RoleDTO> getAllRolesList() {
        log.debug("📋 Récupération de tous les rôles (sans pagination)");

        List<Role> roles = roleRepository.findAllByOrderByNameAsc();
        return roles.stream()
                .map(RoleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 5. MISE À JOUR D'UN RÔLE
    // ============================================================

    /**
     * Met à jour un rôle existant
     */
    @Override
    @Transactional
    @CacheEvict(value = "roles",allEntries = true)
    public RoleDTO updateRole(Long id, RoleRequestDTO dto) {
        log.info("=== DÉBUT mise à jour rôle ID : {} ===", id);

        // ÉTAPE 1 : Vérifier que le rôle existe
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec ID {} non trouvé", id);
                    return new RoleNotFoundException("ID: " + id);
                });
        log.debug("✅ Rôle trouvé : {}", role.getName());

        // ÉTAPE 2 : Vérifier que le nouveau nom n'est pas utilisé
        roleRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("❌ Le nom {} est déjà utilisé par un autre rôle", dto.getName());
                        throw new RoleDejaExistantException(dto.getName());
                    }
                });
        log.debug("✅ Nom disponible");

        // ÉTAPE 3 : Mettre à jour les champs
        RoleMapper.updateEntity(dto, role);
        log.debug("📝 Rôle mis à jour : {}", role.getName());

        // ÉTAPE 4 : Gérer les permissions si fournies
        if (dto.getPermissionIds() != null) {
            // Vider les permissions actuelles
            role.getPermissions().clear();
            // Ajouter les nouvelles permissions
            for (Long permissionId : dto.getPermissionIds()) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> {
                            log.warn("❌ Permission ID {} non trouvée", permissionId);
                            return new PermissionNotFoundException("ID: " + permissionId);
                        });
                role.addPermission(permission);
            }
            log.debug("✅ {} permission(s) mises à jour", dto.getPermissionIds().size());
        }

        // ÉTAPE 5 : Sauvegarder
        Role saved = roleRepository.save(role);
        log.info("✅ Rôle {} mis à jour avec succès", saved.getName());
        log.info("=== FIN mise à jour rôle (succès) ===");

        return RoleMapper.toDTO(saved);
    }

    // ============================================================
    // 6. SUPPRESSION D'UN RÔLE
    // ============================================================

    /**
     * Supprime un rôle
     */
    @Override
    @Transactional
    @CacheEvict(value = "roles",allEntries = true)
    public void supprimerRole(Long id) {
        log.info("🗑️ Suppression du rôle avec ID : {}", id);

        // ÉTAPE 1 : Vérifier que le rôle existe
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec ID {} non trouvé", id);
                    return new RoleNotFoundException("ID: " + id);
                });
        log.debug("✅ Rôle trouvé : {}", role.getName());

        // ÉTAPE 2 : Vérifier qu'il n'est pas utilisé
        long count = roleRepository.countUsersWithRole(role.getName());
        if (count > 0) {
            log.warn("❌ Rôle {} utilisé par {} utilisateur(s)", role.getName(), count);
            throw new RoleUtiliseException(role.getName(), count);
        }
        log.debug("✅ Rôle non utilisé");

        // ÉTAPE 3 : Supprimer
        roleRepository.deleteById(id);
        log.info("✅ Rôle {} supprimé avec succès", role.getName());
    }

    // ============================================================
    // 7. GESTION DES PERMISSIONS
    // ============================================================

    /**
     * Ajoute une permission à un rôle
     */
    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)  // ← AJOUTER
    public void ajouterPermission(Long roleId, Long permissionId) {
        log.info("📋 Ajout de la permission ID {} au rôle ID {}", permissionId, roleId);

        // ÉTAPE 1 : Vérifier que le rôle existe
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec ID {} non trouvé", roleId);
                    return new RoleNotFoundException("ID: " + roleId);
                });
        log.debug("✅ Rôle trouvé : {}", role.getName());

        // ÉTAPE 2 : Vérifier que la permission existe
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec ID {} non trouvée", permissionId);
                    return new PermissionNotFoundException("ID: " + permissionId);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ÉTAPE 3 : Vérifier que la permission n'est pas déjà présente
        if (role.getPermissions().contains(permission)) {
            log.warn("❌ La permission {} est déjà dans le rôle {}", permission.getName(), role.getName());
            throw new PermissionDejaAssignéeException("La permission " + permission.getName() +
                    " est déjà assignée au rôle " + role.getName());
        }

        // ÉTAPE 4 : Ajouter la permission
        role.addPermission(permission);
        roleRepository.save(role);
        log.info("✅ Permission {} ajoutée au rôle {}", permission.getName(), role.getName());
    }

    /**
     * Retire une permission d'un rôle
     */
    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true)  // ← AJOUTER

    public void retirerPermission(Long roleId, Long permissionId) {
        log.info("🗑️ Retrait de la permission ID {} du rôle ID {}", permissionId, roleId);

        // ÉTAPE 1 : Vérifier que le rôle existe
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("❌ Rôle avec ID {} non trouvé", roleId);
                    return new RoleNotFoundException("ID: " + roleId);
                });
        log.debug("✅ Rôle trouvé : {}", role.getName());

        // ÉTAPE 2 : Vérifier que la permission existe
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> {
                    log.warn("❌ Permission avec ID {} non trouvée", permissionId);
                    return new PermissionNotFoundException("ID: " + permissionId);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ÉTAPE 3 : Vérifier que la permission est présente
        if (!role.getPermissions().contains(permission)) {
            log.warn("❌ La permission {} n'est pas dans le rôle {}", permission.getName(), role.getName());
            throw new PermissionNonAssignéeException("La permission " + permission.getName() +
                    " n'est pas assignée au rôle " + role.getName());
        }

        // ÉTAPE 4 : Retirer la permission
        role.removePermission(permission);
        roleRepository.save(role);
        log.info("✅ Permission {} retirée du rôle {}", permission.getName(), role.getName());
    }

    // ============================================================
    // 8. MÉTHODES DE VALIDATION
    // ============================================================

    /**
     * Vérifie si un nom de rôle existe déjà
     */
    @Override
    public boolean existeParNom(String name) {
        return roleRepository.existsByName(name);
    }

    /**
     * Compte le nombre d'utilisateurs ayant un rôle
     */
    @Override
    public long countUtilisateursByRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("ID: " + roleId));
        return roleRepository.countUsersWithRole(role.getName());
    }
}