package com.formation.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.usermanagement.dto.auth.LoginRequestDTO;
import com.formation.usermanagement.dto.auth.RegisterRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============================================================
    // 1. TESTS LOGIN
    // ============================================================

    /**
     * Test 1.1 - Connexion réussie
     *
     * Objectif : Vérifier qu'un utilisateur avec des identifiants valides peut se connecter
     * Données   : admin@example.com / Admin123!
     *
     * Vérifications :
     * - Status 200 OK
     * - Token JWT généré
     * - Type "Bearer"
     * - Email retourné correctement
     */
    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setMotDePasse("Admin123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.utilisateur.email").value("admin@example.com"));
    }

    /**
     * Test 1.2 - Connexion échouée (mauvais mot de passe)
     */
    @Test
    void shouldFailLoginWithWrongPassword() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setMotDePasse("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 1.3 - Connexion échouée (email inexistant)
     */
    @Test
    void shouldFailLoginWithNonExistentEmail() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("inexistant@example.com");
        loginRequest.setMotDePasse("Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Test 1.4 - Connexion échouée (email vide)
     */
    @Test
    void shouldFailLoginWithEmptyEmail() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("");
        loginRequest.setMotDePasse("Admin123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // 2. TESTS REGISTER
    // ============================================================

    /**
     * Test 2.1 - Inscription réussie
     */
    @Test
    void shouldRegisterSuccessfully() throws Exception {
        // ✅ Utiliser un email unique avec timestamp
        String uniqueEmail = "testregister" + System.currentTimeMillis() + "@email.com";

        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setPrenom("Test");
        registerRequest.setNom("User");
        registerRequest.setEmail(uniqueEmail);
        registerRequest.setMotDePasse("Password123@");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.utilisateur.email").value(uniqueEmail));
    }
    /**
     * Test 2.2 - Inscription échouée (email déjà existant)
     */

        @Test
        void shouldFailRegisterWithExistingEmail() throws Exception {
            RegisterRequestDTO registerRequest = new RegisterRequestDTO();
            registerRequest.setPrenom("Admin");
            registerRequest.setNom("System");
            registerRequest.setEmail("admin@example.com");
            registerRequest.setMotDePasse("Password123@");
            registerRequest.setConfirmationMotDePasse("Password123@");  // ✅ AJOUTER

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isConflict());
        }

    /**
     * Test 2.3 - Inscription échouée (email invalide)
     */
    @Test
    void shouldFailRegisterWithInvalidEmail() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setPrenom("Test");
        registerRequest.setNom("User");
        registerRequest.setEmail("invalid-email");
        registerRequest.setMotDePasse("Password123@");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 2.4 - Inscription échouée (mot de passe trop faible)
     */
    @Test
    void shouldFailRegisterWithWeakPassword() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setPrenom("Test");
        registerRequest.setNom("User");
        registerRequest.setEmail("testweak@email.com");
        registerRequest.setMotDePasse("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 2.5 - Inscription échouée (prenom vide)
     */
    @Test
    void shouldFailRegisterWithEmptyPrenom() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setPrenom("");
        registerRequest.setNom("User");
        registerRequest.setEmail("testempty@email.com");
        registerRequest.setMotDePasse("Password123@");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 2.6 - Inscription échouée (nom vide)
     */
    @Test
    void shouldFailRegisterWithEmptyNom() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO();
        registerRequest.setPrenom("Test");
        registerRequest.setNom("");
        registerRequest.setEmail("testnomempty@email.com");
        registerRequest.setMotDePasse("Password123@");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}