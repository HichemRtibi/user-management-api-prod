package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.role.RoleDTO;
import com.formation.usermanagement.dto.role.RoleRequestDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.exception.*;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service Role")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role roleAdmin;
    private Role roleUser;
    private Permission permissionRead;
    private Permission permissionWrite;
    private RoleRequestDTO roleRequestDTO;

    @BeforeEach
    void setUp() {
        // Initialisation des données de test
        roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setName("ROLE_ADMIN");
        roleAdmin.setDescription("Administrateur avec tous les droits");
        roleAdmin.setPermissions(new HashSet<>());

        roleUser = new Role();
        roleUser.setId(2L);
        roleUser.setName("ROLE_USER");
        roleUser.setDescription("Utilisateur standard");
        roleUser.setPermissions(new HashSet<>());

        permissionRead = new Permission();
        permissionRead.setId(1L);
        permissionRead.setName("USER_READ");
        permissionRead.setCategory("USER");

        permissionWrite = new Permission();
        permissionWrite.setId(2L);
        permissionWrite.setName("USER_WRITE");
        permissionWrite.setCategory("USER");

        roleRequestDTO = RoleRequestDTO.builder()
                .name("ROLE_MANAGER")
                .description("Manager avec des permissions limitées")
                .permissionIds(Set.of(1L, 2L))
                .build();
    }

    // ============================================================
    // TESTS DE CRÉATION
    // ============================================================

    @Test
    @DisplayName("Devrait créer un rôle avec succès")
    void shouldCreateRoleSuccessfully() {
        // GIVEN
        when(roleRepository.existsByName(roleRequestDTO.getName())).thenReturn(false);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findById(2L)).thenReturn(Optional.of(permissionWrite));

        Role roleToSave = new Role();
        roleToSave.setName(roleRequestDTO.getName());
        roleToSave.setDescription(roleRequestDTO.getDescription());

        Role savedRole = new Role();
        savedRole.setId(3L);
        savedRole.setName(roleRequestDTO.getName());
        savedRole.setDescription(roleRequestDTO.getDescription());
        Set<Permission> permissions = new HashSet<>();
        permissions.add(permissionRead);
        permissions.add(permissionWrite);
        savedRole.setPermissions(permissions);

        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        // WHEN
        RoleDTO result = roleService.creerRole(roleRequestDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("ROLE_MANAGER");
        assertThat(result.getPermissions()).hasSize(2);

        verify(roleRepository).existsByName(roleRequestDTO.getName());
        verify(permissionRepository).findById(1L);
        verify(permissionRepository).findById(2L);
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si le nom du rôle existe déjà")
    void shouldThrowExceptionWhenRoleNameAlreadyExists() {
        // GIVEN
        when(roleRepository.existsByName(roleRequestDTO.getName())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.creerRole(roleRequestDTO))
                .isInstanceOf(RoleDejaExistantException.class)
                .hasMessageContaining("existe déjà");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si une permission n'existe pas")
    void shouldThrowExceptionWhenPermissionNotFound() {
        // GIVEN
        when(roleRepository.existsByName(roleRequestDTO.getName())).thenReturn(false);
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(permissionRepository.findById(2L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.creerRole(roleRequestDTO))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessageContaining("non trouvée");

        verify(roleRepository, never()).save(any(Role.class));
    }

    // ============================================================
    // TESTS DE RÉCUPÉRATION
    // ============================================================

    @Test
    @DisplayName("Devrait récupérer un rôle par son ID")
    void shouldGetRoleById() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));

        // WHEN
        RoleDTO result = roleService.getRole(1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Devrait lever une exception si le rôle n'existe pas par ID")
    void shouldThrowExceptionWhenRoleNotFoundById() {
        // GIVEN
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.getRole(99L))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessageContaining("non trouvé");
    }

    @Test
    @DisplayName("Devrait récupérer un rôle par son nom")
    void shouldGetRoleByName() {
        // GIVEN
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));

        // WHEN
        RoleDTO result = roleService.getRoleByName("ROLE_ADMIN");

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_ADMIN");
    }

    // ============================================================
    // TESTS DE LISTE
    // ============================================================

    @Test
    @DisplayName("Devrait récupérer tous les rôles paginés")
    void shouldGetAllRolesPaginated() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Role> roles = List.of(roleAdmin, roleUser);
        Page<Role> page = new PageImpl<>(roles, pageable, roles.size());
        when(roleRepository.findAll(pageable)).thenReturn(page);

        // WHEN
        PageResponseDTO<RoleDTO> result = roleService.getAllRoles(pageable);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("Devrait récupérer tous les rôles sans pagination")
    void shouldGetAllRolesList() {
        // GIVEN
        when(roleRepository.findAllByOrderByNameAsc()).thenReturn(List.of(roleAdmin, roleUser));

        // WHEN
        List<RoleDTO> result = roleService.getAllRolesList();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ROLE_ADMIN");
    }

    // ============================================================
    // TESTS DE MISE À JOUR
    // ============================================================

    @Test
    @DisplayName("Devrait mettre à jour un rôle avec succès")
    void shouldUpdateRoleSuccessfully() {
        // GIVEN
        RoleRequestDTO updateDTO = RoleRequestDTO.builder()
                .name("ROLE_ADMIN_UPDATED")
                .description("Admin mis à jour")
                .permissionIds(Set.of(1L))
                .build();

        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.findByName(updateDTO.getName())).thenReturn(Optional.empty());
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(roleRepository.save(any(Role.class))).thenReturn(roleAdmin);

        // WHEN
        RoleDTO result = roleService.updateRole(1L, updateDTO);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("ROLE_ADMIN_UPDATED");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    @DisplayName("Devrait lever une exception si le rôle n'existe pas lors de la mise à jour")
    void shouldThrowExceptionWhenRoleNotFoundForUpdate() {
        // GIVEN
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.updateRole(99L, roleRequestDTO))
                .isInstanceOf(RoleNotFoundException.class);
    }

    // ============================================================
    // TESTS DE SUPPRESSION
    // ============================================================

    @Test
    @DisplayName("Devrait supprimer un rôle avec succès")
    void shouldDeleteRoleSuccessfully() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.countUsersWithRole("ROLE_ADMIN")).thenReturn(0L);

        // WHEN
        roleService.supprimerRole(1L);

        // THEN
        verify(roleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Devrait lever une exception si le rôle est utilisé par des utilisateurs")
    void shouldThrowExceptionWhenRoleIsUsed() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.countUsersWithRole("ROLE_ADMIN")).thenReturn(5L);

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.supprimerRole(1L))
                .isInstanceOf(RoleUtiliseException.class)
                .hasMessageContaining("utilisé par 5 utilisateur(s)");

        verify(roleRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // TESTS DE GESTION DES PERMISSIONS
    // ============================================================

    @Test
    @DisplayName("Devrait ajouter une permission à un rôle avec succès")
    void shouldAddPermissionToRoleSuccessfully() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(roleRepository.save(any(Role.class))).thenReturn(roleAdmin);

        // WHEN
        roleService.ajouterPermission(1L, 1L);

        // THEN
        assertThat(roleAdmin.getPermissions()).contains(permissionRead);
        verify(roleRepository).save(roleAdmin);
    }

    @Test
    @DisplayName("Devrait lever une exception si la permission est déjà dans le rôle")
    void shouldThrowExceptionWhenPermissionAlreadyInRole() {
        // GIVEN
        roleAdmin.getPermissions().add(permissionRead);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.ajouterPermission(1L, 1L))
                .isInstanceOf(PermissionDejaAssignéeException.class)
                .hasMessageContaining("déjà assignée");

        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    @DisplayName("Devrait retirer une permission d'un rôle avec succès")
    void shouldRemovePermissionFromRoleSuccessfully() {
        // GIVEN
        roleAdmin.getPermissions().add(permissionRead);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));
        when(roleRepository.save(any(Role.class))).thenReturn(roleAdmin);

        // WHEN
        roleService.retirerPermission(1L, 1L);

        // THEN
        assertThat(roleAdmin.getPermissions()).doesNotContain(permissionRead);
        verify(roleRepository).save(roleAdmin);
    }

    @Test
    @DisplayName("Devrait lever une exception si la permission n'est pas dans le rôle")
    void shouldThrowExceptionWhenPermissionNotInRole() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permissionRead));

        // WHEN & THEN
        assertThatThrownBy(() -> roleService.retirerPermission(1L, 1L))
                .isInstanceOf(PermissionNonAssignéeException.class)
                .hasMessageContaining("n'est pas assignée");

        verify(roleRepository, never()).save(any(Role.class));
    }

    // ============================================================
    // TESTS DE VALIDATION
    // ============================================================

    @Test
    @DisplayName("Devrait vérifier si un rôle existe par son nom")
    void shouldCheckIfRoleExistsByName() {
        // GIVEN
        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);
        when(roleRepository.existsByName("ROLE_INEXISTANT")).thenReturn(false);

        // WHEN & THEN
        assertThat(roleService.existeParNom("ROLE_ADMIN")).isTrue();
        assertThat(roleService.existeParNom("ROLE_INEXISTANT")).isFalse();
    }

    @Test
    @DisplayName("Devrait compter les utilisateurs d'un rôle")
    void shouldCountUsersByRole() {
        // GIVEN
        when(roleRepository.findById(1L)).thenReturn(Optional.of(roleAdmin));
        when(roleRepository.countUsersWithRole("ROLE_ADMIN")).thenReturn(10L);

        // WHEN
        long count = roleService.countUtilisateursByRole(1L);

        // THEN
        assertThat(count).isEqualTo(10L);
    }
}