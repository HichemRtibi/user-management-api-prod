package com.formation.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.usermanagement.dto.ProductRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Se connecter avec admin
        String loginJson = """
                {
                    "email": "admin@example.com",
                    "motDePasse": "Admin123!"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        adminToken = response.split("\"token\":\"")[1].split("\"")[0];
    }

    // ============================================================
    // 1. TESTS GET /api/products (Liste paginée)
    // ============================================================

    /**
     * Test 1.1 - Récupérer la liste paginée des produits
     *
     * Objectif : Vérifier que l'API retourne la liste paginée des produits
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste "content"
     * - La réponse contient "totalElements"
     */
    @Test
    void shouldGetAllProducts() throws Exception {
        mockMvc.perform(get("/api/products?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * Test 1.2 - Récupérer les produits sans token
     *
     * Objectif : Vérifier que l'API refuse l'accès sans authentification
     *
     * Vérifications :
     * - Status 403 Forbidden
     */
    @Test
    void shouldNotGetAllProductsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 2. TESTS GET /api/products/all (Liste complète)
    // ============================================================

    /**
     * Test 2.1 - Récupérer tous les produits (sans pagination)
     *
     * Objectif : Vérifier que l'API retourne la liste complète des produits
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse est un tableau
     */
    @Test
    void shouldGetAllProductsList() throws Exception {
        mockMvc.perform(get("/api/products/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // 3. TESTS GET /api/products/{id}
    // ============================================================

    /**
     * Test 3.1 - Récupérer un produit par ID
     *
     * Objectif : Vérifier que l'API retourne les détails d'un produit
     *
     * Vérifications :
     * - Status 200 OK
     * - ID correspond
     * - Nom du produit présent
     */
    @Test
    void shouldGetProductById() throws Exception {
        mockMvc.perform(get("/api/products/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    /**
     * Test 3.2 - Récupérer un produit inexistant
     *
     * Objectif : Vérifier que l'API retourne 404 pour un ID inexistant
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/products/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 4. TESTS POST /api/products (Création)
    // ============================================================

    /**
     * Test 4.1 - Créer un produit avec succès
     *
     * Objectif : Vérifier que l'API crée un nouveau produit
     *
     * Vérifications :
     * - Status 201 Created
     * - ID généré
     * - Nom correct
     * - Prix correct
     */
    @Test
    void shouldCreateProduct() throws Exception {
        String uniqueName = "Test Product " + System.currentTimeMillis();

        ProductRequestDTO request = ProductRequestDTO.builder()
                .name(uniqueName)
                .description("Produit créé par le test")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .imageUrl("https://example.com/test-product.jpg")
                .categoryId(1L)  // Catégorie "Électronique"
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(uniqueName))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    /**
     * Test 4.2 - Créer un produit avec une catégorie inexistante
     *
     * Objectif : Vérifier que l'API refuse la création avec une catégorie inexistante
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    void shouldFailCreateProductWithNonExistentCategory() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Test Product")
                .description("Produit avec catégorie inexistante")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .categoryId(999L)  // Catégorie inexistante
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    /**
     * Test 4.3 - Créer un produit avec des données invalides
     *
     * Objectif : Vérifier que l'API refuse la création avec des données invalides
     *
     * Vérifications :
     * - Status 400 Bad Request
     */
    @Test
    void shouldFailCreateProductWithInvalidData() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("")  // Nom vide
                .price(new BigDecimal("-10"))  // Prix négatif
                .quantity(-5)  // Quantité négative
                .categoryId(null)  // Catégorie null
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 5. TESTS PUT /api/products/{id} (Modification)
    // ============================================================

    /**
     * Test 5.1 - Modifier un produit avec succès
     *
     * Objectif : Vérifier que l'API modifie un produit existant
     *
     * Vérifications :
     * - Status 200 OK
     * - Nom modifié
     * - Prix modifié
     */
    @Test
    void shouldUpdateProduct() throws Exception {
        // Créer un produit à modifier
        String uniqueName = "Product To Update " + System.currentTimeMillis();

        ProductRequestDTO createRequest = ProductRequestDTO.builder()
                .name(uniqueName)
                .description("Produit à modifier")
                .price(new BigDecimal("49.99"))
                .quantity(5)
                .categoryId(1L)
                .build();

        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = Long.parseLong(createResponse.split("\"id\":")[1].split(",")[0]);

        // Modifier le produit
        ProductRequestDTO updateRequest = ProductRequestDTO.builder()
                .name(uniqueName + " Updated")
                .description("Produit modifié par le test")
                .price(new BigDecimal("79.99"))
                .quantity(15)
                .categoryId(2L)  // Changer la catégorie vers "Vêtements"
                .build();

        mockMvc.perform(put("/api/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(uniqueName + " Updated"))
                .andExpect(jsonPath("$.price").value(79.99));
    }

    /**
     * Test 5.2 - Modifier un produit inexistant
     *
     * Objectif : Vérifier que l'API retourne 404 pour un ID inexistant
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    void shouldFailUpdateNonExistentProduct() throws Exception {
        ProductRequestDTO request = ProductRequestDTO.builder()
                .name("Inexistant")
                .description("Inexistant")
                .price(new BigDecimal("99.99"))
                .quantity(10)
                .categoryId(1L)
                .build();

        mockMvc.perform(put("/api/products/999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 6. TESTS PATCH /api/products/{id}/stock
    // ============================================================

    /**
     * Test 6.1 - Mettre à jour le stock d'un produit
     *
     * Objectif : Vérifier que l'API met à jour le stock d'un produit
     *
     * Vérifications :
     * - Status 200 OK
     * - Nouvelle quantité
     */
    @Test
    void shouldUpdateStock() throws Exception {
        // Créer un produit pour le test
        String uniqueName = "Stock Product " + System.currentTimeMillis();

        ProductRequestDTO createRequest = ProductRequestDTO.builder()
                .name(uniqueName)
                .description("Produit pour test de stock")
                .price(new BigDecimal("29.99"))
                .quantity(10)
                .categoryId(1L)
                .build();

        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = Long.parseLong(createResponse.split("\"id\":")[1].split(",")[0]);

        // Mettre à jour le stock
        mockMvc.perform(patch("/api/products/" + productId + "/stock?quantity=25")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(25));
    }

    // ============================================================
    // 7. TESTS DELETE /api/products/{id}
    // ============================================================

    /**
     * Test 7.1 - Supprimer un produit
     *
     * Objectif : Vérifier que l'API supprime un produit
     *
     * Vérifications :
     * - Status 204 No Content
     */
    @Test
    void shouldDeleteProduct() throws Exception {
        // Créer un produit à supprimer
        String uniqueName = "Product To Delete " + System.currentTimeMillis();

        ProductRequestDTO createRequest = ProductRequestDTO.builder()
                .name(uniqueName)
                .description("Produit à supprimer")
                .price(new BigDecimal("19.99"))
                .quantity(5)
                .categoryId(1L)
                .build();

        String createResponse = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long productId = Long.parseLong(createResponse.split("\"id\":")[1].split(",")[0]);

        // Supprimer le produit
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ============================================================
    // 8. TESTS GET /api/products/search
    // ============================================================

    /**
     * Test 8.1 - Rechercher des produits par mot-clé
     *
     * Objectif : Vérifier que la recherche fonctionne
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste
     */
    @Test
    void shouldSearchProducts() throws Exception {
        mockMvc.perform(get("/api/products/search?keyword=iPhone")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ============================================================
    // 9. TESTS GET /api/products/category/{categoryId}
    // ============================================================

    /**
     * Test 9.1 - Récupérer les produits par catégorie
     *
     * Objectif : Vérifier que l'API retourne les produits d'une catégorie
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste
     */
    @Test
    void shouldGetProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/products/category/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    /**
     * Test 9.2 - Récupérer les produits d'une catégorie inexistante
     *
     * Objectif : Vérifier que l'API retourne 404 pour une catégorie inexistante
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/api/products/category/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 10. TESTS GET /api/products/price-range
    // ============================================================

    /**
     * Test 10.1 - Récupérer les produits dans une fourchette de prix
     *
     * Objectif : Vérifier que l'API retourne les produits entre min et max
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse est un tableau
     */
    @Test
    void shouldGetProductsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/products/price-range?min=100&max=1000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // 11. TESTS GET /api/products/in-stock
    // ============================================================

    /**
     * Test 11.1 - Récupérer les produits en stock
     *
     * Objectif : Vérifier que l'API retourne les produits en stock
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste
     */
    @Test
    void shouldGetProductsInStock() throws Exception {
        mockMvc.perform(get("/api/products/in-stock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ============================================================
    // 12. TESTS STATISTIQUES
    // ============================================================

    /**
     * Test 12.1 - Compter les produits en stock
     *
     * Objectif : Vérifier que l'API retourne le nombre de produits en stock
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse est un nombre (Long)
     */
    @Test
    void shouldCountInStock() throws Exception {
        mockMvc.perform(get("/api/products/stats/count-in-stock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    /**
     * Test 12.2 - Calculer le prix moyen
     *
     * Objectif : Vérifier que l'API retourne le prix moyen des produits
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse est un nombre (Double)
     */
    @Test
    void shouldGetAveragePrice() throws Exception {
        mockMvc.perform(get("/api/products/stats/average-price")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }

    /**
     * Test 12.3 - Calculer la valeur totale du stock
     *
     * Objectif : Vérifier que l'API retourne la valeur totale du stock
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse est un nombre (BigDecimal)
     */
    @Test
    void shouldGetTotalStockValue() throws Exception {
        mockMvc.perform(get("/api/products/stats/total-value")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }
}