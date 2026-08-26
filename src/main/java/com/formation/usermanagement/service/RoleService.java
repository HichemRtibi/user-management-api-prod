package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * SERVICE ROLE
 *
 * 🎯 OBJECTIF : Gérer toutes les opérations sur les rôles
 *
 * 📋 MÉTHODES DISPONIBLES :
 *
 * 1. CRUD :
 *    - creerRole()       : Créer un nouveau rôle
 *    - getRole()         : Récupérer par ID
 *    - getRoleByName()   : Récupérer par nom
 *    - updateRole()      : Modifier un rôle
 *    - supprimerRole()   : Supprimer un rôle
 *
 * 2. GESTION DES PERMISSIONS :
 *    - ajouterPermission()   : Ajouter une permission à un rôle
 *    - retirerPermission()   : Retirer une permission d'un rôle
 *
 * 3. LISTES :
 *    - getAllRoles()         : Liste paginée
 *    - getAllRolesList()     : Liste complète (sans pagination)
 *
 * 4. VALIDATION :
 *    - existeParNom()           : Vérifier l'existence par nom
 *    - countUtilisateursByRole(): Compter les utilisateurs d'un rôle
 *
 * ⚠️ RÈGLES MÉTIER :
 * - Le nom d'un rôle doit être unique
 * - Un rôle utilisé par des utilisateurs ne peut pas être supprimé
 * - Une permission ne peut pas être ajoutée deux fois au même rôle
 */
public interface RoleService {

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    /**
     * Crée un nouveau rôle.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le nom n'existe pas déjà
     * 2. Convertir DTO → Entité
     * 3. Ajouter les permissions si présentes
     * 4. Sauvegarder en base
     * 5. Retourner le DTO
     *
     * @param dto Les données du rôle
     * @return Le rôle créé avec ses permissions
     * @throws // RoleDejaExistantException si le nom existe déjà
     */
    RoleDTO creerRole(RoleRequestDTO dto);

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    /**
     * Récupère un rôle par son ID.
     *
     * @param id L'ID du rôle
     * @return Le rôle trouvé
     * @throws // RoleNotFoundException si le rôle n'existe pas
     */
    RoleDTO getRole(Long id);

    /**
     * Récupère un rôle par son nom.
     *
     * @param name Le nom du rôle (ex: "ROLE_ADMIN")
     * @return Le rôle trouvé
     * @throws // RoleNotFoundException si le rôle n'existe pas
     */
    RoleDTO getRoleByName(String name);

    // ============================================================
    // 3. LISTES
    // ============================================================

    /**
     * Récupère tous les rôles avec pagination.
     *
     * @param pageable Les paramètres de pagination
     * @return PageResponseDTO avec les rôles et métadonnées
     */
    PageResponseDTO<RoleDTO> getAllRoles(Pageable pageable);

    /**
     * Récupère tous les rôles (sans pagination).
     *
     * @return Liste de tous les rôles
     */
    List<RoleDTO> getAllRolesList();

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    /**
     * Met à jour un rôle existant.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le rôle existe
     * 2. Vérifier que le nouveau nom n'est pas utilisé par un autre rôle
     * 3. Mettre à jour les champs
     * 4. Sauvegarder
     *
     * @param id L'ID du rôle
     * @param dto Les nouvelles données
     * @return Le rôle mis à jour
     * @throws // RoleNotFoundException si le rôle n'existe pas
     */
    RoleDTO updateRole(Long id, RoleRequestDTO dto);

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    /**
     * Supprime un rôle.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le rôle existe
     * 2. Vérifier qu'il n'est pas utilisé par des utilisateurs
     * 3. Supprimer
     *
     * @param id L'ID du rôle
     * @throws  // RoleNotFoundException si le rôle n'existe pas
     * @throws // RoleUtiliseException si le rôle est utilisé par des utilisateurs
     */
    void supprimerRole(Long id);

    // ============================================================
    // 6. GESTION DES PERMISSIONS
    // ============================================================

    /**
     * Ajoute une permission à un rôle.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le rôle existe
     * 2. Vérifier que la permission existe
     * 3. Vérifier que le rôle n'a pas déjà cette permission
     * 4. Ajouter la permission
     *
     * @param roleId L'ID du rôle
     * @param permissionId L'ID de la permission
     */
    void ajouterPermission(Long roleId, Long permissionId);

    /**
     * Retire une permission d'un rôle.
     *
     * @param roleId L'ID du rôle
     * @param permissionId L'ID de la permission
     */
    void retirerPermission(Long roleId, Long permissionId);

    // ============================================================
    // 7. VALIDATION
    // ============================================================

    /**
     * Vérifie si un nom de rôle existe déjà.
     *
     * @param name Le nom du rôle
     * @return true si le rôle existe, false sinon
     */
    boolean existeParNom(String name);

    /**
     * Compte le nombre d'utilisateurs ayant un rôle spécifique.
     *
     * @param roleId L'ID du rôle
     * @return Nombre d'utilisateurs
     */
    long countUtilisateursByRole(Long roleId);
}