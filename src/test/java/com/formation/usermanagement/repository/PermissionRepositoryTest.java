package com.formation.usermanagement.repository;

import com.formation.usermanagement.config.AuditConfig;
import com.formation.usermanagement.entity.Permission;
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
 * TEST DU REPOSITORY PERMISSION
 *
 * ✅ Teste toutes les méthodes de PermissionRepository
 * ✅ Vérifie les cas nominaux et les cas d'erreur
 * ✅ Vérifie les requêtes JPQL personnalisées
 */
@DataJpaTest
@Import(AuditConfig.class)
//
    @ActiveProfiles("test")

class PermissionRepositoryTest {

    @Autowired
    private PermissionRepository permissionRepository;

    private Permission permissionRead;
    private Permission permissionWrite;
    private Permission permissionDelete;
    private Permission permissionActivate;
    private Permission permissionExpire;
    private Permission permissionLock;
    private Permission permissionRoleAssign;
    private Permission permissionPermissionAssign;

    @BeforeEach
    void setUp() {
        // Nettoyer la BDD avant chaque test
        permissionRepository.deleteAll();

        // ============================================================
        // CRÉER LES PERMISSIONS (8 permissions)
        // ============================================================

        permissionRead = permissionRepository.save(
                new Permission("USER", "USER_READ", "Consulter les utilisateurs")
        );
        permissionWrite = permissionRepository.save(
                new Permission("USER", "USER_WRITE", "Créer et modifier des utilisateurs")
        );
        permissionDelete = permissionRepository.save(
                new Permission("USER", "USER_DELETE", "Supprimer des utilisateurs")
        );
        permissionActivate = permissionRepository.save(
                new Permission("USER", "USER_ACTIVATE", "Activer ou désactiver un compte")
        );
        permissionExpire = permissionRepository.save(
                new Permission("USER", "USER_EXPIRE", "Expirer ou renouveler un compte")
        );
        permissionLock = permissionRepository.save(
                new Permission("USER", "USER_LOCK", "Verrouiller ou déverrouiller un compte")
        );
        permissionRoleAssign = permissionRepository.save(
                new Permission("ROLE", "ROLE_ASSIGN", "Assigner ou retirer des rôles")
        );
        permissionPermissionAssign = permissionRepository.save(
                new Permission("PERMISSION", "PERMISSION_ASSIGN", "Ajouter ou retirer des permissions")
        );
    }

    // ============================================================
    // 1. TESTS DE RECHERCHE DE BASE
    // ============================================================

    // ------------------------------------------------------------
    // TEST 1 : findByName() - Permission existe
    // ------------------------------------------------------------

    @Test
    void findByName_DevraitRetournerLaPermission_QuandElleExiste() {
        // WHEN
        Optional<Permission> found = permissionRepository.findByName("USER_READ");

        // THEN
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("USER_READ");
        assertThat(found.get().getCategory()).isEqualTo("USER");
        assertThat(found.get().getDescription()).isEqualTo("Consulter les utilisateurs");
    }

    // ------------------------------------------------------------
    // TEST 2 : findByName() - Permission n'existe pas
    // ------------------------------------------------------------

