package com.formation.usermanagement.dto.utilisateur;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UtilisateurResponseDTOTest {

    @Test
    void getNomComplet_DevraitRetournerPrenomEtNom() {
        // GIVEN
        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
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

        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();
        dto.setId(1L);
        dto.setPrenom("Jean");
        dto.setNom("Dupont");
        dto.setEmail("jean.dupont@email.com");
        dto.setEnabled(true);
        dto.setCompteNonVerrouille(true);
        dto.setCompteNonExpire(true);
        dto.setCredentialsNonExpire(true);
        dto.setRoles(Set.of("ROLE_ADMIN"));
        dto.setPermissions(Set.of("USER_READ", "USER_WRITE"));
        dto.setDerniereConnexion(now);
        dto.setCreatedAt(now.minusDays(10));
        dto.setUpdatedAt(now);

        // THEN
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPrenom()).isEqualTo("Jean");
        assertThat(dto.getNom()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.isCompteNonVerrouille()).isTrue();
        assertThat(dto.isCompteNonExpire()).isTrue();
        assertThat(dto.isCredentialsNonExpire()).isTrue();
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
        assertThat(dto.getPermissions()).contains("USER_READ", "USER_WRITE");
        assertThat(dto.getDerniereConnexion()).isEqualTo(now);
        assertThat(dto.getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void dto_DevraitAccepterDesValeursNull() {
        // GIVEN
        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();

        // WHEN
        dto.setPrenom(null);
        dto.setNom(null);
        dto.setRoles(null);
        dto.setPermissions(null);

        // THEN
        assertThat(dto.getPrenom()).isNull();
        assertThat(dto.getNom()).isNull();
        assertThat(dto.getRoles()).isNull();
        assertThat(dto.getPermissions()).isNull();
        assertThat(dto.getNomComplet()).isEqualTo("null null");  // ⚠️ Comportement attendu
    }
}