package com.formation.usermanagement.service.impl;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.CategoryRequestDTO;
import com.formation.usermanagement.dto.CategoryResponseDTO;
import com.formation.usermanagement.dto.CategorySummaryDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.exception.CategoryDejaExistantException;
import com.formation.usermanagement.exception.CategoryNotFoundException;
import com.formation.usermanagement.exception.CategoryUtiliseException;
import com.formation.usermanagement.mapper.CategoryMapper;
import com.formation.usermanagement.repository.CategoryRepository;
import com.formation.usermanagement.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ================================================================
 * IMPLÉMENTATION DU SERVICE CATEGORY
 * ================================================================
 *
 * 📖 EXPLICATION :
 *
 * Cette classe contient toute la logique métier pour la gestion des catégories.
 *
 * 🔐 PERMISSIONS UTILISÉES :
 * - CATEGORY_READ   : Consultation
 * - CATEGORY_CREATE : Création
 * - CATEGORY_UPDATE : Modification
 * - CATEGORY_DELETE : Suppression
 *
 * 📦 CACHE :
 * - categories : Cache des catégories
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // ============================================================
    // 1. CRÉATION
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO creerCategory(CategoryRequestDTO dto) {
        log.info("=== DÉBUT création catégorie ===");
        log.info("📝 Nom : {}", dto.getName());

        // Vérifier l'unicité du nom
        if (categoryRepository.existsByName(dto.getName())) {
            log.warn("❌ La catégorie {} existe déjà", dto.getName());
            throw new CategoryDejaExistantException(dto.getName());
        }

        // Convertir DTO → Entité
        Category category = CategoryMapper.toEntity(dto);
        log.debug("📦 Entité créée : {}", category.getName());

        // Sauvegarder
        Category saved = categoryRepository.save(category);
        log.info("✅ Catégorie sauvegardée avec ID : {}", saved.getId());

        log.info("=== FIN création catégorie (succès) ===");
        return CategoryMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 2. RÉCUPÉRATION
    // ============================================================

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponseDTO getCategory(Long id) {
        log.debug("🔍 Récupération de la catégorie ID : {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Catégorie avec ID {} non trouvée", id);
                    return new CategoryNotFoundException("ID: " + id);
                });

        log.debug("✅ Catégorie trouvée : {}", category.getName());
        return CategoryMapper.toResponseDTO(category);
    }

    @Override
    @Cacheable(value = "categories", key = "#name")
    public CategoryResponseDTO getCategoryByName(String name) {
        log.debug("🔍 Récupération de la catégorie par nom : {}", name);

        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("❌ Catégorie avec nom {} non trouvée", name);
                    return new CategoryNotFoundException("Nom: " + name);
                });

        return CategoryMapper.toResponseDTO(category);
    }

    // ============================================================
    // 3. LISTES
    // ============================================================

    @Override
    public PageResponseDTO<CategoryResponseDTO> getAllCategories(Pageable pageable) {
        log.info("📋 Récupération des catégories - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Category> page = categoryRepository.findAll(pageable);
        Page<CategoryResponseDTO> mappedPage = page.map(CategoryMapper::toResponseDTO);

        log.info("✅ {} catégories récupérées", mappedPage.getNumberOfElements());
        return new PageResponseDTO<>(mappedPage);
    }

    @Override
    public List<CategoryResponseDTO> getAllCategoriesList() {
        log.debug("📋 Récupération de toutes les catégories (sans pagination)");

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return CategoryMapper.toResponseDTOList(categories);
    }

    @Override
    public List<CategorySummaryDTO> getAllCategoriesSummary() {
        log.debug("📋 Récupération des catégories (version résumée)");

        List<Category> categories = categoryRepository.findAllByOrderByNameAsc();
        return CategoryMapper.toSummaryDTOList(categories);
    }

    // ============================================================
    // 4. MISE À JOUR
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        log.info("=== DÉBUT mise à jour catégorie ID : {} ===", id);

        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Catégorie avec ID {} non trouvée", id);
                    return new CategoryNotFoundException("ID: " + id);
                });

        // Vérifier que le nouveau nom n'est pas utilisé par une autre catégorie
        categoryRepository.findByName(dto.getName())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        log.warn("❌ Le nom {} est déjà utilisé par une autre catégorie", dto.getName());
                        throw new CategoryDejaExistantException(dto.getName());
                    }
                });

        // Mettre à jour
        CategoryMapper.updateEntity(dto, category);
        log.debug("📝 Catégorie mise à jour : {}", category.getName());

        // Sauvegarder
        Category saved = categoryRepository.save(category);
        log.info("✅ Catégorie mise à jour avec succès");

        log.info("=== FIN mise à jour catégorie (succès) ===");
        return CategoryMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 5. SUPPRESSION
    // ============================================================

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        log.info("🗑️ Suppression de la catégorie ID : {}", id);

        // Vérifier que la catégorie existe
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Catégorie avec ID {} non trouvée", id);
                    return new CategoryNotFoundException("ID: " + id);
                });

        // Vérifier que la catégorie n'a pas de produits
        long productCount = categoryRepository.countProductsByCategory().stream()
                .filter(result -> ((Long) result[0]).equals(id))
                .map(result -> (Long) result[2])
                .findFirst()
                .orElse(0L);

        if (productCount > 0) {
            log.warn("❌ La catégorie {} contient {} produit(s)", category.getName(), productCount);
            throw new CategoryUtiliseException(category.getName(), productCount);
        }

        // Supprimer
        categoryRepository.deleteById(id);
        log.info("✅ Catégorie supprimée avec succès");
    }

    // ============================================================
    // 6. RECHERCHE
    // ============================================================

    @Override
    public PageResponseDTO<CategoryResponseDTO> searchCategories(String keyword, Pageable pageable) {
        log.info("🔍 Recherche de catégories - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        // Recherche paginée
        Page<Category> page = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        Page<CategoryResponseDTO> mappedPage = page.map(CategoryMapper::toResponseDTO);

        log.info("✅ {} catégories trouvées pour '{}'", mappedPage.getTotalElements(), keyword);
        return new PageResponseDTO<>(mappedPage);
    }

    // ============================================================
    // 7. VALIDATION
    // ============================================================

    @Override
    public boolean existeParNom(String name) {
        return categoryRepository.existsByName(name);
    }
}