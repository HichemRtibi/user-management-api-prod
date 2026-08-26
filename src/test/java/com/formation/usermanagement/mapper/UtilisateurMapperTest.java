package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.utilisateur.UtilisateurListDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TEST DU MAPPER UTILISATEUR (Version Builder)
 *
 * ✅ Teste toutes les méthodes du Mapper
 * ✅ Vérifie que le Builder fonctionne correctement
 * ✅ Vérifie les conversions avec des données réelles
 */
class UtilisateurMapperTest {

    private Utilisateur utilisateur;
    private Role roleAdmin;
    private Permission permissionRead;
    private Permission permissionWrite;

    @BeforeEach
    void setUp() {
        // === CRÉER LES PERMISSIONS ===
        permissionRead = new Permission("USER", "USER_READ", "Lire les utilisateurs");
        permissionWrite = new Permission("USER", "USER_WRITE", "Écrire les utilisateurs");

        // === CRÉER LE RÔLE ===
        roleAdmin = new Role("ROLE_ADMIN", "Administrateur");
        roleAdmin.addPermission(permissionRead);
        roleAdmin.addPermission(permissionWrite);

        // === CRÉER L'UTILISATEUR ===
        utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setPrenom("Jean");
        utilisateur.setNom("Dupont");
        utilisateur.setEmail("jean.dupont@email.com");
        utilisateur.setMotDePasse("encodedPassword123@");
        utilisateur.setEnabled(true);
        utilisateur.setCompteNonVerrouille(true);
        utilisateur.setCompteNonExpire(true);
        utilisateur.setCredentialsNonExpire(true);
        utilisateur.addRole(roleAdmin);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateur.setCreatedAt(LocalDateTime.now().minusDays(10));
        utilisateur.setUpdatedAt(LocalDateTime.now());
    }

    // ============================================================
    // 1. TESTS : toEntity (DTO → Entité)
    // ============================================================

    @Test
    void toEntity_DevraitConvertirDTOEnEntite_AvecBuilder() {
        // GIVEN - Utilisation du Builder pour créer le DTO
        UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                .prenom("Marie")
                .nom("Martin")
                .email("marie.martin@email.com")
                .motDePasse("Password123@")
                .build();

        // WHEN
        Utilisateur result = UtilisateurMapper.toEntity(dto);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getPrenom()).isEqualTo("Marie");
        assertThat(result.getNom()).isEqualTo("Martin");
        assertThat(result.getEmail()).isEqualTo("marie.martin@email.com");
        assertThat(result.getMotDePasse()).isEqualTo("Password123@");

