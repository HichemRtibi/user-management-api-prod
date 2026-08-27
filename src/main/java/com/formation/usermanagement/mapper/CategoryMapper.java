package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.CategoryRequestDTO;
import com.formation.usermanagement.dto.CategoryResponseDTO;
import com.formation.usermanagement.dto.CategorySummaryDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.entity.Product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ================================================================
 * MAPPER POUR L'ENTITÉ CATEGORY
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce mapper convertit les entités Category en DTOs et vice-versa.
 *
 * 📋 MÉTHODES DISPONIBLES :
 * - toEntity(CategoryRequestDTO) : DTO → Entité (création)
 * - updateEntity(CategoryRequestDTO, Category) : DTO → Entité (mise à jour)
 * - toResponseDTO(Category) : Entité → DTO (réponse complète)
 * - toSummaryDTO(Category) : Entité → DTO (version légère)
 * - toSummaryDTOList(List<Category>) : Liste Entités → Liste DTOs
 */
public class CategoryMapper {

    // ============================================================
    // 1. DTO → ENTITÉ (Création)
    // ============================================================

    /**
     * Convertit un CategoryRequestDTO en entité Category.
     * Utilisé pour la création d'une nouvelle catégorie.
     */
    public static Category toEntity(CategoryRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    // ============================================================
    // 2. DTO → ENTITÉ (Mise à jour)
    // ============================================================

    /**
     * Met à jour une entité Category existante avec les données d'un DTO.
     * Utilisé pour la modification d'une catégorie existante.
     */
    public static void updateEntity(CategoryRequestDTO dto, Category category) {
        if (dto == null || category == null) {
            return;
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
    }

    // ============================================================
    // 3. ENTITÉ → DTO (Réponse complète)
    // ============================================================

    /**
     * Convertit une entité Category en CategoryResponseDTO.
     * Utilisé pour les réponses GET détaillées.
     *
     * ⚠️ Avec les produits (version complète)
     */
    public static CategoryResponseDTO toResponseDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .products(toProductSummaryList(category.getProducts()))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    // ============================================================
    // 4. ENTITÉ → DTO (Version légère pour les listes)
    // ============================================================

    /**
     * Convertit une entité Category en CategorySummaryDTO.
     * Utilisé pour les listes et les relations.
     *
     * ⚠️ Sans les produits (version légère)
     */
    public static CategorySummaryDTO toSummaryDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategorySummaryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    // ============================================================
    // 5. LISTE ENTITÉS → LISTE DTOS
    // ============================================================

    /**
     * Convertit une liste de Category en liste de CategoryResponseDTO.
     */
    public static List<CategoryResponseDTO> toResponseDTOList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }

        return categories.stream()
                .map(CategoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de Category en liste de CategorySummaryDTO.
     */
    public static List<CategorySummaryDTO> toSummaryDTOList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }

        return categories.stream()
                .map(CategoryMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 6. MÉTHODES PRIVÉES UTILITAIRES
    // ============================================================

    /**
     * Convertit une liste de Product en liste de ProductSummaryDTO.
     * Utilisé pour inclure les produits dans la réponse d'une catégorie.
     */
    private static List<ProductSummaryDTO> toProductSummaryList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(product -> ProductSummaryDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .quantity(product.getQuantity())
                        .imageUrl(product.getImageUrl())
                        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }
}