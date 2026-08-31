package com.formation.usermanagement.repository;

import com.formation.usermanagement.annotation.TrackMetrics;
import com.formation.usermanagement.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ================================================================
 * REPOSITORY PRODUCT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce repository gère l'accès aux données des produits.
 *
 * 📋 MÉTHODES DISPONIBLES :
 * - findByCategoryId(Long) : Produits par catégorie
 * - findByPriceBetween(BigDecimal, BigDecimal) : Produits dans une fourchette de prix
 * - findByNameContainingIgnoreCase(String) : Recherche par mot-clé
 * - findByInStockTrue() : Produits en stock
 * - findTop10ByOrderByCreatedAtDesc() : Derniers produits
 * - findByIdWithCategory(Long) : Produit avec catégorie (JOIN FETCH)
 * - countByCategoryId(Long) : Nombre de produits par catégorie
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ============================================================
    // 1. MÉTHODES DE RECHERCHE DE BASE
    // ============================================================

    /**
     * Trouve les produits d'une catégorie spécifique.
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Trouve les produits d'une catégorie avec pagination.
     */
    @TrackMetrics
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Trouve les produits dans une fourchette de prix.
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Trouve les produits dont le prix est supérieur à un montant.
     */
    List<Product> findByPriceGreaterThan(BigDecimal price);

    /**
     * Trouve les produits triés par prix décroissant.
     */
    List<Product> findAllByOrderByPriceDesc();

    // ============================================================
    // 2. MÉTHODES DE RECHERCHE AVEC CRITÈRES
    // ============================================================

    /**
     * Recherche des produits par mot-clé (dans le nom ou la description).
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Recherche des produits par mot-clé avec pagination.
     */
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * Recherche des produits par nom ou description avec pagination.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ============================================================
    // 3. MÉTHODES DE STATUT
    // ============================================================

    /**
     * Trouve les produits en stock (quantité > 0).
     */
    List<Product> findByQuantityGreaterThan(Integer quantity);

    /**
     * Trouve les produits en stock avec pagination.
     */
    Page<Product> findByQuantityGreaterThan(Integer quantity, Pageable pageable);

    /**
     * Compte les produits en stock.
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.quantity > 0")
    long countInStock();

    // ============================================================
    // 4. MÉTHODES AVEC JOIN FETCH (Optimisation N+1)
    // ============================================================

    /**
     * Trouve un produit avec sa catégorie en une seule requête.
     *
     * ⚠️ Utilise JOIN FETCH pour éviter le problème N+1.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    /**
     * Trouve tous les produits avec leurs catégories.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    /**
     * Trouve les produits d'une catégorie avec la catégorie.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);

    // ============================================================
    // 5. MÉTHODES DE STATISTIQUES
    // ============================================================

    /**
     * Compte le nombre de produits dans une catégorie.
     */
    long countByCategoryId(Long categoryId);

    /**
     * Calcule le prix moyen des produits.
     */
    @Query("SELECT AVG(p.price) FROM Product p")
    Double getAveragePrice();

    /**
     * Calcule le prix total de tous les produits (stock * prix).
     */
    @Query("SELECT SUM(p.price * p.quantity) FROM Product p")
    BigDecimal getTotalStockValue();

    /**
     * Trouve les produits les plus chers.
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findTop10MostExpensive();

    /**
     * Trouve les produits les plus vendus (par quantité).
     */
    @Query("SELECT p FROM Product p ORDER BY p.quantity DESC")
    List<Product> findTop10MostStocked();

    List<Product> findTop10ByOrderByQuantityDesc();  // ← Spring Data le fait automatiquement


    // ============================================================
    // 6. MÉTHODES DE MODIFICATION (Modifying)
    // ============================================================

    /**
     * Met à jour le stock d'un produit.
     */
    @Query("UPDATE Product p SET p.quantity = :quantity WHERE p.id = :id")
    int updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * Réduit le stock d'un produit.
     */
    @Query("UPDATE Product p SET p.quantity = p.quantity - :amount WHERE p.id = :id AND p.quantity >= :amount")
    int reduceStock(@Param("id") Long id, @Param("amount") Integer amount);
}