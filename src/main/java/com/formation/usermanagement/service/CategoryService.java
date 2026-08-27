package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.CategoryRequestDTO;
import com.formation.usermanagement.dto.CategoryResponseDTO;
import com.formation.usermanagement.dto.CategorySummaryDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * ================================================================
 * SERVICE CATEGORY - INTERFACE
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette interface définit toutes les opérations possibles sur les catégories.
 *
 * 📋 MÉTHODES DISPONIBLES :
 *
 * 1. CRUD :
 *    - creerCategory(CategoryRequestDTO) : Créer une catégorie
 *    - getCategory(Long) : Récupérer par ID
 *    - getCategoryByName(String) : Récupérer par nom
 *    - updateCategory(Long, CategoryRequestDTO) : Modifier une catégorie
 *    - deleteCategory(Long) : Supprimer une catégorie
 *
 * 2. LISTES :
 *    - getAllCategories(Pageable) : Liste paginée
 *    - getAllCategoriesList() : Liste complète (sans pagination)
 *    - getAllCategoriesSummary() : Liste résumée
 *
 * 3. RECHERCHE :
 *    - searchCategories(String, Pageable) : Recherche par mot-clé
 *
 * 4. VALIDATION :
 *    - existeParNom(String) : Vérifier l'existence d'un nom
 */
public interface CategoryService {

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    /**
     * Crée une nouvelle catégorie.
     *
     * @param dto Les données de la catégorie
     * @return La catégorie créée
     * @throws // CategoryDejaExistantException si le nom existe déjà
     */
    CategoryResponseDTO creerCategory(CategoryRequestDTO dto);

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    /**
     * Récupère une catégorie par son ID.
     *
     * @param id L'ID de la catégorie
     * @return La catégorie trouvée
     * @throws // CategoryNotFoundException si la catégorie n'existe pas
     */
    CategoryResponseDTO getCategory(Long id);

    /**
     * Récupère une catégorie par son nom.
     *
     * @param name Le nom de la catégorie
     * @return La catégorie trouvée
     * @throws // CategoryNotFoundException si la catégorie n'existe pas
     */
    CategoryResponseDTO getCategoryByName(String name);

    // ============================================================
    // 3. LISTES
    // ============================================================

    /**
     * Récupère toutes les catégories avec pagination.
     */
    PageResponseDTO<CategoryResponseDTO> getAllCategories(Pageable pageable);

    /**
     * Récupère toutes les catégories (sans pagination).
     */
    List<CategoryResponseDTO> getAllCategoriesList();

    /**
     * Récupère toutes les catégories en version résumée.
     * Utilisé pour les formulaires de sélection.
     */
    List<CategorySummaryDTO> getAllCategoriesSummary();

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    /**
     * Met à jour une catégorie existante.
     */
    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto);

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    /**
     * Supprime une catégorie.
     *
     * @throws // CategoryNotFoundException CategoryUtiliseException si la catégorie contient des produits
     */
    void deleteCategory(Long id);

    // ============================================================
    // 6. RECHERCHE
    // ============================================================

    /**
     * Recherche des catégories par mot-clé.
     */
    PageResponseDTO<CategoryResponseDTO> searchCategories(String keyword, Pageable pageable);

    // ============================================================
    // 7. VALIDATION
    // ============================================================

    /**
     * Vérifie si une catégorie existe par son nom.
     */
    boolean existeParNom(String name);
}