        // Vérifier les valeurs par défaut
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isCompteNonVerrouille()).isTrue();
        assertThat(result.isCompteNonExpire()).isTrue();
        assertThat(result.isCredentialsNonExpire()).isTrue();
    }

    @Test
    void toEntity_DevraitRetournerNull_SiDtoEstNull() {
        // WHEN
        Utilisateur result = UtilisateurMapper.toEntity(null);

        // THEN
        assertThat(result).isNull();
    }

    // ============================================================
    // 2. TESTS : updateEntity (DTO → Entité existante)
    // ============================================================

    @Test
    void updateEntity_DevraitMettreAJourLEntite_AvecBuilder() {
        // GIVEN
        UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                .prenom("Pierre")
                .nom("Durand")
                .email("pierre.durand@email.com")
                .motDePasse("NewPassword123@")
                .build();

        // WHEN
        UtilisateurMapper.updateEntity(dto, utilisateur);

        // THEN
        assertThat(utilisateur.getPrenom()).isEqualTo("Pierre");
        assertThat(utilisateur.getNom()).isEqualTo("Durand");
        assertThat(utilisateur.getEmail()).isEqualTo("pierre.durand@email.com");
        assertThat(utilisateur.getMotDePasse()).isEqualTo("NewPassword123@");
        assertThat(utilisateur.getId()).isEqualTo(1L);
    }

    @Test
    void updateEntity_DevraitIgnorerMotDePasse_SiNonFourni() {
        // GIVEN
        String oldPassword = utilisateur.getMotDePasse();
        UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                .prenom("Pierre")
                .nom("Durand")
                .email("pierre.durand@email.com")
                .motDePasse(null)
                .build();

        // WHEN
        UtilisateurMapper.updateEntity(dto, utilisateur);

        // THEN - Le mot de passe n'a pas changé
        assertThat(utilisateur.getMotDePasse()).isEqualTo(oldPassword);
        assertThat(utilisateur.getPrenom()).isEqualTo("Pierre");
    }

    // ============================================================
    // 3. TESTS : toResponseDTO (Entité → DTO complet avec Builder)
    // ============================================================

    @Test
    void toResponseDTO_DevraitConvertirEntiteEnDTO_AvecBuilder() {
        // WHEN
        UtilisateurResponseDTO dto = UtilisateurMapper.toResponseDTO(utilisateur);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPrenom()).isEqualTo("Jean");
        assertThat(dto.getNom()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(dto.getNomComplet()).isEqualTo("Jean Dupont");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.isCompteNonVerrouille()).isTrue();
        assertThat(dto.isCompteNonExpire()).isTrue();
        assertThat(dto.isCredentialsNonExpire()).isTrue();
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
        assertThat(dto.getPermissions()).contains("USER_READ", "USER_WRITE");
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    void toResponseDTO_NeDevraitJamaisInclureLeMotDePasse() {
        // WHEN
        UtilisateurResponseDTO dto = UtilisateurMapper.toResponseDTO(utilisateur);

        // THEN - Vérifier que le mot de passe n'est pas dans le DTO
        assertThat(dto.getClass().getDeclaredFields())
                .extracting("name")
                .doesNotContain("motDePasse");
    }

    @Test
    void toResponseDTO_DevraitRetournerNull_SiEntiteEstNull() {
        // WHEN
        UtilisateurResponseDTO dto = UtilisateurMapper.toResponseDTO(null);

        // THEN
        assertThat(dto).isNull();
    }

    @Test
    void toResponseDTO_DevraitGererDesRolesVides_AvecBuilder() {
        // GIVEN - Utilisateur sans rôles
        Utilisateur user = new Utilisateur();
        user.setPrenom("Sans");
        user.setNom("Role");
        user.setEmail("sans.role@email.com");
        user.setMotDePasse("Password123@");

        // WHEN
        UtilisateurResponseDTO dto = UtilisateurMapper.toResponseDTO(user);

        // THEN
        assertThat(dto.getRoles()).isNotNull();
        assertThat(dto.getRoles()).isEmpty();
        assertThat(dto.getPermissions()).isNotNull();
        assertThat(dto.getPermissions()).isEmpty();
    }

    // ============================================================
    // 4. TESTS : toListDTO (Entité → DTO léger avec Builder)
    // ============================================================

    @Test
    void toListDTO_DevraitConvertirEntiteEnDTOLege_AvecBuilder() {
        // WHEN
        UtilisateurListDTO dto = UtilisateurMapper.toListDTO(utilisateur);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getPrenom()).isEqualTo("Jean");
        assertThat(dto.getNom()).isEqualTo("Dupont");
        assertThat(dto.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(dto.getNomComplet()).isEqualTo("Jean Dupont");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getRoles()).contains("ROLE_ADMIN");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void toListDTO_DevraitEtrePlusLegeQueResponseDTO() {
        // WHEN
        UtilisateurListDTO listDTO = UtilisateurMapper.toListDTO(utilisateur);

        // THEN - Vérifier les champs absents (version légère)
        assertThat(listDTO.getClass().getDeclaredFields())
                .extracting("name")
                .doesNotContain("compteNonVerrouille", "compteNonExpire",
                        "credentialsNonExpire", "permissions",
                        "derniereConnexion", "updatedAt");
    }

    // ============================================================
    // 5. TEST : VÉRIFICATION DU BUILDER DANS LES DTOS
    // ============================================================

    @Test
    void builder_DevraitCreerDesDTOsCorrectement() {
        // GIVEN - Construction avec Builder
        UtilisateurResponseDTO dto = UtilisateurResponseDTO.builder()
                .id(100L)
                .prenom("Test")
                .nom("Builder")
                .email("test.builder@email.com")
                .enabled(false)
                .roles(Set.of("ROLE_TEST"))
                .permissions(Set.of("TEST_READ"))
                .build();

        // THEN
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getPrenom()).isEqualTo("Test");
        assertThat(dto.getNom()).isEqualTo("Builder");
        assertThat(dto.getNomComplet()).isEqualTo("Test Builder");
        assertThat(dto.isEnabled()).isFalse();
        assertThat(dto.getRoles()).contains("ROLE_TEST");
        assertThat(dto.getPermissions()).contains("TEST_READ");
    }
}