package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.ProductRequestDTO;
import com.formation.usermanagement.dto.ProductResponseDTO;
import com.formation.usermanagement.dto.ProductSummaryDTO;
import com.formation.usermanagement.entity.Category;
import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.exception.CategoryNotFoundException;
import com.formation.usermanagement.exception.ProductNotFoundException;
import com.formation.usermanagement.repository.CategoryRepository;
import com.formation.usermanagement.repository.ProductRepository;
import com.formation.usermanagement.service.impl.ProductServiceImpl;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires du service Product")
class ProductServiceImplTest {

    // ============================================================
    // 1. MOCKS
    // ============================================================

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    // ============================================================
    // 2. DONNÉES DE TEST
    // ============================================================

    private Product product;
    private Category category;
    private ProductRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Créer une catégorie
        category = new Category();
        category.setId(1L);
        category.setName("Électronique");
        category.setDescription("Appareils électroniques");
        category.setProducts(new ArrayList<>());

        // Créer un produit
        product = new Product();
        product.setId(1L);
        product.setName("iPhone 15");
        product.setDescription("Le dernier smartphone Apple avec puce A16");
        product.setPrice(new BigDecimal("999.99"));
        product.setQuantity(10);
        product.setImageUrl("https://example.com/iphone.jpg");
        product.setCategory(category);

