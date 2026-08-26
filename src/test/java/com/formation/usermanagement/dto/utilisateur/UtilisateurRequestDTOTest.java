package com.formation.usermanagement.dto.utilisateur;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurRequestDTOTest {

    private static Validator validator;

    // ⚠️ Mot de passe VALIDE selon notre regex
    private static final String VALID_PASSWORD = "Password123@";  // ← @ est autorisé
    private static final String VALID_EMAIL = "jean.dupont@email.com";
    private static final String VALID_PRENOM = "Jean";
    private static final String VALID_NOM = "Dupont";

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ============================================================
    // TEST 1 : DTO VALIDE
    // ============================================================

    @Test
    void validateur_DevraitAccepterUnDTOValide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse(VALID_PASSWORD);  // ✅ VALIDE

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations).isEmpty();
    }

    // ============================================================
    // TEST 2 : PRÉNOM - Erreurs
    // ============================================================

    @Test
    void validateur_DevraitRejeter_QuandPrenomEstVide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom("");
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN - @NotBlank + @Size sur prenom
        assertThat(violations)
                .extracting("message")
                .contains("Le prénom est obligatoire",
                        "Le prénom doit contenir entre 2 et 50 caractères");
    }

    @Test
    void validateur_DevraitRejeter_QuandPrenomEstTropLong() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom("JeanJeanJeanJeanJeanJeanJeanJeanJeanJeanJeanJeanJean");  // > 50 caractères
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("Le prénom doit contenir entre 2 et 50 caractères");
    }

    @Test
    void validateur_DevraitRejeter_QuandPrenomContientDesChiffres() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom("Jean123");
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("Le prénom ne doit contenir que des lettres, des espaces ou des tirets");
    }

    // ============================================================
    // TEST 3 : NOM - Erreurs
    // ============================================================

    @Test
    void validateur_DevraitRejeter_QuandNomEstVide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom("");
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("Le nom est obligatoire",
                        "Le nom doit contenir entre 2 et 50 caractères");
    }

    // ============================================================
    // TEST 4 : EMAIL - Erreurs
    // ============================================================

    @Test
    void validateur_DevraitRejeter_QuandEmailEstInvalide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail("email_invalide");
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("L'email doit être valide (ex: user@domain.com)");
    }

    @Test
    void validateur_DevraitRejeter_QuandEmailEstVide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail("");
        dto.setMotDePasse(VALID_PASSWORD);

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN - Seulement @NotBlank sur email
        assertThat(violations)
                .extracting("message")
                .contains("L'email est obligatoire");
    }

    // ============================================================
    // TEST 5 : MOT DE PASSE - Erreurs
    // ============================================================

    @Test
    void validateur_DevraitRejeter_QuandMotDePasseEstTropCourt() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse("123");

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN - @Size + @Pattern sur motDePasse
        assertThat(violations)
                .extracting("message")
                .contains("Le mot de passe doit contenir au moins 8 caractères",
                        "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
    }

    @Test
    void validateur_DevraitRejeter_QuandMotDePasseNaPasLesCaracteresRequis() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse("password123");

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN
        assertThat(violations)
                .extracting("message")
                .contains("Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
    }

    @Test
    void validateur_DevraitRejeter_QuandMotDePasseEstVide() {
        // GIVEN
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom(VALID_PRENOM);
        dto.setNom(VALID_NOM);
        dto.setEmail(VALID_EMAIL);
        dto.setMotDePasse("");

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN - @NotBlank + @Size + @Pattern sur motDePasse
        assertThat(violations)
                .extracting("message")
                .contains("Le mot de passe est obligatoire",
                        "Le mot de passe doit contenir au moins 8 caractères",
                        "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial");
    }

    // ============================================================
    // TEST 6 : MULTIPLES ERREURS
    // ============================================================

    @Test
    void validateur_DevraitRejeter_QuandPlusieursChampsSontInvalides() {
        // GIVEN - Tous les champs invalides
        UtilisateurRequestDTO dto = new UtilisateurRequestDTO();
        dto.setPrenom("");        // ❌ VIDE
        dto.setNom("");           // ❌ VIDE
        dto.setEmail("invalide"); // ❌ INVALIDE
        dto.setMotDePasse("123"); // ❌ TROP COURT

        // WHEN
        Set<ConstraintViolation<UtilisateurRequestDTO>> violations = validator.validate(dto);

        // THEN - On vérifie qu'il y a plusieurs violations
        assertThat(violations).hasSizeGreaterThan(3);
        assertThat(violations)
                .extracting("message")
                .contains("Le prénom est obligatoire",
                        "Le nom est obligatoire",
                        "L'email doit être valide (ex: user@domain.com)");
    }
}