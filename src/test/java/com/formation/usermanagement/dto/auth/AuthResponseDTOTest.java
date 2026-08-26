package com.formation.usermanagement.dto.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseDTOTest {

    @Test
    void success_DevraitCreerUneReponseDeSucces() {
        // GIVEN
        String message = "Connexion réussie";
        Object data = "Données de test";

        // WHEN
        AuthResponseDTO response = AuthResponseDTO.success(message, data);

        // THEN
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Connexion réussie");
        assertThat(response.getData()).isEqualTo("Données de test");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void error_DevraitCreerUneReponseDErreur() {
        // GIVEN
        String message = "Email ou mot de passe incorrect";

        // WHEN
        AuthResponseDTO response = AuthResponseDTO.error(message);

        // THEN
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Email ou mot de passe incorrect");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
}