package com.formation.usermanagement.service.impl;

import com.formation.usermanagement.annotation.TrackMetrics;
import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.ProductRequestDTO;
import com.formation.usermanagement.dto.ProductResponseDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.exception.CategoryNotFoundException;
import com.formation.usermanagement.exception.ProductNotFoundException;
import com.formation.usermanagement.mapper.ProductMapper;
import com.formation.usermanagement.repository.CategoryRepository;
import com.formation.usermanagement.repository.ProductRepository;
import com.formation.usermanagement.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * ================================================================
 * IMPLÉMENTATION DU SERVICE PRODUCT
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette classe contient toute la logique métier pour la gestion des produits.
 *
 * 🔐 PERMISSIONS UTILISÉES :
 * - PRODUCT_READ   : Consultation
 * - PRODUCT_CREATE : Création
 * - PRODUCT_UPDATE : Modification
 * - PRODUCT_DELETE : Suppression
 *
 * 📦 CACHE :
 * - products : Cache des produits
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    @Override
    @TrackMetrics
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO creerProduct(ProductRequestDTO dto) {
        log.info("=== DÉBUT création produit ===");
        log.info("📝 Nom : {}", dto.getName());
        log.info("💰 Prix : {}", dto.getPrice());

        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("❌ Catégorie avec ID {} non trouvée", dto.getCategoryId());
                    return new CategoryNotFoundException("ID: " + dto.getCategoryId());
                });

        // Convertir DTO → Entité
        Product product = ProductMapper.toEntity(dto);
        product.setCategory(category);

        // Sauvegarder
        Product saved = productRepository.save(product);
        log.info("✅ Produit sauvegardé avec ID : {}", saved.getId());

        log.info("=== FIN création produit (succès) ===");
        return ProductMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    @Override
    @TrackMetrics
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProduct(Long id) {
        log.debug("🔍 Récupération du produit ID : {}", id);

        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> {
                    log.warn("❌ Produit avec ID {} non trouvé", id);
                    return new ProductNotFoundException("ID: " + id);
                });

        log.debug("✅ Produit trouvé : {}", product.getName());
        return ProductMapper.toResponseDTO(product);
    }

    // ============================================================
    // 3. LISTES
    // ============================================================

    @Override
    @TrackMetrics
    public PageResponseDTO<ProductResponseDTO> getAllProducts(Pageable pageable) {
        log.info("📋 Récupération des produits - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> page = productRepository.findAll(pageable);
        Page<ProductResponseDTO> mappedPage = page.map(ProductMapper::toResponseDTO);

        log.info("✅ {} produits récupérés", mappedPage.getNumberOfElements());
        return new PageResponseDTO<>(mappedPage);
    }

    @Override
    @TrackMetrics
    public List<ProductSummaryDTO> getAllProductsList() {
        log.debug("📋 Récupération de tous les produits (sans pagination)");

        List<Product> products = productRepository.findAllWithCategory();
        return ProductMapper.toSummaryDTOList(products);
    }

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    @Override
    @TrackMetrics
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        log.info("=== DÉBUT mise à jour produit ID : {} ===", id);

        // Vérifier que le produit existe
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Produit avec ID {} non trouvé", id);
                    return new ProductNotFoundException("ID: " + id);
                });

        // Vérifier que la catégorie existe (si modifiée)
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("❌ Catégorie avec ID {} non trouvée", dto.getCategoryId());
                        return new CategoryNotFoundException("ID: " + dto.getCategoryId());
                    });
            product.setCategory(category);
        }

        // Mettre à jour les champs
        ProductMapper.updateEntity(dto, product);

        // Sauvegarder
        Product saved = productRepository.save(product);
        log.info("✅ Produit mis à jour avec succès");

        log.info("=== FIN mise à jour produit (succès) ===");
        return ProductMapper.toResponseDTO(saved);
    }

    @Override
    @TrackMetrics
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO updateStock(Long id, Integer quantity) {
        log.info("📦 Mise à jour du stock - Produit ID: {}, Quantité: {}", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Produit avec ID {} non trouvé", id);
                    return new ProductNotFoundException("ID: " + id);
                });

        product.setQuantity(quantity);
        Product saved = productRepository.save(product);

        log.info("✅ Stock mis à jour : {}", saved.getQuantity());
        return ProductMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    @Override
    @TrackMetrics
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("🗑️ Suppression du produit ID : {}", id);

        if (!productRepository.existsById(id)) {
            log.warn("❌ Produit avec ID {} non trouvé", id);
            throw new ProductNotFoundException("ID: " + id);
        }

        productRepository.deleteById(id);
        log.info("✅ Produit supprimé avec succès");
    }

    // ============================================================
    // 6. RECHERCHE
    // ============================================================

    @Override
    @TrackMetrics
    public PageResponseDTO<ProductResponseDTO> searchProducts(String keyword, Pageable pageable) {
        log.info("🔍 Recherche de produits - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        Page<ProductResponseDTO> mappedPage = page.map(ProductMapper::toResponseDTO);

        log.info("✅ {} produits trouvés pour '{}'", mappedPage.getTotalElements(), keyword);
        return new PageResponseDTO<>(mappedPage);
    }

    @Override
    @TrackMetrics
    public PageResponseDTO<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.info("📋 Récupération des produits de la catégorie ID : {}", categoryId);

        // Vérifier que la catégorie existe
        if (!categoryRepository.existsById(categoryId)) {
            log.warn("❌ Catégorie avec ID {} non trouvée", categoryId);
            throw new CategoryNotFoundException("ID: " + categoryId);
        }

        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        Page<ProductResponseDTO> mappedPage = page.map(ProductMapper::toResponseDTO);

        log.info("✅ {} produits trouvés dans la catégorie", mappedPage.getTotalElements());
        return new PageResponseDTO<>(mappedPage);
    }

    @Override
    @TrackMetrics
    public List<ProductResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("📋 Récupération des produits entre {} et {}", minPrice, maxPrice);

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return ProductMapper.toResponseDTOList(products);
    }

    @Override
    @TrackMetrics
    public PageResponseDTO<ProductResponseDTO> getProductsInStock(Pageable pageable) {
        log.info("📋 Récupération des produits en stock - Page: {}", pageable.getPageNumber());

        Page<Product> page = productRepository.findByQuantityGreaterThan(0, pageable);
        Page<ProductResponseDTO> mappedPage = page.map(ProductMapper::toResponseDTO);

        log.info("✅ {} produits en stock", mappedPage.getTotalElements());
        return new PageResponseDTO<>(mappedPage);
    }

    // ============================================================
    // 7. STATISTIQUES
    // ============================================================

    @Override
    public long countInStock() {
        return productRepository.countInStock();
    }

    @Override
    public double getAveragePrice() {
        Double avg = productRepository.getAveragePrice();
        return avg != null ? avg : 0.0;
    }

    @Override
    public BigDecimal getTotalStockValue() {
        BigDecimal total = productRepository.getTotalStockValue();
        return total != null ? total : BigDecimal.ZERO;
    }
}