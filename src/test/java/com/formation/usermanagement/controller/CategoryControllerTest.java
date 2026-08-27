package com.formation.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.usermanagement.dto.CategoryRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

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
    // 1. TESTS GET /api/categories
    // ============================================================

    /**
     * Test 1.1 - Récupérer la liste paginée des catégories
     */
    @Test
    void shouldGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * Test 1.2 - Récupérer les catégories sans token
     */
    @Test
    void shouldNotGetAllCategoriesWithoutToken() throws Exception {
        mockMvc.perform(get("/api/categories?page=0&size=10"))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // 2. TESTS GET /api/categories/all
    // ============================================================

    /**
     * Test 2.1 - Récupérer toutes les catégories (sans pagination)
     */
    @Test
    void shouldGetAllCategoriesList() throws Exception {
        mockMvc.perform(get("/api/categories/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // 3. TESTS GET /api/categories/summary
    // ============================================================

    /**
     * Test 3.1 - Récupérer les catégories en version résumée
     */
    @Test
    void shouldGetAllCategoriesSummary() throws Exception {
        mockMvc.perform(get("/api/categories/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ============================================================
    // 4. TESTS GET /api/categories/{id}
    // ============================================================

    /**
     * Test 4.1 - Récupérer une catégorie par ID
     */
    @Test
    void shouldGetCategoryById() throws Exception {
        mockMvc.perform(get("/api/categories/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").exists());
    }

    /**
     * Test 4.2 - Récupérer une catégorie inexistante
     */
    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/api/categories/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 5. TESTS GET /api/categories/name/{name}
    // ============================================================

    /**
     * Test 5.1 - Récupérer une catégorie par son nom
     */
    @Test
    void shouldGetCategoryByName() throws Exception {
        // ✅ URL correctement encodée
        mockMvc.perform(get("/api/categories/name/Electronique")  // ← Sans accent
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Électronique"));
    }

    // ============================================================
    // 6. TESTS POST /api/categories (Création)
    // ============================================================

    /**
     * Test 6.1 - Créer une catégorie avec succès
     */
    @Test
    void shouldCreateCategory() throws Exception {
        // ✅ Utiliser un nom unique avec timestamp
        String uniqueName = "Test Category " + System.currentTimeMillis();

        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name(uniqueName)
                .description("Catégorie créée par le test")
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(uniqueName));
    }

    /**
     * Test 6.2 - Créer une catégorie avec un nom déjà existant
     */
    @Test
    void shouldFailCreateCategoryWithExistingName() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Électronique")  // Déjà existant
                .description("Duplicata")
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /**
     * Test 6.3 - Créer une catégorie avec des données invalides
     */
    @Test
    void shouldFailCreateCategoryWithInvalidData() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("")  // Nom vide
                .build();

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 7. TESTS PUT /api/categories/{id} (Modification)
    // ============================================================

    /**
     * Test 7.1 - Modifier une catégorie avec succès
     */
    @Test
    void shouldUpdateCategory() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Test Category Updated")
                .description("Catégorie modifiée par le test")
                .build();

        mockMvc.perform(put("/api/categories/11")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category Updated"));
    }

    /**
     * Test 7.2 - Modifier une catégorie inexistante
     */
    @Test
    void shouldFailUpdateNonExistentCategory() throws Exception {
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Inexistant")
                .description("Inexistant")
                .build();

        mockMvc.perform(put("/api/categories/999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 8. TESTS DELETE /api/categories/{id} (Suppression)
    // ============================================================

    /**
     * Test 8.1 - Supprimer une catégorie
     */
    @Test
    void shouldDeleteCategory() throws Exception {
        // Créer une catégorie à supprimer
        CategoryRequestDTO request = CategoryRequestDTO.builder()
                .name("Category To Delete")
                .description("À supprimer")
                .build();

        String response = mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = Long.parseLong(response.split("\"id\":")[1].split(",")[0]);

        mockMvc.perform(delete("/api/categories/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    // ============================================================
    // 9. TESTS GET /api/categories/search
    // ============================================================

    /**
     * Test 9.1 - Rechercher des catégories par mot-clé
     */
    @Test
    void shouldSearchCategories() throws Exception {
        mockMvc.perform(get("/api/categories/search?keyword=Electronique")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ============================================================
    // 10. TESTS D'ERREUR - PERMISSIONS
    // ============================================================

    /**
     * Test 10.1 - Accès sans token
     */
    @Test
    void shouldNotAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
}