    @Test
    void findByName_DevraitRetournerVide_QuandPermissionNExistePas() {
        // WHEN
        Optional<Permission> found = permissionRepository.findByName("PERMISSION_INEXISTANTE");

        // THEN
        assertThat(found).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 3 : existsByName() - Permission existe
    // ------------------------------------------------------------

    @Test
    void existsByName_DevraitRetournerTrue_QuandPermissionExiste() {
        // WHEN
        boolean exists = permissionRepository.existsByName("USER_WRITE");

        // THEN
        assertThat(exists).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 4 : existsByName() - Permission n'existe pas
    // ------------------------------------------------------------

    @Test
    void existsByName_DevraitRetournerFalse_QuandPermissionNExistePas() {
        // WHEN
        boolean exists = permissionRepository.existsByName("PERMISSION_INEXISTANTE");

        // THEN
        assertThat(exists).isFalse();
    }

    // ------------------------------------------------------------
    // TEST 5 : existsByCategoryAndName() - Combinaison existe
    // ------------------------------------------------------------

    @Test
    void existsByCategoryAndName_DevraitRetournerTrue_QuandCombinaisonExiste() {
        // WHEN
        boolean exists = permissionRepository.existsByCategoryAndName("USER", "USER_DELETE");

        // THEN
        assertThat(exists).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 6 : existsByCategoryAndName() - Combinaison n'existe pas
    // ------------------------------------------------------------

    @Test
    void existsByCategoryAndName_DevraitRetournerFalse_QuandCombinaisonNExistePas() {
        // WHEN
        boolean exists = permissionRepository.existsByCategoryAndName("USER", "PERMISSION_INEXISTANTE");

        // THEN
        assertThat(exists).isFalse();
    }

    // ============================================================
    // 2. TESTS DE RECHERCHE PAR CATÉGORIE
    // ============================================================

    // ------------------------------------------------------------
    // TEST 7 : findByCategory() - Toutes les permissions USER
    // ------------------------------------------------------------

    @Test
    void findByCategory_DevraitRetournerToutesLesPermissionsDeLaCategorie() {
        // WHEN
        List<Permission> permissions = permissionRepository.findByCategory("USER");

        // THEN
        assertThat(permissions).hasSize(6);  // 6 permissions USER
        assertThat(permissions)
                .extracting("name")
                .contains("USER_READ", "USER_WRITE", "USER_DELETE",
                        "USER_ACTIVATE", "USER_EXPIRE", "USER_LOCK");
    }

    // ------------------------------------------------------------
    // TEST 8 : findByCategory() - Catégorie vide
    // ------------------------------------------------------------

    @Test
    void findByCategory_DevraitRetournerVide_QuandCategorieNExistePas() {
        // WHEN
        List<Permission> permissions = permissionRepository.findByCategory("CATEGORIE_INEXISTANTE");

        // THEN
        assertThat(permissions).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 9 : findByCategoryOrderByNameAsc() - Permissions triées
    // ------------------------------------------------------------

    @Test
    void findByCategoryOrderByNameAsc_DevraitRetournerLesPermissionsTrieesParNom() {
        // WHEN
        List<Permission> permissions = permissionRepository.findByCategoryOrderByNameAsc("USER");

        // THEN
        assertThat(permissions).hasSize(6);
        // Vérifier l'ordre alphabétique
        assertThat(permissions.get(0).getName()).isEqualTo("USER_ACTIVATE");
        assertThat(permissions.get(1).getName()).isEqualTo("USER_DELETE");
        assertThat(permissions.get(2).getName()).isEqualTo("USER_EXPIRE");
        assertThat(permissions.get(3).getName()).isEqualTo("USER_LOCK");
        assertThat(permissions.get(4).getName()).isEqualTo("USER_READ");
        assertThat(permissions.get(5).getName()).isEqualTo("USER_WRITE");
    }

    // ------------------------------------------------------------
    // TEST 10 : findAllByOrderByCategoryAscNameAsc() - Toutes triées
    // ------------------------------------------------------------

    @Test
    void findAllByOrderByCategoryAscNameAsc_DevraitRetournerToutesLesPermissionsTriees() {
        // WHEN
        List<Permission> permissions = permissionRepository.findAllByOrderByCategoryAscNameAsc();

        // THEN
        assertThat(permissions).hasSize(8);
        // Vérifier l'ordre : d'abord par catégorie, puis par nom
        assertThat(permissions.get(0).getCategory()).isEqualTo("PERMISSION");
        assertThat(permissions.get(0).getName()).isEqualTo("PERMISSION_ASSIGN");
        assertThat(permissions.get(1).getCategory()).isEqualTo("ROLE");
        assertThat(permissions.get(1).getName()).isEqualTo("ROLE_ASSIGN");
        assertThat(permissions.get(2).getCategory()).isEqualTo("USER");
        assertThat(permissions.get(2).getName()).isEqualTo("USER_ACTIVATE");
    }

    // ============================================================
    // 3. TESTS DES REQUÊTES JPQL PERSONNALISÉES
    // ============================================================

    // ------------------------------------------------------------
    // TEST 11 : countPermissionsByCategory() - Statistiques
    // ------------------------------------------------------------

    @Test
    void countPermissionsByCategory_DevraitRetournerLeNombreDePermissionsParCategorie() {
        // WHEN
        List<Object[]> result = permissionRepository.countPermissionsByCategory();

        // THEN
        assertThat(result).hasSize(3);

        // Vérifier les résultats
        for (Object[] row : result) {
            String category = (String) row[0];
            Long count = (Long) row[1];

            if (category.equals("USER")) {
                assertThat(count).isEqualTo(6);
            } else if (category.equals("ROLE")) {
                assertThat(count).isEqualTo(1);
            } else if (category.equals("PERMISSION")) {
                assertThat(count).isEqualTo(1);
            }
        }
    }

    // ------------------------------------------------------------
    // TEST 12 : searchByKeyword() - Recherche dans category
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerLesPermissions_QuandKeywordCorrespondACategory() {
        // WHEN
        List<Permission> permissions = permissionRepository.searchByKeyword("USER");

        // THEN
        assertThat(permissions).hasSize(6);
        assertThat(permissions)
                .extracting("category")
                .containsOnly("USER");
    }

    // ------------------------------------------------------------
    // TEST 13 : searchByKeyword() - Recherche dans name
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerLesPermissions_QuandKeywordCorrespondAName() {
        // WHEN
        List<Permission> permissions = permissionRepository.searchByKeyword("READ");

        // THEN
        assertThat(permissions).hasSize(1);
        assertThat(permissions.get(0).getName()).isEqualTo("USER_READ");
    }

    // ------------------------------------------------------------
    // TEST 14 : searchByKeyword() - Aucun résultat
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerVide_QuandKeywordNeCorrespondARien() {
        // WHEN
        List<Permission> permissions = permissionRepository.searchByKeyword("KEYWORD_INEXISTANT");

        // THEN
        assertThat(permissions).isEmpty();
    }

    // ============================================================
    // 4. TESTS DE CRÉATION ET SUPPRESSION
    // ============================================================

    // ------------------------------------------------------------
    // TEST 15 : save() - Création d'une permission
    // ------------------------------------------------------------

    @Test
    void save_DevraitCreerUnePermission() {
        // GIVEN
        Permission newPermission = new Permission(
                "PRODUCT",
                "PRODUCT_READ",
                "Lire les produits"
        );

        // WHEN
        Permission saved = permissionRepository.save(newPermission);

        // THEN
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCategory()).isEqualTo("PRODUCT");
        assertThat(saved.getName()).isEqualTo("PRODUCT_READ");
        assertThat(saved.getDescription()).isEqualTo("Lire les produits");

        // Vérifier que la permission a été ajoutée
        Optional<Permission> found = permissionRepository.findByName("PRODUCT_READ");
        assertThat(found).isPresent();
    }

    // ------------------------------------------------------------
    // TEST 16 : delete() - Suppression d'une permission
    // ------------------------------------------------------------

    @Test
    void delete_DevraitSupprimerUnePermission() {
        // GIVEN
        Permission permission = permissionRepository.save(
                new Permission("TEST", "TEST_DELETE", "Permission de test")
        );
        Long id = permission.getId();

        // WHEN
        permissionRepository.deleteById(id);

        // THEN
        Optional<Permission> found = permissionRepository.findById(id);
        assertThat(found).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 17 : findAll() - Récupérer toutes les permissions
    // ------------------------------------------------------------

    @Test
    void findAll_DevraitRetournerToutesLesPermissions() {
        // WHEN
        List<Permission> permissions = permissionRepository.findAll();

        // THEN
        assertThat(permissions).hasSize(8);
    }

    // ------------------------------------------------------------
    // TEST 18 : findById() - Permission existe
    // ------------------------------------------------------------

    @Test
    void findById_DevraitRetournerLaPermission_QuandElleExiste() {
        // WHEN
        Optional<Permission> found = permissionRepository.findById(permissionRead.getId());

        // THEN
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("USER_READ");
    }

    // ------------------------------------------------------------
    // TEST 19 : findById() - Permission n'existe pas
    // ------------------------------------------------------------

    @Test
    void findById_DevraitRetournerVide_QuandPermissionNExistePas() {
        // WHEN
        Optional<Permission> found = permissionRepository.findById(999L);

        // THEN
        assertThat(found).isEmpty();
    }

    // ============================================================
    // 5. TEST DE CONTRAINTE D'UNICITÉ
    // ============================================================

    // ------------------------------------------------------------
    // TEST 20 : Contrainte d'unicité (category + name)
    // ------------------------------------------------------------

    @Test
    void save_DevraitLeverUneException_QuandCombinaisonCategoryNameExisteDeja() {
        // GIVEN
        Permission duplicate = new Permission(
                "USER",           // Même catégorie
                "USER_READ",      // Même nom
                "Duplicata"       // Description différente
        );

        // WHEN / THEN
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> permissionRepository.save(duplicate)
        );
    }
}