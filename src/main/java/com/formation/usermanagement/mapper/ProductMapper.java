package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.CategorySummaryDTO;
import com.formation.usermanagement.dto.ProductRequestDTO;
import com.formation.usermanagement.dto.ProductResponseDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.entity.Product;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ================================================================
 * MAPPER POUR L'ENTITÉ PRODUCT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Ce mapper convertit les entités Product en DTOs et vice-versa.
 *
 * 📋 MÉTHODES DISPONIBLES :
 * - toEntity(ProductRequestDTO) : DTO → Entité (création)
 * - updateEntity(ProductRequestDTO, Product) : DTO → Entité (mise à jour)
 * - toResponseDTO(Product) : Entité → DTO (réponse complète)
 * - toSummaryDTO(Product) : Entité → DTO (version légère)
 * - toSummaryDTOList(List<Product>) : Liste Entités → Liste DTOs
 */
public class ProductMapper {

    // ============================================================
    // 1. DTO → ENTITÉ (Création)
    // ============================================================

    /**
     * Convertit un ProductRequestDTO en entité Product.
     * Utilisé pour la création d'un nouveau produit.
     *
     * ⚠️ La catégorie doit être définie séparément.
     */
    public static Product toEntity(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }

    // ============================================================
    // 2. DTO → ENTITÉ (Mise à jour)
    // ============================================================

    /**
     * Met à jour une entité Product existante avec les données d'un DTO.
     * Utilisé pour la modification d'un produit existant.
     *
     * ⚠️ La catégorie est mise à jour séparément.
     */
    public static void updateEntity(ProductRequestDTO dto, Product product) {
        if (dto == null || product == null) {
            return;
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setImageUrl(dto.getImageUrl());
    }

    // ============================================================
    // 3. ENTITÉ → DTO (Réponse complète)
    // ============================================================

    /**
     * Convertit une entité Product en ProductResponseDTO.
     * Utilisé pour les réponses GET détaillées.
     *
     * ⚠️ Avec la catégorie (version complète)
     */
    public static ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .imageUrl(product.getImageUrl())
                .inStock(product.isInStock())
                .category(toCategorySummaryDTO(product.getCategory()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // ============================================================
    // 4. ENTITÉ → DTO (Version légère pour les listes)
    // ============================================================

    /**
     * Convertit une entité Product en ProductSummaryDTO.
     * Utilisé pour les listes et les relations.
     *
     * ⚠️ Sans la catégorie complète (version légère)
     */
    public static ProductSummaryDTO toSummaryDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSummaryDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .build();
    }

    // ============================================================
    // 5. LISTE ENTITÉS → LISTE DTOS
    // ============================================================

    /**
     * Convertit une liste de Product en liste de ProductResponseDTO.
     */
    public static List<ProductResponseDTO> toResponseDTOList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de Product en liste de ProductSummaryDTO.
     */
    public static List<ProductSummaryDTO> toSummaryDTOList(List<Product> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(ProductMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // 6. MÉTHODES PRIVÉES UTILITAIRES
    // ============================================================

    /**
     * Convertit une entité Category en CategorySummaryDTO.
     * Utilisé pour inclure la catégorie dans la réponse d'un produit.
     */
    private static CategorySummaryDTO toCategorySummaryDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategorySummaryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}