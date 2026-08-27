package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.ProductRequestDTO;
import com.formation.usermanagement.dto.ProductResponseDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import com.formation.usermanagement.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * ================================================================
 * CONTROLLER POUR LA GESTION DES PRODUITS
 * ================================================================
 *
 * 📋 ENDPOINTS DISPONIBLES :
 *
 * CRUD :
 * - GET    /api/products                   → Liste paginée
 * - GET    /api/products/all               → Liste complète
 * - GET    /api/products/{id}              → Détail d'un produit
 * - POST   /api/products                   → Création
 * - PUT    /api/products/{id}              → Modification
 * - PATCH  /api/products/{id}/stock        → Mise à jour du stock
 * - DELETE /api/products/{id}              → Suppression
 *
 * RECHERCHE :
 * - GET    /api/products/search?keyword=xxx    → Recherche
 * - GET    /api/products/category/{categoryId} → Par catégorie
 * - GET    /api/products/price-range?min=10&max=100 → Par prix
 * - GET    /api/products/in-stock              → En stock
 *
 * STATISTIQUES :
 * - GET    /api/products/stats/count-in-stock → Nombre en stock
 * - GET    /api/products/stats/average-price  → Prix moyen
 * - GET    /api/products/stats/total-value    → Valeur totale du stock
 *
 * 🔐 SÉCURITÉ :
 * - PRODUCT_READ   : Consultation
 * - PRODUCT_CREATE : Création
 * - PRODUCT_UPDATE : Modification
 * - PRODUCT_DELETE : Suppression
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Produits", description = "API pour gérer les produits")
public class ProductController {

    private final ProductService productService;

    // ============================================================
    // 1. LISTE PAGINÉE
    // ============================================================

    @GetMapping
    @Operation(summary = "Récupérer tous les produits (paginé)")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getAllProducts(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des produits - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        PageResponseDTO<ProductResponseDTO> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 2. LISTE COMPLÈTE
    // ============================================================

    @GetMapping("/all")
    @Operation(summary = "Récupérer tous les produits (sans pagination)")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductSummaryDTO>> getAllProductsList() {
        log.info("📋 Récupération de tous les produits (sans pagination)");
        List<ProductSummaryDTO> response = productService.getAllProductsList();
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 3. RÉCUPÉRATION PAR ID
    // ============================================================

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit trouvé"),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductResponseDTO> getProduct(
            @Parameter(description = "ID du produit", example = "1")
            @PathVariable Long id) {

        log.info("🔍 Récupération du produit ID : {}", id);
        ProductResponseDTO response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 4. CRÉATION
    // ============================================================

    @PostMapping
    @Operation(summary = "Créer un nouveau produit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO dto) {

        log.info("=== CRÉATION PRODUIT ===");
        log.info("📝 Nom : {}", dto.getName());
        log.info("💰 Prix : {}", dto.getPrice());

        ProductResponseDTO response = productService.creerProduct(dto);

        log.info("✅ Produit créé avec ID : {}", response.getId());
        log.info("=== FIN CRÉATION PRODUIT ===");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // 5. MISE À JOUR
    // ============================================================

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un produit")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID du produit", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto) {

        log.info("=== MISE À JOUR PRODUIT ID : {} ===", id);
        log.info("📝 Nouveau nom : {}", dto.getName());

        ProductResponseDTO response = productService.updateProduct(id, dto);

        log.info("✅ Produit mis à jour : {}", response.getName());
        log.info("=== FIN MISE À JOUR PRODUIT ===");

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 6. MISE À JOUR DU STOCK
    // ============================================================

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Mettre à jour le stock d'un produit")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductResponseDTO> updateStock(
            @Parameter(description = "ID du produit", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nouvelle quantité", example = "15")
            @RequestParam Integer quantity) {

        log.info("📦 Mise à jour du stock - Produit ID: {}, Quantité: {}", id, quantity);

        ProductResponseDTO response = productService.updateStock(id, quantity);

        log.info("✅ Stock mis à jour : {}", response.getQuantity());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 7. SUPPRESSION
    // ============================================================

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID du produit", example = "1")
            @PathVariable Long id) {

        log.info("🗑️ Suppression du produit ID : {}", id);

        productService.deleteProduct(id);

        log.info("✅ Produit supprimé avec succès");
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // 8. RECHERCHE PAR MOT-CLÉ
    // ============================================================

    @GetMapping("/search")
    @Operation(summary = "Rechercher des produits par mot-clé")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> searchProducts(
            @Parameter(description = "Mot-clé à rechercher", example = "iPhone")
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("🔍 Recherche de produits - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        PageResponseDTO<ProductResponseDTO> response = productService.searchProducts(keyword, pageable);

        log.info("✅ {} produits trouvés pour '{}'", response.getTotalElements(), keyword);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 9. PRODUITS PAR CATÉGORIE
    // ============================================================

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Récupérer les produits d'une catégorie")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getProductsByCategory(
            @Parameter(description = "ID de la catégorie", example = "1")
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des produits de la catégorie ID : {}", categoryId);

        PageResponseDTO<ProductResponseDTO> response = productService.getProductsByCategory(categoryId, pageable);

        log.info("✅ {} produits trouvés", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 10. PRODUITS PAR FOURCHETTE DE PRIX
    // ============================================================

    @GetMapping("/price-range")
    @Operation(summary = "Récupérer les produits dans une fourchette de prix")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPriceRange(
            @Parameter(description = "Prix minimum", example = "10")
            @RequestParam BigDecimal min,
            @Parameter(description = "Prix maximum", example = "100")
            @RequestParam BigDecimal max) {

        log.info("📋 Récupération des produits entre {} et {}", min, max);

        List<ProductResponseDTO> response = productService.getProductsByPriceRange(min, max);

        log.info("✅ {} produits trouvés", response.size());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 11. PRODUITS EN STOCK
    // ============================================================

    @GetMapping("/in-stock")
    @Operation(summary = "Récupérer les produits en stock")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> getProductsInStock(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des produits en stock - Page: {}",
                pageable.getPageNumber());

        PageResponseDTO<ProductResponseDTO> response = productService.getProductsInStock(pageable);

        log.info("✅ {} produits en stock trouvés", response.getTotalElements());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 12. STATISTIQUES
    // ============================================================

    @GetMapping("/stats/count-in-stock")
    @Operation(summary = "Compter les produits en stock")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Long> countInStock() {
        long count = productService.countInStock();
        log.info("📊 Nombre de produits en stock : {}", count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/average-price")
    @Operation(summary = "Calculer le prix moyen des produits")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Double> getAveragePrice() {
        double avg = productService.getAveragePrice();
        log.info("📊 Prix moyen : {}", avg);
        return ResponseEntity.ok(avg);
    }

    @GetMapping("/stats/total-value")
    @Operation(summary = "Calculer la valeur totale du stock")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<BigDecimal> getTotalStockValue() {
        BigDecimal total = productService.getTotalStockValue();
        log.info("📊 Valeur totale du stock : {}", total);
        return ResponseEntity.ok(total);
    }
}