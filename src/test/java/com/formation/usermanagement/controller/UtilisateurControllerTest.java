package com.formation.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Tests du Controller Utilisateur")
class UtilisateurControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // 1. Se connecter avec admin pour obtenir un token
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
    // 1. TESTS GET /api/utilisateurs (Liste paginée)
    // ============================================================

    /**
     * Test 1.1 - Récupérer la liste paginée des utilisateurs
     *
     * Objectif : Vérifier que l'API retourne la liste paginée des utilisateurs
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste "content"
     * - La réponse contient "totalElements"
     */
    @Test
    @DisplayName("✅ Devrait récupérer la liste paginée des utilisateurs")
    void shouldGetAllUtilisateurs() throws Exception {
        mockMvc.perform(get("/api/utilisateurs?page=0&size=10")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    /**
     * Test 1.2 - Récupérer les utilisateurs sans token
     *
     * Objectif : Vérifier que l'API refuse l'accès sans authentification
     *
     * Vérifications :
     * - Status 403 Forbidden
     */
    @Test
    @DisplayName("❌ Devrait refuser l'accès sans token")
    void shouldNotGetAllUtilisateursWithoutToken() throws Exception {
        mockMvc.perform(get("/api/utilisateurs?page=0&size=10"))
                .andExpect(status().isForbidden());
    }

    /**
     * Test 1.3 - Récupérer avec tri par nom ascendant
     */
    @Test
    @DisplayName("✅ Devrait trier les utilisateurs par nom ascendant")
    void shouldGetAllUtilisateursSortedByNomAsc() throws Exception {
        mockMvc.perform(get("/api/utilisateurs?page=0&size=10&sort=nom,asc")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ============================================================
    // 2. TESTS GET /api/utilisateurs/{id}
    // ============================================================

    /**
     * Test 2.1 - Récupérer un utilisateur par ID
     *
     * Objectif : Vérifier que l'API retourne les détails d'un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     * - ID correspond
     * - Email présent
     */
    @Test
    @DisplayName("✅ Devrait récupérer un utilisateur par ID")
    void shouldGetUtilisateurById() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").exists());
    }

    /**
     * Test 2.2 - Récupérer un utilisateur inexistant
     *
     * Objectif : Vérifier que l'API retourne 404 pour un ID inexistant
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    @DisplayName("❌ Devrait retourner 404 si l'utilisateur n'existe pas")
    void shouldReturn404WhenUtilisateurNotFound() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 3. TESTS GET /api/utilisateurs/email/{email}
    // ============================================================

    /**
     * Test 3.1 - Récupérer un utilisateur par email
     *
     * Objectif : Vérifier que l'API retourne un utilisateur par son email
     *
     * Vérifications :
     * - Status 200 OK
     * - Email correspond
     */
    @Test
    @DisplayName("✅ Devrait récupérer un utilisateur par email")
    void shouldGetUtilisateurByEmail() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/email/admin@example.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@example.com"));
    }

    /**
     * Test 3.2 - Récupérer un utilisateur par email inexistant
     */
    @Test
    @DisplayName("❌ Devrait retourner 404 si l'email n'existe pas")
    void shouldReturn404WhenEmailNotFound() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/email/inexistant@email.com")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 4. TESTS POST /api/utilisateurs (Création)
    // ============================================================

    /**
     * Test 4.1 - Créer un utilisateur avec succès
     *
     * Objectif : Vérifier que l'API crée un nouvel utilisateur
     *
     * Vérifications :
     * - Status 201 Created
     * - ID généré
     * - Email correct
     */
    @Test
    @DisplayName("✅ Devrait créer un utilisateur avec succès")
    void shouldCreateUtilisateur() throws Exception {
        String uniqueEmail = "test" + System.currentTimeMillis() + "@email.com";

        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("Jean")
                .nom("Test")
                .email(uniqueEmail)
                .motDePasse("Password123@")
                .build();

        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.prenom").value("Jean"));
    }

    /**
     * Test 4.2 - Créer un utilisateur avec email déjà existant
     *
     * Objectif : Vérifier que l'API refuse la création avec un email existant
     *
     * Vérifications :
     * - Status 409 Conflict
     */
    @Test
    @DisplayName("❌ Devrait échouer si l'email existe déjà")
    void shouldFailCreateWithExistingEmail() throws Exception {
        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("Duplicate")
                .nom("Test")
                .email("admin@example.com")
                .motDePasse("Password123@")
                .build();

        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    /**
     * Test 4.3 - Créer un utilisateur avec des données invalides
     *
     * Objectif : Vérifier que l'API refuse la création avec des données invalides
     *
     * Vérifications :
     * - Status 400 Bad Request
     */
    @Test
    @DisplayName("❌ Devrait échouer avec des données invalides")
    void shouldFailCreateWithInvalidData() throws Exception {
        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("")
                .nom("")
                .email("invalid-email")
                .motDePasse("123")
                .build();

        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 5. TESTS PUT /api/utilisateurs/{id} (Modification)
    // ============================================================

    /**
     * Test 5.1 - Modifier un utilisateur avec succès
     *
     * Objectif : Vérifier que l'API modifie un utilisateur existant
     *
     * Vérifications :
     * - Status 200 OK
     * - Prénom modifié
     */
    @Test
    @DisplayName("✅ Devrait modifier un utilisateur avec succès")
    void shouldUpdateUtilisateur() throws Exception {
        String uniqueEmail = "update" + System.currentTimeMillis() + "@email.com";

        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("Jean-Pierre")
                .nom("Test")
                .email(uniqueEmail)
                .motDePasse("Password123@")
                .build();

        mockMvc.perform(put("/api/utilisateurs/1")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Jean-Pierre"));
    }

    /**
     * Test 5.2 - Modifier un utilisateur inexistant
     *
     * Objectif : Vérifier que l'API retourne 404 pour un ID inexistant
     *
     * Vérifications :
     * - Status 404 Not Found
     */
    @Test
    @DisplayName("❌ Devrait retourner 404 si l'utilisateur n'existe pas")
    void shouldFailUpdateNonExistentUtilisateur() throws Exception {
        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("Test")
                .nom("Test")
                .email("test@email.com")
                .motDePasse("Password123@")
                .build();

        mockMvc.perform(put("/api/utilisateurs/999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 6. TESTS DELETE /api/utilisateurs/{id} (Suppression)
    // ============================================================

    /**
     * Test 6.1 - Supprimer un utilisateur
     *
     * Objectif : Vérifier que l'API supprime un utilisateur
     *
     * Vérifications :
     * - Status 204 No Content
     */
    @Test
    @DisplayName("✅ Devrait supprimer un utilisateur avec succès")
    void shouldDeleteUtilisateur() throws Exception {
        // Créer un utilisateur à supprimer
        String uniqueEmail = "delete" + System.currentTimeMillis() + "@email.com";

        UtilisateurRequestDTO request = UtilisateurRequestDTO.builder()
                .prenom("A")
                .nom("Supprimer")
                .email(uniqueEmail)
                .motDePasse("Password123@")
                .build();

        String response = mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = response.split("\"id\":")[1].split(",")[0];

        mockMvc.perform(delete("/api/utilisateurs/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    /**
     * Test 6.2 - Supprimer un utilisateur inexistant
     */
    @Test
    @DisplayName("❌ Devrait retourner 404 si l'utilisateur n'existe pas")
    void shouldFailDeleteNonExistentUtilisateur() throws Exception {
        mockMvc.perform(delete("/api/utilisateurs/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // 7. TESTS PATCH /api/utilisateurs/{id}/activer
    // ============================================================

    /**
     * Test 7.1 - Activer un utilisateur
     *
     * Objectif : Vérifier que l'API active un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait activer un utilisateur")
    void shouldActivateUtilisateur() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/2/activer")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 8. TESTS PATCH /api/utilisateurs/{id}/desactiver
    // ============================================================

    /**
     * Test 8.1 - Désactiver un utilisateur
     *
     * Objectif : Vérifier que l'API désactive un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait désactiver un utilisateur")
    void shouldDeactivateUtilisateur() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/2/desactiver")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 9. TESTS PATCH /api/utilisateurs/{id}/verrouiller
    // ============================================================

    /**
     * Test 9.1 - Verrouiller un utilisateur
     */
    @Test
    @DisplayName("✅ Devrait verrouiller un utilisateur")
    void shouldLockUtilisateur() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/2/verrouiller")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 10. TESTS PATCH /api/utilisateurs/{id}/deverrouiller
    // ============================================================

    /**
     * Test 10.1 - Déverrouiller un utilisateur
     */
    @Test
    @DisplayName("✅ Devrait déverrouiller un utilisateur")
    void shouldUnlockUtilisateur() throws Exception {
        mockMvc.perform(patch("/api/utilisateurs/2/deverrouiller")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 11. TESTS GET /api/utilisateurs/search
    // ============================================================

    /**
     * Test 11.1 - Rechercher des utilisateurs par mot-clé
     *
     * Objectif : Vérifier que la recherche fonctionne
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste
     */
    @Test
    @DisplayName("✅ Devrait rechercher des utilisateurs par mot-clé")
    void shouldSearchUtilisateurs() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/search?keyword=admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    /**
     * Test 11.2 - Rechercher sans mot-clé
     */
    @Test
    @DisplayName("✅ Devrait retourner tous les utilisateurs si mot-clé vide")
    void shouldSearchWithEmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/search?keyword=")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ============================================================
    // 12. TESTS GET /api/utilisateurs/role/{roleName}
    // ============================================================

    /**
     * Test 12.1 - Récupérer les utilisateurs par rôle
     *
     * Objectif : Vérifier que l'API retourne les utilisateurs d'un rôle
     *
     * Vérifications :
     * - Status 200 OK
     * - La réponse contient une liste
     */
    @Test
    @DisplayName("✅ Devrait récupérer les utilisateurs par rôle")
    void shouldGetUtilisateursByRole() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/role/ROLE_ADMIN")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    /**
     * Test 12.2 - Récupérer les utilisateurs d'un rôle inexistant
     */
    @Test
    @DisplayName("❌ Devrait retourner une liste vide pour un rôle inexistant")
    void shouldReturnEmptyListForNonExistentRole() throws Exception {
        mockMvc.perform(get("/api/utilisateurs/role/ROLE_INEXISTANT")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ============================================================
    // 13. TESTS POST /api/utilisateurs/{id}/roles/{roleName}
    // ============================================================

    /**
     * Test 13.1 - Assigner un rôle à un utilisateur
     *
     * Objectif : Vérifier que l'API assigne un rôle à un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait assigner un rôle à un utilisateur")
    void shouldAssignRole() throws Exception {
        mockMvc.perform(post("/api/utilisateurs/1/roles/ROLE_MANAGER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 14. TESTS DELETE /api/utilisateurs/{id}/roles/{roleName}
    // ============================================================

    /**
     * Test 14.1 - Retirer un rôle d'un utilisateur
     *
     * Objectif : Vérifier que l'API retire un rôle d'un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait retirer un rôle d'un utilisateur")
    void shouldRemoveRole() throws Exception {
        // D'abord assigner le rôle
        mockMvc.perform(post("/api/utilisateurs/1/roles/ROLE_MANAGER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Puis le retirer
        mockMvc.perform(delete("/api/utilisateurs/1/roles/ROLE_MANAGER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 15. TESTS POST /api/utilisateurs/{id}/permissions/{permissionName}
    // ============================================================

    /**
     * Test 15.1 - Ajouter une permission directe
     *
     * Objectif : Vérifier que l'API ajoute une permission directe à un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait ajouter une permission directe")
    void shouldAddDirectPermission() throws Exception {
        mockMvc.perform(post("/api/utilisateurs/1/permissions/CATEGORY_READ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 16. TESTS DELETE /api/utilisateurs/{id}/permissions/{permissionName}
    // ============================================================

    /**
     * Test 16.1 - Retirer une permission directe
     *
     * Objectif : Vérifier que l'API retire une permission directe d'un utilisateur
     *
     * Vérifications :
     * - Status 200 OK
     */
    @Test
    @DisplayName("✅ Devrait retirer une permission directe")
    void shouldRemoveDirectPermission() throws Exception {
        // D'abord ajouter la permission (utiliser une permission qui existe)
        mockMvc.perform(post("/api/utilisateurs/1/permissions/USER_READ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Puis la retirer
        mockMvc.perform(delete("/api/utilisateurs/1/permissions/USER_READ")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // 17. TESTS D'ERREUR - PERMISSIONS
    // ============================================================

    /**
     * Test 17.1 - Accès sans token
     */
    @Test
    @DisplayName("❌ Devrait refuser l'accès sans token")
    void shouldNotAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/utilisateurs"))
                .andExpect(status().isForbidden());
    }
}