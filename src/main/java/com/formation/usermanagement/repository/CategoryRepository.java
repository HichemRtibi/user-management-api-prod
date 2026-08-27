package com.formation.usermanagement.repository;

import com.formation.usermanagement.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * REPOSITORY CATEGORY
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce repository gère l'accès aux données des catégories.
 *
 * 📋 MÉTHODES DISPONIBLES :
 * - findByName(String) : Recherche par nom
 * - existsByName(String) : Vérification d'existence
 * - findByNameWithProducts(String) : Recherche avec produits (JOIN FETCH)
 * - findAllWithProducts() : Toutes les catégories avec produits
 * - findByNameContainingIgnoreCase(String) : Recherche par mot-clé
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ============================================================
    // 1. MÉTHODES DE RECHERCHE DE BASE
    // ============================================================

    /**
     * Trouve une catégorie par son nom.
     *
     * Utilisé pour :
     * - Vérifier l'unicité du nom
     * - Récupérer une catégorie par son nom
     */
    Optional<Category> findByName(String name);

    /**
     * Vérifie si une catégorie existe par son nom.
     */
    boolean existsByName(String name);

    /**
     * Trouve toutes les catégories triées par nom.
     */
    List<Category> findAllByOrderByNameAsc();

    // ============================================================
    // 2. MÉTHODES AVEC JOIN FETCH (Optimisation N+1)
    // ============================================================

    /**
     * Trouve une catégorie avec ses produits en une seule requête.
     *
     * ⚠️ Utilise JOIN FETCH pour éviter le problème N+1.
     *
     * Utilisé quand on a besoin des produits de la catégorie.
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<Category> findByIdWithProducts(@Param("id") Long id);

    /**
     * Trouve une catégorie avec ses produits par son nom.
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products WHERE c.name = :name")
    Optional<Category> findByNameWithProducts(@Param("name") String name);

    /**
     * Trouve toutes les catégories avec leurs produits.
     *
     * ⚠️ ATTENTION : Peut charger beaucoup de données !
     * À utiliser avec précaution.
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();

    // ============================================================
    // 3. MÉTHODES DE RECHERCHE AVANCÉE
    // ============================================================

    /**
     * Recherche des catégories par mot-clé (dans le nom).
     */
    List<Category> findByNameContainingIgnoreCase(String keyword);

    // ✅ AJOUTER CETTE MÉTHODE (version paginée)
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);


    /**
     * Compte le nombre de produits par catégorie.
     *
     * Utilisé pour les statistiques.
     */
    @Query("SELECT c.id, c.name, COUNT(p) FROM Category c LEFT JOIN c.products p GROUP BY c.id, c.name")
    List<Object[]> countProductsByCategory();
}