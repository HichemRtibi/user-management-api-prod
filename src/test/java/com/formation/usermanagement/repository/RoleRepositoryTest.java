package com.formation.usermanagement.repository;

import com.formation.usermanagement.config.AuditConfig;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ⚠️ TEST DES REPOSITORIES AVEC H2 EN MÉMOIRE
 *
 * ✅ @Import(AuditConfig.class) : Active l'audit pour les tests
 * ✅ Les dates created_at, updated_at sont remplies automatiquement
 * ✅ created_by et updated_by prennent "SYSTEM" par défaut
 */
@DataJpaTest
@Import(AuditConfig.class)
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Role roleAdmin;
    private Role roleManager;
    private Role roleUser;
    private Permission permissionRead;
    private Permission permissionWrite;
    private Permission permissionDelete;
    private Utilisateur utilisateur1;
    private Utilisateur utilisateur2;

    @BeforeEach
    void setUp() {
        // Nettoyer la BDD avant chaque test
        utilisateurRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // === CRÉER LES PERMISSIONS ===
        permissionRead = permissionRepository.save(
                new Permission("USER", "USER_READ", "Lire les utilisateurs")
        );
        permissionWrite = permissionRepository.save(
                new Permission("USER", "USER_WRITE", "Écrire les utilisateurs")
        );
        permissionDelete = permissionRepository.save(
                new Permission("USER", "USER_DELETE", "Supprimer les utilisateurs")
        );

        // === CRÉER LES RÔLES ===
        roleAdmin = new Role("ROLE_ADMIN", "Administrateur");
        roleAdmin.addPermission(permissionRead);
        roleAdmin.addPermission(permissionWrite);
        roleAdmin.addPermission(permissionDelete);
        roleAdmin = roleRepository.save(roleAdmin);

        roleManager = new Role("ROLE_MANAGER", "Manager");
        roleManager.addPermission(permissionRead);
        roleManager.addPermission(permissionWrite);
        roleManager = roleRepository.save(roleManager);

        roleUser = new Role("ROLE_USER", "Utilisateur standard");
        roleUser.addPermission(permissionRead);
        roleUser = roleRepository.save(roleUser);

        // === CRÉER DES UTILISATEURS ===
        utilisateur1 = new Utilisateur();
        utilisateur1.setPrenom("Jean");
        utilisateur1.setNom("Dupont");
        utilisateur1.setEmail("jean.dupont@email.com");
        utilisateur1.setMotDePasse("Password123!");
        utilisateur1.addRole(roleAdmin);
        utilisateur1 = utilisateurRepository.save(utilisateur1);

        utilisateur2 = new Utilisateur();
        utilisateur2.setPrenom("Marie");
        utilisateur2.setNom("Martin");
        utilisateur2.setEmail("marie.martin@email.com");
        utilisateur2.setMotDePasse("Password123!");
        utilisateur2.addRole(roleUser);
        utilisateur2 = utilisateurRepository.save(utilisateur2);
    }

    // ============================================================
    // TESTS
    // ============================================================

    @Test
    void findByName_DevraitRetournerLeRole_QuandIlExiste() {
        Optional<Role> found = roleRepository.findByName("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_ADMIN");
        assertThat(found.get().getPermissions()).hasSize(3);
        assertThat(found.get().getPermissions())
                .extracting("name")
                .contains("USER_READ", "USER_WRITE", "USER_DELETE");
    }

    @Test
    void findByName_DevraitRetournerVide_QuandRoleNExistePas() {
        Optional<Role> found = roleRepository.findByName("ROLE_SUPER_ADMIN");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByName_DevraitRetournerTrue_QuandRoleExiste() {
        boolean exists = roleRepository.existsByName("ROLE_MANAGER");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_DevraitRetournerFalse_QuandRoleNExistePas() {
        boolean exists = roleRepository.existsByName("ROLE_INEXISTANT");
        assertThat(exists).isFalse();
    }

    @Test
    void findAllByOrderByNameAsc_DevraitRetournerLesRolesTries() {
        List<Role> roles = roleRepository.findAllByOrderByNameAsc();

        assertThat(roles).hasSize(3);
        assertThat(roles.get(0).getName()).isEqualTo("ROLE_ADMIN");
        assertThat(roles.get(1).getName()).isEqualTo("ROLE_MANAGER");
        assertThat(roles.get(2).getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void findByNameWithPermissions_DevraitChargerLesPermissions() {
        Optional<Role> found = roleRepository.findByNameWithPermissions("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertThat(found.get().getPermissions()).hasSize(3);
    }

    @Test
    void findByNameWithUtilisateurs_DevraitChargerLesUtilisateurs() {
        Optional<Role> found = roleRepository.findByNameWithUtilisateurs("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertThat(found.get().getUtilisateurs()).hasSize(1);
        assertThat(found.get().getUtilisateurs().iterator().next().getEmail())
                .isEqualTo("jean.dupont@email.com");
    }

    @Test
    void findAllWithUtilisateurs_DevraitRetournerTousLesRolesAvecUtilisateurs() {
        List<Role> roles = roleRepository.findAllWithUtilisateurs();

        assertThat(roles).hasSize(3);

        Role admin = roles.stream()
                .filter(r -> r.getName().equals("ROLE_ADMIN"))
                .findFirst()
                .orElse(null);
        assertThat(admin).isNotNull();
        assertThat(admin.getUtilisateurs()).hasSize(1);

        Role user = roles.stream()
                .filter(r -> r.getName().equals("ROLE_USER"))
                .findFirst()
                .orElse(null);
        assertThat(user).isNotNull();
        assertThat(user.getUtilisateurs()).hasSize(1);
    }

    @Test
    void countUsersWithRole_DevraitRetournerLeNombreDUtilisateurs() {
        long count = roleRepository.countUsersWithRole("ROLE_ADMIN");
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countUsersWithRole_DevraitRetournerZero_QuandAucunUtilisateur() {
        long count = roleRepository.countUsersWithRole("ROLE_MANAGER");
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findRolesByPermissionName_DevraitRetournerLesRolesAyantCettePermission() {
        List<Role> roles = roleRepository.findRolesByPermissionName("USER_WRITE");

        assertThat(roles).hasSize(2);
        assertThat(roles)
                .extracting("name")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MANAGER");
    }

    @Test
    void findRolesByPermissionName_DevraitRetournerVide_QuandAucunRoleAAcettePermission() {
        List<Role> roles = roleRepository.findRolesByPermissionName("PERMISSION_INEXISTANTE");
        assertThat(roles).isEmpty();
    }

    @Test
    void findRolesByPermissionCategory_DevraitRetournerLesRolesAvecPermissionsDeCetteCategorie() {
        List<Role> roles = roleRepository.findRolesByPermissionCategory("USER");
        assertThat(roles).hasSize(3);
    }

    @Test
    void findRolesWithoutUsers_DevraitRetournerLesRolesSansUtilisateurs() {
        List<Role> roles = roleRepository.findRolesWithoutUsers();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getName()).isEqualTo("ROLE_MANAGER");
    }

//    @Test
//    void findMostPopularRoles_DevraitRetournerLesRolesTriesParNombreDUtilisateurs() {
//        List<Object[]> result = roleRepository.findMostPopularRoles();
//
//        assertThat(result).hasSize(3);
//        assertThat(result.get(0)[0]).isEqualTo("ROLE_ADMIN");
//        assertThat((Long) result.get(0)[1]).isEqualTo(1);
//        assertThat(result.get(1)[0]).isEqualTo("ROLE_USER");
//        assertThat((Long) result.get(1)[1]).isEqualTo(1);
//        assertThat(result.get(2)[0]).isEqualTo("ROLE_MANAGER");
//        assertThat((Long) result.get(2)[1]).isEqualTo(0);
//    }

    @Test
    void findByNameWithAllRelations_DevraitChargerToutesLesRelations() {
        Optional<Role> found = roleRepository.findByNameWithAllRelations("ROLE_ADMIN");

        assertThat(found).isPresent();
        assertThat(found.get().getPermissions()).hasSize(3);
        assertThat(found.get().getUtilisateurs()).hasSize(1);
        assertThat(found.get().getUtilisateurs().iterator().next().getEmail())
                .isEqualTo("jean.dupont@email.com");
    }
}