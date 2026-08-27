package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.CategoryRequestDTO;
import com.formation.usermanagement.dto.CategoryResponseDTO;
import com.formation.usermanagement.dto.CategorySummaryDTO;
import com.formation.usermanagement.service.CategoryService;
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

import java.util.List;

/**
 * ================================================================
 * CONTROLLER POUR LA GESTION DES CATÉGORIES
 * ================================================================
 *
 * 📋 ENDPOINTS DISPONIBLES :
 *
 * CRUD :
 * - GET    /api/categories              → Liste paginée
 * - GET    /api/categories/all          → Liste complète
 * - GET    /api/categories/summary      → Liste résumée (pour formulaires)
 * - GET    /api/categories/{id}         → Détail d'une catégorie
 * - GET    /api/categories/name/{name}  → Détail par nom
 * - POST   /api/categories              → Création
 * - PUT    /api/categories/{id}         → Modification
 * - DELETE /api/categories/{id}         → Suppression
 *
 * RECHERCHE :
 * - GET    /api/categories/search?keyword=xxx → Recherche
 *
 * 🔐 SÉCURITÉ :
 * - CATEGORY_READ   : Consultation
 * - CATEGORY_CREATE : Création
 * - CATEGORY_UPDATE : Modification
 * - CATEGORY_DELETE : Suppression
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Catégories", description = "API pour gérer les catégories de produits")
public class CategoryController {

    private final CategoryService categoryService;

    // ============================================================
    // 1. LISTE PAGINÉE
    // ============================================================

    /**
     * GET /api/categories
     *
     * Récupère une liste paginée de toutes les catégories.
     */
    @GetMapping
    @Operation(summary = "Récupérer toutes les catégories (paginé)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> getAllCategories(
            @Parameter(description = "Paramètres de pagination")
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("📋 Récupération des catégories - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        PageResponseDTO<CategoryResponseDTO> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 2. LISTE COMPLÈTE
    // ============================================================

    /**
     * GET /api/categories/all
     *
     * Récupère toutes les catégories (sans pagination).
     */
    @GetMapping("/all")
    @Operation(summary = "Récupérer toutes les catégories (sans pagination)")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategoriesList() {
        log.info("📋 Récupération de toutes les catégories (sans pagination)");
        List<CategoryResponseDTO> response = categoryService.getAllCategoriesList();
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 3. LISTE RÉSUMÉE (pour formulaires)
    // ============================================================

    /**
     * GET /api/categories/summary
     *
     * Récupère toutes les catégories en version résumée.
     * Utilisé pour les formulaires de sélection (dropdown).
     */
    @GetMapping("/summary")
    @Operation(summary = "Récupérer toutes les catégories (version résumée)")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<List<CategorySummaryDTO>> getAllCategoriesSummary() {
        log.info("📋 Récupération des catégories (version résumée)");
        List<CategorySummaryDTO> response = categoryService.getAllCategoriesSummary();
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 4. RÉCUPÉRATION PAR ID
    // ============================================================

    /**
     * GET /api/categories/{id}
     *
     * Récupère une catégorie par son ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une catégorie par ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie trouvée"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<CategoryResponseDTO> getCategory(
            @Parameter(description = "ID de la catégorie", example = "1")
            @PathVariable Long id) {

        log.info("🔍 Récupération de la catégorie ID : {}", id);
        CategoryResponseDTO response = categoryService.getCategory(id);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 5. RÉCUPÉRATION PAR NOM
    // ============================================================

    /**
     * GET /api/categories/name/{name}
     *
     * Récupère une catégorie par son nom.
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Récupérer une catégorie par son nom")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<CategoryResponseDTO> getCategoryByName(
            @Parameter(description = "Nom de la catégorie", example = "Électronique")
            @PathVariable String name) {

        log.info("🔍 Récupération de la catégorie par nom : {}", name);
        CategoryResponseDTO response = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 6. CRÉATION
    // ============================================================

    /**
     * POST /api/categories
     *
     * Crée une nouvelle catégorie.
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle catégorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Validation échouée"),
            @ApiResponse(responseCode = "409", description = "Catégorie déjà existante")
    })
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryRequestDTO dto) {

        log.info("=== CRÉATION CATÉGORIE ===");
        log.info("📝 Nom : {}", dto.getName());

        CategoryResponseDTO response = categoryService.creerCategory(dto);

        log.info("✅ Catégorie créée avec ID : {}", response.getId());
        log.info("=== FIN CRÉATION CATÉGORIE ===");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ============================================================
    // 7. MISE À JOUR
    // ============================================================

    /**
     * PUT /api/categories/{id}
     *
     * Modifie une catégorie existante.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une catégorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catégorie mise à jour"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé")
    })
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID de la catégorie", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {

        log.info("=== MISE À JOUR CATÉGORIE ID : {} ===", id);
        log.info("📝 Nouveau nom : {}", dto.getName());

        CategoryResponseDTO response = categoryService.updateCategory(id, dto);

        log.info("✅ Catégorie mise à jour : {}", response.getName());
        log.info("=== FIN MISE À JOUR CATÉGORIE ===");

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 8. SUPPRESSION
    // ============================================================

    /**
     * DELETE /api/categories/{id}
     *
     * Supprime une catégorie.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une catégorie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Catégorie supprimée"),
            @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
            @ApiResponse(responseCode = "409", description = "Catégorie utilisée par des produits")
    })
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la catégorie", example = "1")
            @PathVariable Long id) {

        log.info("🗑️ Suppression de la catégorie ID : {}", id);

        categoryService.deleteCategory(id);

        log.info("✅ Catégorie supprimée avec succès");
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // 9. RECHERCHE
    // ============================================================

    /**
     * GET /api/categories/search?keyword=xxx
     *
     * Recherche des catégories par mot-clé.
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher des catégories par mot-clé")
    @PreAuthorize("hasAuthority('CATEGORY_READ')")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> searchCategories(
            @Parameter(description = "Mot-clé à rechercher", example = "Electronique")
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {

        log.info("🔍 Recherche de catégories - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        PageResponseDTO<CategoryResponseDTO> response = categoryService.searchCategories(keyword, pageable);

        log.info("✅ {} catégories trouvées pour '{}'", response.getTotalElements(), keyword);
        return ResponseEntity.ok(response);
    }
}