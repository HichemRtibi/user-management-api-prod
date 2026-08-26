package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.permision.PermissionRequestDTO;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * SERVICE PERMISSION
 *
 * 🎯 OBJECTIF : Gérer toutes les opérations sur les permissions
 *
 * 📋 MÉTHODES DISPONIBLES :
 *
 * 1. CRUD :
 *    - creerPermission()    : Créer une nouvelle permission
 *    - getPermission()      : Récupérer par ID
 *    - getPermissionByName(): Récupérer par nom
 *    - updatePermission()   : Modifier une permission
 *    - supprimerPermission(): Supprimer une permission
 *
 * 2. LISTES :
 *    - getAllPermissions()      : Liste paginée
 *    - getAllPermissionsList()  : Liste complète (sans pagination)
 *    - getPermissionsByCategory(): Liste par catégorie
 *
 * 3. VALIDATION :
 *    - existeParNom()              : Vérifier l'existence par nom
 *    - existeParCategoryEtNom()    : Vérifier la combinaison
 *
 * ⚠️ RÈGLES MÉTIER :
 * - Le nom d'une permission doit être unique
 * - La combinaison (category, name) doit être unique
 * - Une permission utilisée par des rôles ne peut pas être supprimée
 */
public interface PermissionService {

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    /**
     * Crée une nouvelle permission.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que le nom n'existe pas déjà
     * 2. Vérifier que la combinaison category+name n'existe pas
     * 3. Convertir DTO → Entité
     * 4. Sauvegarder en base
     * 5. Retourner le DTO
     *
     * @param dto Les données de la permission
     * @return La permission créée
     * @throws // PermissionDejaExistantException si le nom existe déjà
     */
    PermissionDTO creerPermission(@Valid PermissionRequestDTO dto);

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    /**
     * Récupère une permission par son ID.
     *
     * @param id L'ID de la permission
     * @return La permission trouvée
     * @throws // PermissionNotFoundException si la permission n'existe pas
     */
    PermissionDTO getPermission(Long id);

    /**
     * Récupère une permission par son nom.
     *
     * @param name Le nom de la permission (ex: "USER_READ")
     * @return La permission trouvée
     * @throws // PermissionNotFoundException si la permission n'existe pas
     */
    PermissionDTO getPermissionByName(String name);

    // ============================================================
    // 3. LISTES
    // ============================================================

    /**
     * Récupère toutes les permissions avec pagination.
     *
     * @param pageable Les paramètres de pagination
     * @return PageResponseDTO avec les permissions et métadonnées
     */
    PageResponseDTO<PermissionDTO> getAllPermissions(Pageable pageable);

    /**
     * Récupère toutes les permissions (sans pagination).
     *
     * @return Liste de toutes les permissions
     */
    List<PermissionDTO> getAllPermissionsList();

    /**
     * Récupère les permissions d'une catégorie spécifique.
     *
     * @param category La catégorie (ex: "USER")
     * @return Liste des permissions de cette catégorie
     */
    List<PermissionDTO> getPermissionsByCategory(String category);

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    /**
     * Met à jour une permission existante.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que la permission existe
     * 2. Vérifier que le nouveau nom n'est pas utilisé par une autre permission
     * 3. Vérifier la nouvelle combinaison category+name
     * 4. Mettre à jour les champs
     * 5. Sauvegarder
     * 6. Retourner le DTO
     *
     * @param id L'ID de la permission
     * @param dto Les nouvelles données
     * @return La permission mise à jour
     */
    PermissionDTO updatePermission(Long id, PermissionRequestDTO dto);

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    /**
     * Supprime une permission.
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que la permission existe
     * 2. Vérifier qu'elle n'est pas utilisée par des rôles
     * 3. Supprimer
     *
     * @param id L'ID de la permission
     * @throws // PermissionNotFoundException si la permission n'existe pas
     * @throws // PermissionUtiliseException si la permission est utilisée
     */
    void supprimerPermission(Long id);

    // ============================================================
    // 6. VALIDATION
    // ============================================================

    /**
     * Vérifie si une permission existe par son nom.
     *
     * @param name Le nom de la permission
     * @return true si la permission existe, false sinon
     */
    boolean existeParNom(String name);

    /**
     * Vérifie si une combinaison category + name existe.
     *
     * @param category La catégorie
     * @param name Le nom
     * @return true si la combinaison existe, false sinon
     */
    boolean existeParCategoryEtNom(String category, String name);
}