package com.formation.usermanagement.mapper;

import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.entity.Permission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionMapperTest {

    @Test
    void toDTO_DevraitConvertirPermissionEnDTO() {
        // GIVEN
        Permission permission = new Permission("USER", "USER_READ", "Lire les utilisateurs");
        permission.setId(1L);

        // WHEN
        PermissionDTO dto = PermissionMapper.toDTO(permission);

        // THEN
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCategory()).isEqualTo("USER");
        assertThat(dto.getName()).isEqualTo("USER_READ");
        assertThat(dto.getDescription()).isEqualTo("Lire les utilisateurs");
    }



    @Test
    void toDTO_DevraitRetournerNull_SiPermissionEstNull() {
        // WHEN
        PermissionDTO dto = PermissionMapper.toDTO(null);

        // THEN
        assertThat(dto).isNull();
    }
}