package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.ProductRequestDTO;
import com.formation.usermanagement.dto.ProductResponseDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * ================================================================
 * SERVICE PRODUCT - INTERFACE
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette interface définit toutes les opérations possibles sur les produits.
 *
 * 📋 MÉTHODES DISPONIBLES :
 *
 * 1. CRUD :
 *    - creerProduct(ProductRequestDTO) : Créer un produit
 *    - getProduct(Long) : Récupérer par ID
 *    - updateProduct(Long, ProductRequestDTO) : Modifier un produit
 *    - deleteProduct(Long) : Supprimer un produit
 *
 * 2. LISTES :
 *    - getAllProducts(Pageable) : Liste paginée
 *    - getAllProductsList() : Liste complète
 *
 * 3. RECHERCHE :
 *    - searchProducts(String, Pageable) : Recherche par mot-clé
 *    - getProductsByCategory(Long, Pageable) : Produits par catégorie
 *    - getProductsByPriceRange(BigDecimal, BigDecimal) : Produits par prix
 *    - getProductsInStock(Pageable) : Produits en stock
 *
 * 4. STATISTIQUES :
 *    - countInStock() : Nombre de produits en stock
 *    - getAveragePrice() : Prix moyen
 *    - getTotalStockValue() : Valeur totale du stock
 */
public interface ProductService {

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    /**
     * Crée un nouveau produit.
     *
     * @param dto Les données du produit
     * @return Le produit créé
     * @throws // CategoryNotFoundException si la catégorie n'existe pas
     */
    ProductResponseDTO creerProduct(ProductRequestDTO dto);

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    /**
     * Récupère un produit par son ID.
     */
    ProductResponseDTO getProduct(Long id);

    // ============================================================
    // 3. LISTES
    // ============================================================

    /**
     * Récupère tous les produits avec pagination.
     */
    PageResponseDTO<ProductResponseDTO> getAllProducts(Pageable pageable);

    /**
     * Récupère tous les produits (sans pagination).
     */
    List<ProductSummaryDTO> getAllProductsList();

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    /**
     * Met à jour un produit existant.
     */
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto);

    /**
     * Met à jour le stock d'un produit.
     */
    ProductResponseDTO updateStock(Long id, Integer quantity);

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    /**
     * Supprime un produit.
     */
    void deleteProduct(Long id);

    // ============================================================
    // 6. RECHERCHE
    // ============================================================

    /**
     * Recherche des produits par mot-clé.
     */
    PageResponseDTO<ProductResponseDTO> searchProducts(String keyword, Pageable pageable);

    /**
     * Récupère les produits d'une catégorie.
     */
    PageResponseDTO<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    /**
     * Récupère les produits dans une fourchette de prix.
     */
    List<ProductResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Récupère les produits en stock.
     */
    PageResponseDTO<ProductResponseDTO> getProductsInStock(Pageable pageable);

    // ============================================================
    // 7. STATISTIQUES
    // ============================================================

    /**
     * Compte le nombre de produits en stock.
     */
    long countInStock();

    /**
     * Calcule le prix moyen des produits.
     */
    double getAveragePrice();

    /**
     * Calcule la valeur totale du stock.
     */
    BigDecimal getTotalStockValue();
}