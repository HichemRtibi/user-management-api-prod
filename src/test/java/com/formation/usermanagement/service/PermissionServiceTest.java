package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.permision.PermissionDTO;
import com.formation.usermanagement.dto.permision.PermissionRequestDTO;

import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.exception.PermissionDejaExistantException;
import com.formation.usermanagement.exception.PermissionNotFoundException;
import com.formation.usermanagement.exception.PermissionUtiliseException;
import com.formation.usermanagement.mapper.PermissionMapper;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    // ============================================================
    // 1. MOCKS
    // ============================================================

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    // ============================================================
    // 2. DONNÉES DE TEST
    // ============================================================

    private Permission permissionRead;
    private Permission permissionWrite;
    private PermissionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // === CRÉER LES PERMISSIONS ===
        permissionRead = new Permission("USER", "USER_READ", "Lire les utilisateurs");
        permissionRead.setId(1L);

        permissionWrite = new Permission("USER", "USER_WRITE", "Écrire les utilisateurs");
        permissionWrite.setId(2L);

        // === CRÉER LE DTO ===
        requestDTO = PermissionRequestDTO.builder()
                .category("USER")
                .name("USER_READ")
                .description("Lire les utilisateurs")
                .build();
    }

    // ============================================================
    // 3. TESTS CRÉATION (creerPermission)
    // ============================================================

    /**
     * ✅ TEST : creerPermission_DevraitReussir
     *
     * Vérifie qu'une permission est créée correctement.
     */
    @Test
    void creerPermission_DevraitReussir() {
        // GIVEN
        when(permissionRepository.existsByName("USER_READ")).thenReturn(false);
        when(permissionRepository.existsByCategoryAndName("USER", "USER_READ")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permissionRead);

        // WHEN
        PermissionDTO response = permissionService.creerPermission(requestDTO);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("USER_READ");
        assertThat(response.getCategory()).isEqualTo("USER");
        assertThat(response.getDescription()).isEqualTo("Lire les utilisateurs");

        verify(permissionRepository).existsByName("USER_READ");
        verify(permissionRepository).existsByCategoryAndName("USER", "USER_READ");
        verify(permissionRepository).save(any(Permission.class));
    }

    /**
     * ✅ TEST : creerPermission_NomExistant_DevraitEchouer
     *
     * Vérifie qu'on ne peut pas créer une permission avec un nom déjà existant.
     */
    @Test
    void creerPermission_NomExistant_DevraitEchouer() {
        // GIVEN
        when(permissionRepository.existsByName("USER_READ")).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.creerPermission(requestDTO))
                .isInstanceOf(PermissionDejaExistantException.class)
                .hasMessageContaining("La permission USER_READ existe déjà");

        verify(permissionRepository).existsByName("USER_READ");
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    /**
     * ✅ TEST : creerPermission_CombinaisonExistant_DevraitEchouer
     *
     * Vérifie qu'on ne peut pas créer une permission avec une combinaison
     * category + name déjà existante.
     */
    @Test
    void creerPermission_CombinaisonExistant_DevraitEchouer() {
        // GIVEN
        when(permissionRepository.existsByName("USER_READ")).thenReturn(false);
        when(permissionRepository.existsByCategoryAndName("USER", "USER_READ")).thenReturn(true);

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.creerPermission(requestDTO))
                .isInstanceOf(PermissionDejaExistantException.class)
                .hasMessageContaining("La permission USER_USER_READ existe déjà");

        verify(permissionRepository).existsByName("USER_READ");
        verify(permissionRepository).existsByCategoryAndName("USER", "USER_READ");
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ============================================================
    // 4. TESTS RÉCUPÉRATION (getPermission, getPermissionByName)
    // ============================================================

    /**
     * ✅ TEST : getPermission_DevraitReussir
     *
     * Vérifie qu'on peut récupérer une permission par son ID.
     */
    @Test
    void getPermission_DevraitReussir() {
        // GIVEN
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));

        // WHEN
        PermissionDTO response = permissionService.getPermission(1L);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("USER_READ");

        verify(permissionRepository).findById(1L);
    }

    /**
     * ✅ TEST : getPermission_Inexistant_DevraitEchouer
     */
    @Test
    void getPermission_Inexistant_DevraitEchouer() {
        // GIVEN
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.getPermission(999L))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessageContaining("ID: 999");

        verify(permissionRepository).findById(999L);
    }

    /**
     * ✅ TEST : getPermissionByName_DevraitReussir
     */
    @Test
    void getPermissionByName_DevraitReussir() {
        // GIVEN
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permissionRead));

        // WHEN
        PermissionDTO response = permissionService.getPermissionByName("USER_READ");

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("USER_READ");

        verify(permissionRepository).findByName("USER_READ");
    }

    // ============================================================
    // 5. TESTS LISTES (getAllPermissions, getPermissionsByCategory)
    // ============================================================

    /**
     * ✅ TEST : getAllPermissions_DevraitRetournerUnePage
     */
    @Test
    void getAllPermissions_DevraitRetournerUnePage() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> page = new PageImpl<>(List.of(permissionRead, permissionWrite), pageable, 2);
        when(permissionRepository.findAll(pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<PermissionDTO> response = permissionService.getAllPermissions(pageable);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getContent().get(0).getName()).isEqualTo("USER_READ");
        assertThat(response.getContent().get(1).getName()).isEqualTo("USER_WRITE");

        verify(permissionRepository).findAll(pageable);
    }

    /**
     * ✅ TEST : getAllPermissionsList_DevraitRetournerToutesLesPermissions
     */
    @Test
    void getAllPermissionsList_DevraitRetournerToutesLesPermissions() {
        // GIVEN
        List<Permission> permissions = List.of(permissionRead, permissionWrite);
        when(permissionRepository.findAllByOrderByCategoryAscNameAsc()).thenReturn(permissions);

        // WHEN
        List<PermissionDTO> response = permissionService.getAllPermissionsList();

        // THEN
        assertThat(response).hasSize(2);
        assertThat(response).extracting("name").contains("USER_READ", "USER_WRITE");

        verify(permissionRepository).findAllByOrderByCategoryAscNameAsc();
    }

    /**
     * ✅ TEST : getPermissionsByCategory_DevraitReussir
     */
    @Test
    void getPermissionsByCategory_DevraitReussir() {
        // GIVEN
        List<Permission> permissions = List.of(permissionRead, permissionWrite);
        when(permissionRepository.findByCategoryOrderByNameAsc("USER")).thenReturn(permissions);

        // WHEN
        List<PermissionDTO> response = permissionService.getPermissionsByCategory("USER");

        // THEN
        assertThat(response).hasSize(2);
        assertThat(response).extracting("category").containsOnly("USER");

        verify(permissionRepository).findByCategoryOrderByNameAsc("USER");
    }

    @Test
    void getPermissionsByCategory_CategorieVide_DevraitRetournerListeVide() {
        // GIVEN
        when(permissionRepository.findByCategoryOrderByNameAsc("INEXISTANT")).thenReturn(List.of());

        // WHEN
        List<PermissionDTO> response = permissionService.getPermissionsByCategory("INEXISTANT");

        // THEN
        assertThat(response).isEmpty();
        verify(permissionRepository).findByCategoryOrderByNameAsc("INEXISTANT");
    }

    // ============================================================
    // 6. TESTS MISE À JOUR (updatePermission)
    // ============================================================

    /**
     * ✅ TEST : updatePermission_DevraitReussir
     */
    @Test
    void updatePermission_DevraitReussir() {
        // GIVEN
        PermissionRequestDTO updateDTO = PermissionRequestDTO.builder()
                .category("USER")
                .name("USER_READ")  // ← Garde le même nom
                .description("Nouvelle description")
                .build();

        // Créer une permission avec la nouvelle description
        Permission permissionUpdated = new Permission("USER", "USER_READ", "Nouvelle description");
        permissionUpdated.setId(1L);

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findByCategoryAndName("USER", "USER_READ")).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.save(any(Permission.class))).thenReturn(permissionUpdated);

        // WHEN
        PermissionDTO response = permissionService.updatePermission(1L, updateDTO);

        // THEN
        assertThat(response).isNotNull();
        // ✅ Vérifier que le nom reste le même
        assertThat(response.getName()).isEqualTo("USER_READ");
        // ✅ Vérifier que la description a changé
        assertThat(response.getDescription()).isEqualTo("Nouvelle description");

        verify(permissionRepository).findById(1L);
        verify(permissionRepository).save(any(Permission.class));
    }

    /**
     * ✅ TEST : updatePermission_NomDejaUtilise_DevraitEchouer
     */
    @Test
    void updatePermission_NomDejaUtilise_DevraitEchouer() {
        // GIVEN
        PermissionRequestDTO updateDTO = PermissionRequestDTO.builder()
                .category("USER")
                .name("USER_WRITE")
                .description("Permission modifiée")
                .build();

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findByName("USER_WRITE")).thenReturn(Optional.of(permissionWrite));

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.updatePermission(1L, updateDTO))
                .isInstanceOf(PermissionDejaExistantException.class)
                .hasMessageContaining("La permission USER_WRITE existe déjà");

        verify(permissionRepository).findById(1L);
        verify(permissionRepository).findByName("USER_WRITE");
        verify(permissionRepository, never()).save(any(Permission.class));
    }

    // ============================================================
    // 7. TESTS SUPPRESSION (supprimerPermission)
    // ============================================================

    /**
     * ✅ TEST : supprimerPermission_DevraitReussir
     */
    @Test
    void supprimerPermission_DevraitReussir() {
        // GIVEN
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(roleRepository.countRolesByPermissionName("USER_READ")).thenReturn(0L);

        // WHEN
        permissionService.supprimerPermission(1L);

        // THEN
        verify(permissionRepository).findById(1L);
        verify(roleRepository).countRolesByPermissionName("USER_READ");
        verify(permissionRepository).deleteById(1L);
    }

    /**
     * ✅ TEST : supprimerPermission_Utilisee_DevraitEchouer
     */
    @Test
    void supprimerPermission_Utilisee_DevraitEchouer() {
        // GIVEN
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(roleRepository.countRolesByPermissionName("USER_READ")).thenReturn(3L);

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.supprimerPermission(1L))
                .isInstanceOf(PermissionUtiliseException.class)
                .hasMessageContaining("utilisée par 3 rôle(s)");

        verify(permissionRepository).findById(1L);
        verify(roleRepository).countRolesByPermissionName("USER_READ");
        verify(permissionRepository, never()).deleteById(anyLong());
    }

    /**
     * ✅ TEST : supprimerPermission_Inexistante_DevraitEchouer
     */
    @Test
    void supprimerPermission_Inexistante_DevraitEchouer() {
        // GIVEN
        when(permissionRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> permissionService.supprimerPermission(999L))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessageContaining("ID: 999");

        verify(permissionRepository).findById(999L);
        verify(permissionRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // 8. TESTS VALIDATION
    // ============================================================

    @Test
    void existeParNom_DevraitRetournerTrue_QuandPermissionExiste() {
        // GIVEN
        when(permissionRepository.existsByName("USER_READ")).thenReturn(true);

        // WHEN
        boolean existe = permissionService.existeParNom("USER_READ");

        // THEN
        assertThat(existe).isTrue();
        verify(permissionRepository).existsByName("USER_READ");
    }

    @Test
    void existeParNom_DevraitRetournerFalse_QuandPermissionNExistePas() {
        // GIVEN
        when(permissionRepository.existsByName("INEXISTANT")).thenReturn(false);

        // WHEN
        boolean existe = permissionService.existeParNom("INEXISTANT");

        // THEN
        assertThat(existe).isFalse();
        verify(permissionRepository).existsByName("INEXISTANT");
    }

    @Test
    void existeParCategoryEtNom_DevraitRetournerTrue_QuandCombinaisonExiste() {
        // GIVEN
        when(permissionRepository.existsByCategoryAndName("USER", "USER_READ")).thenReturn(true);

        // WHEN
        boolean existe = permissionService.existeParCategoryEtNom("USER", "USER_READ");

        // THEN
        assertThat(existe).isTrue();
        verify(permissionRepository).existsByCategoryAndName("USER", "USER_READ");
    }

    // ============================================================
    // 9. TEST : ArgumentCaptor
    // ============================================================

    /**
     * ✅ TEST : Utilisation d'ArgumentCaptor
     *
     * Vérifie que les bonnes valeurs sont passées lors de la sauvegarde.
     */
    @Test
    void creerPermission_DevraitPasserLesBonnesValeursALaSauvegarde() {
        // GIVEN
        when(permissionRepository.existsByName("USER_READ")).thenReturn(false);
        when(permissionRepository.existsByCategoryAndName("USER", "USER_READ")).thenReturn(false);
        when(permissionRepository.save(any(Permission.class))).thenReturn(permissionRead);

        // WHEN
        permissionService.creerPermission(requestDTO);

        // THEN
        ArgumentCaptor<Permission> permissionCaptor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(permissionCaptor.capture());

        Permission captured = permissionCaptor.getValue();
        assertThat(captured.getCategory()).isEqualTo("USER");
        assertThat(captured.getName()).isEqualTo("USER_READ");
        assertThat(captured.getDescription()).isEqualTo("Lire les utilisateurs");
    }
}