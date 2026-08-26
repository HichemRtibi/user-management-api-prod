package com.formation.usermanagement.dto.utilisateur;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurListDTOTest {

    @Test
    void getNomComplet_DevraitRetournerPrenomEtNom() {
        // GIVEN
        UtilisateurListDTO dto = new UtilisateurListDTO();
        dto.setPrenom("Jean");
        dto.setNom("Dupont");

        // WHEN
        String nomComplet = dto.getNomComplet();

        // THEN
        assertThat(nomComplet).isEqualTo("Jean Dupont");
    }

    @Test
    void dto_DevraitAccepterTousLesChamps() {
        // GIVEN
        LocalDateTime now = LocalDateTime.now();

        UtilisateurListDTO dto = new UtilisateurListDTO();
        dto.setId(1L);
        dto.setPrenom("Jean");
        dto.setNom("Dupont");
        dto.setEmail("jean.dupont@email.com");
        dto.setEnabled(true);
        dto.setRoles(Set.of("ROLE_ADMIN"));
        dto.setCreatedAt(now);

        // THEN
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPrenom()).isEqualTo("Jean");
        assertThat(dto.getNom()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void dto_DevraitAccepterDesValeursNull() {
        // GIVEN
        UtilisateurListDTO dto = new UtilisateurListDTO();
        dto.setPrenom(null);
        dto.setNom(null);
        dto.setRoles(null);

        // THEN
        assertThat(dto.getPrenom()).isNull();
        assertThat(dto.getNom()).isNull();
        assertThat(dto.getRoles()).isNull();
        assertThat(dto.getNomComplet()).isEqualTo("null null");
    }

    @Test
    void dto_DevraitEtrePlusLegereQueUtilisateurResponseDTO() {
        // GIVEN
        UtilisateurListDTO listDTO = new UtilisateurListDTO();
        UtilisateurResponseDTO responseDTO = new UtilisateurResponseDTO();

        // Vérification des champs présents dans UtilisateurResponseDTO
        // mais ABSENTS dans UtilisateurListDTO
        assertThat(responseDTO.getClass().getDeclaredFields())
                .extracting("name")
                .contains("compteNonVerrouille", "compteNonExpire",
                        "credentialsNonExpire", "derniereConnexion",
                        "permissions", "updatedAt");

        assertThat(listDTO.getClass().getDeclaredFields())
                .extracting("name")
                .doesNotContain("compteNonVerrouille", "compteNonExpire",
                        "credentialsNonExpire", "derniereConnexion",
                        "permissions", "updatedAt");
    }
}