        // Créer un DTO de requête
        requestDTO = ProductRequestDTO.builder()
                .name("Samsung Galaxy S24")
                .description("Smartphone Samsung avec intelligence artificielle")
                .price(new BigDecimal("899.99"))
                .quantity(15)
                .imageUrl("https://example.com/samsung.jpg")
                .categoryId(1L)
                .build();
    }

    // ============================================================
    // 3. TESTS DE CRÉATION
    // ============================================================

    /**
     * TEST 1 - Créer un produit avec succès
     *
     * Objectif : Vérifier qu'un produit est créé correctement
     *
     * Étapes :
     * 1. Vérifier que la catégorie existe
     * 2. Convertir DTO → Entité
     * 3. Sauvegarder
     * 4. Retourner le DTO
     *
     * Vérifications :
     * - Le produit a un ID
     * - Le nom correspond
     * - Le prix correspond
     * - La catégorie est bien assignée
     */
    @Test
    @DisplayName("✅ Devrait créer un produit avec succès")
    void shouldCreateProductSuccessfully() {
        // GIVEN
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // WHEN
        ProductResponseDTO result = productService.creerProduct(requestDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Samsung Galaxy S24");
        assertThat(result.getDescription()).isEqualTo("Smartphone Samsung avec intelligence artificielle");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("899.99"));
        assertThat(result.getQuantity()).isEqualTo(15);
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/samsung.jpg");
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getId()).isEqualTo(1L);
        assertThat(result.getCategory().getName()).isEqualTo("Électronique");

        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    /**
     * TEST 2 - Créer un produit avec une catégorie inexistante
     *
     * Objectif : Vérifier que l'exception est levée
     *
     * Vérifications :
     * - Exception CategoryNotFoundException
     * - Le save() n'est pas appelé
     */
    @Test
    @DisplayName("❌ Devrait échouer si la catégorie n'existe pas")
    void shouldFailWhenCategoryNotFound() {
        // GIVEN
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        ProductRequestDTO invalidDTO = ProductRequestDTO.builder()
                .name("Test")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .categoryId(999L)
                .build();

        assertThatThrownBy(() -> productService.creerProduct(invalidDTO))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * TEST 3 - Créer un produit avec des données invalides (pas de test ici car géré par validation)
     * La validation est gérée par les annotations @Valid dans le controller
     */

    // ============================================================
    // 4. TESTS DE RÉCUPÉRATION
    // ============================================================

    /**
     * TEST 4 - Récupérer un produit par ID
     *
     * Objectif : Vérifier qu'un produit est récupéré par son ID
     *
     * Vérifications :
     * - Le produit n'est pas null
     * - L'ID correspond
     * - Le nom correspond
     * - Le prix correspond
     */
    @Test
    @DisplayName("✅ Devrait récupérer un produit par ID")
    void shouldGetProductById() {
        // GIVEN
        when(productRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(product));

        // WHEN
        ProductResponseDTO result = productService.getProduct(1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getDescription()).isEqualTo("Le dernier smartphone Apple avec puce A16");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/iphone.jpg");
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getName()).isEqualTo("Électronique");

        verify(productRepository).findByIdWithCategory(1L);
    }

    /**
     * TEST 5 - Récupérer un produit inexistant
     *
     * Objectif : Vérifier que l'exception est levée
     *
     * Vérifications :
     * - Exception ProductNotFoundException
     */
    @Test
    @DisplayName("❌ Devrait échouer si le produit n'existe pas")
    void shouldThrowExceptionWhenProductNotFound() {
        // GIVEN
        when(productRepository.findByIdWithCategory(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productService.getProduct(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ============================================================
    // 5. TESTS DE LISTE
    // ============================================================

    /**
     * TEST 6 - Récupérer tous les produits (paginé)
     *
     * Objectif : Vérifier que la liste paginée est retournée
     *
     * Vérifications :
     * - La page n'est pas null
     * - La page contient 1 élément
     * - Le nombre total d'éléments est 1
     * - Le nombre total de pages est 1
     */
    @Test
    @DisplayName("✅ Devrait récupérer tous les produits (paginé)")
    void shouldGetAllProductsPaginated() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findAll(pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<ProductResponseDTO> result = productService.getAllProducts(pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15");
    }

    /**
     * TEST 7 - Récupérer tous les produits (liste complète)
     *
     * Objectif : Vérifier que la liste complète est retournée
     *
     * Vérifications :
     * - La liste n'est pas null
     * - La liste contient 1 élément
     */
    @Test
    @DisplayName("✅ Devrait récupérer tous les produits (liste complète)")
    void shouldGetAllProductsList() {
        // GIVEN
        when(productRepository.findAllWithCategory()).thenReturn(List.of(product));

        // WHEN
        List<ProductSummaryDTO> result = productService.getAllProductsList();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("iPhone 15");
        assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("999.99"));
        assertThat(result.get(0).getCategoryName()).isEqualTo("Électronique");
    }

    // ============================================================
    // 6. TESTS DE MISE À JOUR
    // ============================================================

    /**
     * TEST 8 - Mettre à jour un produit
     *
     * Objectif : Vérifier qu'un produit est mis à jour correctement
     *
     * Étapes :
     * 1. Vérifier que le produit existe
     * 2. Vérifier que la catégorie existe (si modifiée)
     * 3. Mettre à jour les champs
     * 4. Sauvegarder
     *
     * Vérifications :
     * - Le nom a changé
     * - Le prix a changé
     * - La quantité a changé
     */
    @Test
    @DisplayName("✅ Devrait mettre à jour un produit avec succès")
    void shouldUpdateProductSuccessfully() {
        // GIVEN
        ProductRequestDTO updateDTO = ProductRequestDTO.builder()
                .name("iPhone 15 Pro Max")
                .description("Le dernier iPhone Pro Max avec appareil photo amélioré")
                .price(new BigDecimal("1299.99"))
                .quantity(5)
                .imageUrl("https://example.com/iphone-pro.jpg")
                .categoryId(1L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // WHEN
        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro Max");
        assertThat(result.getDescription()).isEqualTo("Le dernier iPhone Pro Max avec appareil photo amélioré");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("1299.99"));
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/iphone-pro.jpg");

        verify(productRepository).save(any(Product.class));
    }

    /**
     * TEST 9 - Mettre à jour un produit inexistant
     *
     * Objectif : Vérifier que l'exception est levée
     *
     * Vérifications :
     * - Exception ProductNotFoundException
     */
    @Test
    @DisplayName("❌ Devrait échouer si le produit n'existe pas")
    void shouldFailWhenProductNotFoundForUpdate() {
        // GIVEN
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> productService.updateProduct(999L, requestDTO))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, never()).save(any(Product.class));
    }

    /**
     * TEST 10 - Mettre à jour le stock d'un produit
     *
     * Objectif : Vérifier que le stock est mis à jour
     *
     * Vérifications :
     * - La nouvelle quantité est 25
     */
    @Test
    @DisplayName("✅ Devrait mettre à jour le stock d'un produit")
    void shouldUpdateStock() {
        // GIVEN
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // WHEN
        ProductResponseDTO result = productService.updateStock(1L, 25);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(25);
        verify(productRepository).save(any(Product.class));
    }

    // ============================================================
    // 7. TESTS DE SUPPRESSION
    // ============================================================

    /**
     * TEST 11 - Supprimer un produit
     *
     * Objectif : Vérifier qu'un produit est supprimé
     *
     * Vérifications :
     * - La méthode deleteById est appelée
     */
    @Test
    @DisplayName("✅ Devrait supprimer un produit avec succès")
    void shouldDeleteProduct() {
        // GIVEN
        when(productRepository.existsById(1L)).thenReturn(true);

        // WHEN
        productService.deleteProduct(1L);

        // THEN
        verify(productRepository).deleteById(1L);
    }

    /**
     * TEST 12 - Supprimer un produit inexistant
     *
     * Objectif : Vérifier que l'exception est levée
     *
     * Vérifications :
     * - Exception ProductNotFoundException
     * - La méthode deleteById n'est pas appelée
     */
    @Test
    @DisplayName("❌ Devrait échouer si le produit n'existe pas")
    void shouldFailWhenProductNotFoundForDelete() {
        // GIVEN
        when(productRepository.existsById(999L)).thenReturn(false);

        // WHEN & THEN
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // 8. TESTS DE RECHERCHE
    // ============================================================

    /**
     * TEST 13 - Rechercher des produits par mot-clé
     *
     * Objectif : Vérifier que la recherche fonctionne
     *
     * Vérifications :
     * - La page contient 1 élément
     * - Le produit trouvé correspond au mot-clé
     */
    @Test
    @DisplayName("✅ Devrait rechercher des produits par mot-clé")
    void shouldSearchProducts() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        when(productRepository.searchByKeyword("iPhone", pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<ProductResponseDTO> result = productService.searchProducts("iPhone", pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("iPhone");
    }

    /**
     * TEST 14 - Récupérer les produits par catégorie
     *
     * Objectif : Vérifier que les produits d'une catégorie sont retournés
     *
     * Vérifications :
     * - La page contient 1 élément
     * - Le produit appartient à la bonne catégorie
     */
    @Test
    @DisplayName("✅ Devrait récupérer les produits par catégorie")
    void shouldGetProductsByCategory() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCategoryId(1L, pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<ProductResponseDTO> result = productService.getProductsByCategory(1L, pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory().getId()).isEqualTo(1L);
    }

    /**
     * TEST 15 - Récupérer les produits d'une catégorie inexistante
     *
     * Objectif : Vérifier que l'exception est levée
     *
     * Vérifications :
     * - Exception CategoryNotFoundException
     */
    @Test
    @DisplayName("❌ Devrait échouer si la catégorie n'existe pas")
    void shouldFailWhenCategoryNotFoundForProducts() {
        // GIVEN
        when(categoryRepository.existsById(999L)).thenReturn(false);

        // WHEN & THEN
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> productService.getProductsByCategory(999L, pageable))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, never()).findByCategoryId(anyLong(), any(Pageable.class));
    }

    /**
     * TEST 16 - Récupérer les produits dans une fourchette de prix
     *
     * Objectif : Vérifier que les produits dans la fourchette sont retournés
     *
     * Vérifications :
     * - La liste contient 1 élément
     * - Le prix est entre 100 et 1000
     */
    @Test
    @DisplayName("✅ Devrait récupérer les produits dans une fourchette de prix")
    void shouldGetProductsByPriceRange() {
        // GIVEN
        when(productRepository.findByPriceBetween(
                new BigDecimal("100"), new BigDecimal("1000")))
                .thenReturn(List.of(product));

        // WHEN
        List<ProductResponseDTO> result = productService.getProductsByPriceRange(
                new BigDecimal("100"), new BigDecimal("1000"));

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isBetween(
                new BigDecimal("100"), new BigDecimal("1000"));
    }

    /**
     * TEST 17 - Récupérer les produits en stock
     *
     * Objectif : Vérifier que les produits en stock sont retournés
     *
     * Vérifications :
     * - La page contient 1 élément
     * - La quantité est > 0
     */
    @Test
    @DisplayName("✅ Devrait récupérer les produits en stock")
    void shouldGetProductsInStock() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> page = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByQuantityGreaterThan(0, pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<ProductResponseDTO> result = productService.getProductsInStock(pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getQuantity()).isGreaterThan(0);
    }

    // ============================================================
    // 9. TESTS STATISTIQUES
    // ============================================================

    /**
     * TEST 18 - Compter les produits en stock
     *
     * Objectif : Vérifier que le comptage fonctionne
     *
     * Vérifications :
     * - Le comptage est égal à 5
     */
    @Test
    @DisplayName("✅ Devrait compter les produits en stock")
    void shouldCountInStock() {
        // GIVEN
        when(productRepository.countInStock()).thenReturn(5L);

        // WHEN
        long count = productService.countInStock();

        // THEN
        assertThat(count).isEqualTo(5L);
    }

    /**
     * TEST 19 - Calculer le prix moyen
     *
     * Objectif : Vérifier que le prix moyen est calculé
     *
     * Vérifications :
     * - Le prix moyen est 500.0
     */
    @Test
    @DisplayName("✅ Devrait calculer le prix moyen")
    void shouldGetAveragePrice() {
        // GIVEN
        when(productRepository.getAveragePrice()).thenReturn(500.0);

        // WHEN
        double avg = productService.getAveragePrice();

        // THEN
        assertThat(avg).isEqualTo(500.0);
    }

    /**
     * TEST 20 - Calculer la valeur totale du stock
     *
     * Objectif : Vérifier que la valeur totale est calculée
     *
     * Vérifications :
     * - La valeur totale est 5000.00
     */
    @Test
    @DisplayName("✅ Devrait calculer la valeur totale du stock")
    void shouldGetTotalStockValue() {
        // GIVEN
        when(productRepository.getTotalStockValue()).thenReturn(new BigDecimal("5000.00"));

        // WHEN
        BigDecimal total = productService.getTotalStockValue();

        // THEN
        assertThat(total).isEqualTo(new BigDecimal("5000.00"));
    }
}