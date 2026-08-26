package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleListDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleMapperTest {

    private Role role;
    private Permission permission1;
    private Permission permission2;

    @BeforeEach
    void setUp() {
        permission1 = new Permission("USER", "USER_READ", "Lire");
        permission1.setId(1L);

        permission2 = new Permission("USER", "USER_WRITE", "Écrire");
        permission2.setId(2L);

        role = new Role("ROLE_ADMIN", "Administrateur");
        role.setId(1L);
        role.addPermission(permission1);
        role.addPermission(permission2);
        role.setCreatedAt(LocalDateTime.now().minusDays(10));
        role.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void toEntity_DevraitConvertirRoleRequestEnEntite() {
        // GIVEN
        RoleRequestDTO dto = RoleRequestDTO.builder()
                .name("ROLE_MANAGER")
                .description("Manager")
                .permissionIds(Set.of(1L, 2L))
                .build();

        // WHEN
        Role result = RoleMapper.toEntity(dto);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_MANAGER");
        assertThat(result.getDescription()).isEqualTo("Manager");
        assertThat(result.getPermissions()).isEmpty();  // Les permissions ne sont pas gérées ici
    }

    @Test
    void updateEntity_DevraitMettreAJourLeRole() {
        // GIVEN
        RoleRequestDTO dto = RoleRequestDTO.builder()
                .name("ROLE_SUPER_ADMIN")
                .description("Super Administrateur")
                .build();

        // WHEN
        RoleMapper.updateEntity(dto, role);

        // THEN
        assertThat(role.getName()).isEqualTo("ROLE_SUPER_ADMIN");
        assertThat(role.getDescription()).isEqualTo("Super Administrateur");
        assertThat(role.getId()).isEqualTo(1L);  // L'ID reste inchangé
    }

    @Test
    void toDTO_DevraitConvertirRoleEnDTOComplet() {
        // WHEN
        RoleDTO dto = RoleMapper.toDTO(role);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(dto.getDescription()).isEqualTo("Administrateur");
        assertThat(dto.getPermissions()).hasSize(2);
        assertThat(dto.getPermissions())
                .extracting("name")
                .contains("USER_READ", "USER_WRITE");
        assertThat(dto.getCreatedAt()).isNotNull();
        assertThat(dto.getUpdatedAt()).isNotNull();
    }

    @Test
    void toListDTO_DevraitConvertirRoleEnDTOLege() {
        // WHEN
        RoleListDTO dto = RoleMapper.toListDTO(role);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(dto.getDescription()).isEqualTo("Administrateur");
        assertThat(dto.getPermissionNames()).hasSize(2);
        assertThat(dto.getPermissionNames()).contains("USER_READ", "USER_WRITE");
    }

    @Test
    void toDTO_DevraitRetournerNull_SiRoleEstNull() {
        // WHEN
        RoleDTO dto = RoleMapper.toDTO(null);

        // THEN
        assertThat(dto).isNull();
    }
}