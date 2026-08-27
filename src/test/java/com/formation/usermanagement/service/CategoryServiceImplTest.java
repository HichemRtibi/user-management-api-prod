package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.CategoryRequestDTO;
import com.formation.usermanagement.dto.CategoryResponseDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.exception.CategoryDejaExistantException;
import com.formation.usermanagement.exception.CategoryNotFoundException;
import com.formation.usermanagement.exception.CategoryUtiliseException;
import com.formation.usermanagement.repository.CategoryRepository;
import com.formation.usermanagement.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Category")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Électronique");
        category.setDescription("Appareils électroniques");

        requestDTO = CategoryRequestDTO.builder()
                .name("Informatique")
                .description("Ordinateurs et accessoires")
                .build();
    }

    // ============================================================
    // 1. TESTS DE CRÉATION
    // ============================================================

    @Test
    @DisplayName("✅ Devrait créer une catégorie avec succès")
    void shouldCreateCategorySuccessfully() {
        // GIVEN
        when(categoryRepository.existsByName("Informatique")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // WHEN
        CategoryResponseDTO result = categoryService.creerCategory(requestDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Informatique");
        assertThat(result.getDescription()).isEqualTo("Ordinateurs et accessoires");

        verify(categoryRepository).existsByName("Informatique");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("❌ Devrait échouer si le nom existe déjà")
    void shouldFailWhenNameAlreadyExists() {
        // GIVEN
        when(categoryRepository.existsByName("Informatique")).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.creerCategory(requestDTO))
                .isInstanceOf(CategoryDejaExistantException.class)
                .hasMessageContaining("existe déjà");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ============================================================
    // 2. TESTS DE RÉCUPÉRATION
    // ============================================================

    @Test
    @DisplayName("✅ Devrait récupérer une catégorie par ID")
    void shouldGetCategoryById() {
        // GIVEN
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // WHEN
        CategoryResponseDTO result = categoryService.getCategory(1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Électronique");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("❌ Devrait échouer si la catégorie n'existe pas")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // GIVEN
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.getCategory(999L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("✅ Devrait récupérer une catégorie par nom")
    void shouldGetCategoryByName() {
        // GIVEN
        when(categoryRepository.findByName("Électronique")).thenReturn(Optional.of(category));

        // WHEN
        CategoryResponseDTO result = categoryService.getCategoryByName("Électronique");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Électronique");
    }

    // ============================================================
    // 3. TESTS DE LISTE
    // ============================================================

    @Test
    @DisplayName("✅ Devrait récupérer toutes les catégories (paginé)")
    void shouldGetAllCategoriesPaginated() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> page = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<CategoryResponseDTO> result = categoryService.getAllCategories(pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("✅ Devrait récupérer toutes les catégories (liste complète)")
    void shouldGetAllCategoriesList() {
        // GIVEN
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(category));

        // WHEN
        List<CategoryResponseDTO> result = categoryService.getAllCategoriesList();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Électronique");
    }

    // ============================================================
    // 4. TESTS DE MISE À JOUR
    // ============================================================

    @Test
    @DisplayName("✅ Devrait mettre à jour une catégorie avec succès")
    void shouldUpdateCategorySuccessfully() {
        // GIVEN
        CategoryRequestDTO updateDTO = CategoryRequestDTO.builder()
                .name("Informatique")
                .description("Ordinateurs et accessoires")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Informatique")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // WHEN
        CategoryResponseDTO result = categoryService.updateCategory(1L, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Informatique");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName(" Devrait échouer si la catégorie n'existe pas")
    void shouldFailWhenCategoryNotFoundForUpdate() {
        // GIVEN
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.updateCategory(999L, requestDTO))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // ============================================================
    // 5. TESTS DE SUPPRESSION
    // ============================================================


    @Test
    @DisplayName("✅ Devrait supprimer une catégorie sans produits")
    void shouldDeleteCategoryWithoutProducts() {
        // GIVEN
        Category categoryWithoutProducts = new Category();
        categoryWithoutProducts.setId(1L);
        categoryWithoutProducts.setName("Électronique");
        categoryWithoutProducts.setProducts(new ArrayList<>());  // Liste vide

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryWithoutProducts));

        // WHEN
        categoryService.deleteCategory(1L);

        // THEN
        verify(categoryRepository).deleteById(1L);
    }


    @Test
    @DisplayName("❌ Devrait échouer si la catégorie contient des produits")
    void shouldFailWhenCategoryHasProducts() {
        // GIVEN
        Category categoryWithProducts = new Category();
        categoryWithProducts.setId(1L);
        categoryWithProducts.setName("Électronique");

        // Ajouter des produits
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("iPhone");
        product1.setCategory(categoryWithProducts);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Samsung");
        product2.setCategory(categoryWithProducts);

        categoryWithProducts.getProducts().add(product1);
        categoryWithProducts.getProducts().add(product2);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryWithProducts));

        // WHEN & THEN
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryUtiliseException.class)
                .hasMessageContaining("2 produit(s)");

        verify(categoryRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // 6. TESTS DE RECHERCHE
    // ============================================================

    @Test
    @DisplayName("✅ Devrait rechercher des catégories par mot-clé")
    void shouldSearchCategories() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = List.of(category);
        Page<Category> page = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findByNameContainingIgnoreCase("Electronique", pageable))
                .thenReturn(page);

        // WHEN
        PageResponseDTO<CategoryResponseDTO> result = categoryService.searchCategories("Electronique", pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}