package com.formation.usermanagement.dto.auth;

import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseDTOTest {

    @Test
    void builder_DevraitCreerUnLoginResponseComplet() {
        // GIVEN
        UtilisateurResponseDTO utilisateur = UtilisateurResponseDTO.builder()
                .id(1L)
                .prenom("Jean")
                .nom("Dupont")
                .email("jean.dupont@email.com")
                .roles(Set.of("ROLE_ADMIN"))
                .permissions(Set.of("USER_READ", "USER_WRITE"))
                .build();

        // WHEN
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .type("Bearer")
                .utilisateur(utilisateur)
                .build();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUtilisateur()).isNotNull();
        assertThat(response.getUtilisateur().getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getUtilisateur().getRoles()).contains("ROLE_ADMIN");
    }

    @Test
    void constructeurSimplifie_DevraitCreerUnLoginResponse() {
        // GIVEN
        UtilisateurResponseDTO utilisateur = UtilisateurResponseDTO.builder()
                .id(1L)
                .email("test@email.com")
                .build();

        // WHEN
        LoginResponseDTO response = new LoginResponseDTO("monToken123", utilisateur);

        // THEN
        assertThat(response.getToken()).isEqualTo("monToken123");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUtilisateur()).isNotNull();
        assertThat(response.getUtilisateur().getEmail()).isEqualTo("test@email.com");
    }
}