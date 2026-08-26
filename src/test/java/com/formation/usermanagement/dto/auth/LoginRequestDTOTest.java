package com.formation.usermanagement.dto.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validateur_DevraitAccepterUnLoginValide() {
        // GIVEN
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .email("jean.dupont@email.com")
                .motDePasse("Password123@")
                .build();

        // WHEN
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations).isEmpty();
    }

    @Test
    void validateur_DevraitRejeter_QuandEmailEstVide() {
        // GIVEN
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .email("")
                .motDePasse("Password123@")
                .build();

        // WHEN
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("L'email est obligatoire");
    }

    @Test
    void validateur_DevraitRejeter_QuandEmailEstInvalide() {
        // GIVEN
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .email("email_invalide")
                .motDePasse("Password123@")
                .build();

        // WHEN
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("L'email doit être valide");
    }

    @Test
    void validateur_DevraitRejeter_QuandMotDePasseEstVide() {
        // GIVEN
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .email("jean.dupont@email.com")
                .motDePasse("")
                .build();

        // WHEN
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("Le mot de passe est obligatoire");
    }

    @Test
    void validateur_DevraitRejeter_QuandEmailEtMotDePasseSontVides() {
        // GIVEN
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .email("")
                .motDePasse("")
                .build();

        // WHEN
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .extracting("message")
                .contains("L'email est obligatoire", "Le mot de passe est obligatoire");
    }
}