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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TEST DU REPOSITORY UTILISATEUR
 *
 * ✅ Teste toutes les méthodes de UtilisateurRepository
 * ✅ Vérifie les cas nominaux et les cas d'erreur
 * ✅ Vérifie les requêtes JPQL personnalisées
 * ✅ Vérifie les méthodes de gestion d'état (@Modifying)
 */
@DataJpaTest
@Import(AuditConfig.class)
@ActiveProfiles("test")
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Utilisateur utilisateur1;
    private Utilisateur utilisateur2;
    private Role roleAdmin;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        // Nettoyer la BDD avant chaque test
        utilisateurRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // ============================================================
        // 1. CRÉER LES PERMISSIONS
        // ============================================================
        Permission permissionRead = permissionRepository.save(
                new Permission("USER", "USER_READ", "Lire les utilisateurs")
        );
        Permission permissionWrite = permissionRepository.save(
                new Permission("USER", "USER_WRITE", "Écrire les utilisateurs")
        );
        Permission permissionDelete = permissionRepository.save(
                new Permission("USER", "USER_DELETE", "Supprimer les utilisateurs")
        );

        // ============================================================
        // 2. CRÉER LES RÔLES
        // ============================================================
        roleAdmin = new Role("ROLE_ADMIN", "Administrateur");
        roleAdmin.addPermission(permissionRead);
        roleAdmin.addPermission(permissionWrite);
        roleAdmin.addPermission(permissionDelete);
        roleAdmin = roleRepository.save(roleAdmin);

        roleUser = new Role("ROLE_USER", "Utilisateur standard");
        roleUser.addPermission(permissionRead);
        roleUser = roleRepository.save(roleUser);

        // ============================================================
        // 3. CRÉER LES UTILISATEURS
        // ============================================================

        // Utilisateur 1 : Jean Dupont (Admin)
        utilisateur1 = new Utilisateur();
        utilisateur1.setPrenom("Jean");
        utilisateur1.setNom("Dupont");
        utilisateur1.setEmail("jean.dupont@email.com");
        utilisateur1.setMotDePasse("Password123!");
        utilisateur1.addRole(roleAdmin);
        utilisateur1 = utilisateurRepository.save(utilisateur1);

        // Utilisateur 2 : Marie Martin (User)
        utilisateur2 = new Utilisateur();
        utilisateur2.setPrenom("Marie");
        utilisateur2.setNom("Martin");
        utilisateur2.setEmail("marie.martin@email.com");
        utilisateur2.setMotDePasse("Password123!");
        utilisateur2.addRole(roleUser);
        utilisateur2 = utilisateurRepository.save(utilisateur2);
    }

    // ============================================================
    // 1. TESTS DE RECHERCHE DE BASE
    // ============================================================

    // ------------------------------------------------------------
    // TEST 1 : findByEmail() - Utilisateur existe
    // ------------------------------------------------------------

    @Test
    void findByEmail_DevraitRetournerLUtilisateur_QuandEmailExiste() {
        // WHEN
        Optional<Utilisateur> found = utilisateurRepository.findByEmail("jean.dupont@email.com");

        // THEN
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(found.get().getPrenom()).isEqualTo("Jean");
        assertThat(found.get().getNom()).isEqualTo("Dupont");
        assertThat(found.get().getRoles()).hasSize(1);
        assertThat(found.get().hasRole("ROLE_ADMIN")).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 2 : findByEmail() - Email n'existe pas
    // ------------------------------------------------------------

    @Test
    void findByEmail_DevraitRetournerVide_QuandEmailNExistePas() {
        // WHEN
        Optional<Utilisateur> found = utilisateurRepository.findByEmail("inexistant@email.com");

        // THEN
        assertThat(found).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 3 : existsByEmail() - Email existe
    // ------------------------------------------------------------

    @Test
    void existsByEmail_DevraitRetournerTrue_QuandEmailExiste() {
        // WHEN
        boolean exists = utilisateurRepository.existsByEmail("marie.martin@email.com");

        // THEN
        assertThat(exists).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 4 : existsByEmail() - Email n'existe pas
    // ------------------------------------------------------------

    @Test
    void existsByEmail_DevraitRetournerFalse_QuandEmailNExistePas() {
        // WHEN
        boolean exists = utilisateurRepository.existsByEmail("inexistant@email.com");

        // THEN
        assertThat(exists).isFalse();
    }

    // ------------------------------------------------------------
    // TEST 5 : findByEnabledTrue() - Utilisateurs actifs
    // ------------------------------------------------------------

    @Test
    void findByEnabledTrue_DevraitRetournerLesUtilisateursActifs() {
        // WHEN
        List<Utilisateur> actifs = utilisateurRepository.findByEnabledTrue();

        // THEN
        assertThat(actifs).hasSize(2);
        assertThat(actifs)
                .extracting("email")
                .contains("jean.dupont@email.com", "marie.martin@email.com");
    }

    // ------------------------------------------------------------
    // TEST 6 : findByEnabledFalse() - Utilisateurs désactivés
    // ------------------------------------------------------------

    @Test
    void findByEnabledFalse_DevraitRetournerLesUtilisateursDesactives() {
        // GIVEN - Désactiver un utilisateur
        utilisateurRepository.desactiverUtilisateur(utilisateur1.getId());

        // WHEN
        List<Utilisateur> desactives = utilisateurRepository.findByEnabledFalse();

        // THEN
        assertThat(desactives).hasSize(1);
        assertThat(desactives.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 7 : findByCompteNonVerrouilleFalse() - Utilisateurs verrouillés
    // ------------------------------------------------------------

    @Test
    void findByCompteNonVerrouilleFalse_DevraitRetournerLesUtilisateursVerrouilles() {
        // GIVEN - Verrouiller un utilisateur
        utilisateurRepository.verrouillerUtilisateur(utilisateur2.getId());

        // WHEN
        List<Utilisateur> verrouilles = utilisateurRepository.findByCompteNonVerrouilleFalse();

        // THEN
        assertThat(verrouilles).hasSize(1);
        assertThat(verrouilles.get(0).getEmail()).isEqualTo("marie.martin@email.com");
    }

    // ------------------------------------------------------------
    // TEST 8 : findByCompteNonExpireFalse() - Utilisateurs expirés
    // ------------------------------------------------------------

    @Test
    void findByCompteNonExpireFalse_DevraitRetournerLesUtilisateursExpires() {
        // GIVEN - Expirer un utilisateur
        utilisateurRepository.expirerUtilisateur(utilisateur1.getId());

        // WHEN
        List<Utilisateur> expires = utilisateurRepository.findByCompteNonExpireFalse();

        // THEN
        assertThat(expires).hasSize(1);
        assertThat(expires.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 9 : findByCredentialsNonExpireFalse() - Mots de passe expirés
    // ------------------------------------------------------------

    @Test
    void findByCredentialsNonExpireFalse_DevraitRetournerLesUtilisateursAvecMotDePasseExpire() {
        // GIVEN - Expirer le mot de passe d'un utilisateur
        utilisateurRepository.expirerUtilisateur(utilisateur1.getId());

        // WHEN
        List<Utilisateur> expires = utilisateurRepository.findByCredentialsNonExpireFalse();

        // THEN
        assertThat(expires).hasSize(1);
        assertThat(expires.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ============================================================
    // 2. TESTS DE RECHERCHE AVANCÉE (JPQL)
    // ============================================================

    // ------------------------------------------------------------
    // TEST 10 : searchByKeyword() - Recherche par prénom
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerLesUtilisateurs_QuandKeywordCorrespondAuPrenom() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchByKeyword("Jean");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 11 : searchByKeyword() - Recherche par nom
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerLesUtilisateurs_QuandKeywordCorrespondAuNom() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchByKeyword("Martin");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("marie.martin@email.com");
    }

    // ------------------------------------------------------------
    // TEST 12 : searchByKeyword() - Recherche partielle
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerLesUtilisateurs_QuandKeywordEstPartiel() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchByKeyword("Du");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 13 : searchByKeyword() - Aucun résultat
    // ------------------------------------------------------------

    @Test
    void searchByKeyword_DevraitRetournerVide_QuandKeywordNeCorrespondARien() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchByKeyword("Inexistant");

        // THEN
        assertThat(result).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 14 : searchByEmailKeyword() - Recherche par email
    // ------------------------------------------------------------

    @Test
    void searchByEmailKeyword_DevraitRetournerLesUtilisateurs_QuandEmailCorrespond() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchByEmailKeyword("dupont");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 15 : searchWithFilters() - Filtres multiples
    // ------------------------------------------------------------

    @Test
    void searchWithFilters_DevraitRetournerLesUtilisateurs_QuandFiltresCorrespondent() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchWithFilters("Jean", "Dupont", true);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 16 : searchWithFilters() - Filtre null
    // ------------------------------------------------------------

    @Test
    void searchWithFilters_DevraitRetournerTousLesUtilisateurs_QuandFiltresSontNull() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.searchWithFilters(null, null, null);

        // THEN
        assertThat(result).hasSize(2);
    }

    // ============================================================
    // 3. TESTS DE GESTION DES ÉTATS (@Modifying)
    // ============================================================

    // ------------------------------------------------------------
    // TEST 17 : desactiverUtilisateur() - Désactiver un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void desactiverUtilisateur_DevraitDesactiverLUtilisateur() {
        // GIVEN
        assertThat(utilisateur1.isEnabled()).isTrue();

        // WHEN
        int updated = utilisateurRepository.desactiverUtilisateur(utilisateur1.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isFalse();
    }

    // ------------------------------------------------------------
    // TEST 18 : activerUtilisateur() - Activer un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void activerUtilisateur_DevraitActiverLUtilisateur() {
        // GIVEN - Désactiver d'abord
        utilisateurRepository.desactiverUtilisateur(utilisateur1.getId());
        assertThat(utilisateurRepository.findById(utilisateur1.getId()).get().isEnabled()).isFalse();

        // WHEN
        int updated = utilisateurRepository.activerUtilisateur(utilisateur1.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 19 : verrouillerUtilisateur() - Verrouiller un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void verrouillerUtilisateur_DevraitVerrouillerLUtilisateur() {
        // GIVEN
        assertThat(utilisateur2.isCompteNonVerrouille()).isTrue();

        // WHEN
        int updated = utilisateurRepository.verrouillerUtilisateur(utilisateur2.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur2.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isCompteNonVerrouille()).isFalse();
    }

    // ------------------------------------------------------------
    // TEST 20 : deverrouillerUtilisateur() - Déverrouiller un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void deverrouillerUtilisateur_DevraitDeverrouillerLUtilisateur() {
        // GIVEN - Verrouiller d'abord
        utilisateurRepository.verrouillerUtilisateur(utilisateur2.getId());
        assertThat(utilisateurRepository.findById(utilisateur2.getId()).get().isCompteNonVerrouille()).isFalse();

        // WHEN
        int updated = utilisateurRepository.deverrouillerUtilisateur(utilisateur2.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur2.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isCompteNonVerrouille()).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 21 : expirerUtilisateur() - Expirer un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void expirerUtilisateur_DevraitExpirerLUtilisateur() {
        // GIVEN
        assertThat(utilisateur1.isCompteNonExpire()).isTrue();

        // WHEN
        int updated = utilisateurRepository.expirerUtilisateur(utilisateur1.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isCompteNonExpire()).isFalse();
    }

    // ------------------------------------------------------------
    // TEST 22 : renouvelerUtilisateur() - Renouveler un utilisateur
    // ------------------------------------------------------------

    @Test
    @Transactional
    void renouvelerUtilisateur_DevraitRenouvelerLUtilisateur() {
        // GIVEN - Expirer d'abord
        utilisateurRepository.expirerUtilisateur(utilisateur1.getId());
        assertThat(utilisateurRepository.findById(utilisateur1.getId()).get().isCompteNonExpire()).isFalse();

        // WHEN
        int updated = utilisateurRepository.renouvelerUtilisateur(utilisateur1.getId());

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isCompteNonExpire()).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 23 : updateDerniereConnexion() - Mettre à jour la date de connexion
    // ------------------------------------------------------------

    @Test
    @Transactional
    void updateDerniereConnexion_DevraitMettreAJourLaDateDeConnexion() {
        // GIVEN
        LocalDateTime now = LocalDateTime.now();
        assertThat(utilisateur1.getDerniereConnexion()).isNull();

        // WHEN
        int updated = utilisateurRepository.updateDerniereConnexion(utilisateur1.getId(), now);

        // THEN
        assertThat(updated).isEqualTo(1);

        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDerniereConnexion()).isNotNull();
    }

    // ============================================================
    // 4. TESTS STATISTIQUES
    // ============================================================

    // ------------------------------------------------------------
    // TEST 24 : countUtilisateursActifs()
    // ------------------------------------------------------------

    @Test
    void countUtilisateursActifs_DevraitRetournerLeNombreDUtilisateursActifs() {
        // WHEN
        long count = utilisateurRepository.countUtilisateursActifs();

        // THEN
        assertThat(count).isEqualTo(2);
    }

    // ------------------------------------------------------------
    // TEST 25 : countUtilisateursDesactives()
    // ------------------------------------------------------------

    @Test
    void countUtilisateursDesactives_DevraitRetournerLeNombreDUtilisateursDesactives() {
        // GIVEN
        utilisateurRepository.desactiverUtilisateur(utilisateur1.getId());

        // WHEN
        long count = utilisateurRepository.countUtilisateursDesactives();

        // THEN
        assertThat(count).isEqualTo(1);
    }

    // ------------------------------------------------------------
    // TEST 26 : countUtilisateursVerrouilles()
    // ------------------------------------------------------------

    @Test
    void countUtilisateursVerrouilles_DevraitRetournerLeNombreDUtilisateursVerrouilles() {
        // GIVEN
        utilisateurRepository.verrouillerUtilisateur(utilisateur1.getId());

        // WHEN
        long count = utilisateurRepository.countUtilisateursVerrouilles();

        // THEN
        assertThat(count).isEqualTo(1);
    }

    // ------------------------------------------------------------
    // TEST 27 : countUtilisateursExpires()
    // ------------------------------------------------------------

    @Test
    void countUtilisateursExpires_DevraitRetournerLeNombreDUtilisateursExpires() {
        // GIVEN
        utilisateurRepository.expirerUtilisateur(utilisateur1.getId());

        // WHEN
        long count = utilisateurRepository.countUtilisateursExpires();

        // THEN
        assertThat(count).isEqualTo(1);
    }

    // ------------------------------------------------------------
    // TEST 28 : findUtilisateursJamaisConnectes()
    // ------------------------------------------------------------

    @Test
    void findUtilisateursJamaisConnectes_DevraitRetournerLesUtilisateursJamaisConnectes() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.findUtilisateursJamaisConnectes();

        // THEN
        assertThat(result).hasSize(2);  // Les deux utilisateurs ne se sont jamais connectés
    }

    // ------------------------------------------------------------
    // TEST 29 : findUtilisateursInactifs()
    // ------------------------------------------------------------

    @Test
    void findUtilisateursInactifs_DevraitRetournerLesUtilisateursInactifs() {
        // GIVEN - Mettre à jour la date de connexion de Jean
        LocalDateTime oldDate = LocalDateTime.now().minusDays(100);
        utilisateurRepository.updateDerniereConnexion(utilisateur1.getId(), oldDate);

        // Marie n'a jamais été connectée

        // WHEN - Rechercher les utilisateurs inactifs depuis 30 jours
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Utilisateur> result = utilisateurRepository.findUtilisateursInactifs(threshold);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 30 : findUtilisateursByRole()
    // ------------------------------------------------------------

    @Test
    void findUtilisateursByRole_DevraitRetournerLesUtilisateursAvecCeRole() {
        // WHEN
        List<Utilisateur> result = utilisateurRepository.findUtilisateursByRole("ROLE_ADMIN");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 31 : countUtilisateursByRole()
    // ------------------------------------------------------------

    @Test
    void countUtilisateursByRole_DevraitRetournerLeNombreDUtilisateursParRole() {
        // WHEN
        List<Object[]> result = utilisateurRepository.countUtilisateursByRole();

        // THEN
        assertThat(result).hasSize(2);

        for (Object[] row : result) {
            String roleName = (String) row[0];
            Long count = (Long) row[1];

            if (roleName.equals("ROLE_ADMIN")) {
                assertThat(count).isEqualTo(1);
            } else if (roleName.equals("ROLE_USER")) {
                assertThat(count).isEqualTo(1);
            }
        }
    }

    // ------------------------------------------------------------
    // TEST 32 : findDerniersConnectes()
    // ------------------------------------------------------------

    @Test
    void findDerniersConnectes_DevraitRetournerLesUtilisateursTriesParDateDeConnexion() {
        // GIVEN
        LocalDateTime now = LocalDateTime.now();
        utilisateurRepository.updateDerniereConnexion(utilisateur1.getId(), now);
        utilisateurRepository.updateDerniereConnexion(utilisateur2.getId(), now.minusDays(1));

        // WHEN
        List<Utilisateur> result = utilisateurRepository.findDerniersConnectes();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("jean.dupont@email.com");  // Le plus récent
        assertThat(result.get(1).getEmail()).isEqualTo("marie.martin@email.com");
    }

    // ============================================================
    // 5. TESTS DE CRÉATION ET CONTRAINTES
    // ============================================================

    // ------------------------------------------------------------
    // TEST 33 : save() - Création d'un utilisateur
    // ------------------------------------------------------------

    @Test
    void save_DevraitCreerUnUtilisateur() {
        // GIVEN
        Utilisateur newUser = new Utilisateur();
        newUser.setPrenom("Pierre");
        newUser.setNom("Durand");
        newUser.setEmail("pierre.durand@email.com");
        newUser.setMotDePasse("Password123!");
        newUser.addRole(roleUser);

        // WHEN
        Utilisateur saved = utilisateurRepository.save(newUser);

        // THEN
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("pierre.durand@email.com");
        assertThat(saved.getRoles()).hasSize(1);
        assertThat(saved.hasRole("ROLE_USER")).isTrue();
        assertThat(saved.isEnabled()).isTrue();  // Vérifier la valeur par défaut
        assertThat(saved.isCompteNonVerrouille()).isTrue();
        assertThat(saved.isCompteNonExpire()).isTrue();
        assertThat(saved.isCredentialsNonExpire()).isTrue();
    }

    // ------------------------------------------------------------
    // TEST 34 : Contrainte d'unicité sur l'email
    // ------------------------------------------------------------

    @Test
    void save_DevraitLeverUneException_QuandEmailExisteDeja() {
        // GIVEN
        Utilisateur duplicate = new Utilisateur();
        duplicate.setPrenom("Jean");
        duplicate.setNom("Dupont");
        duplicate.setEmail("jean.dupont@email.com");  // Email déjà existant
        duplicate.setMotDePasse("Password123!");

        // WHEN / THEN
        assertThatThrownBy(() -> utilisateurRepository.save(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ------------------------------------------------------------
    // TEST 35 : delete() - Suppression d'un utilisateur
    // ------------------------------------------------------------

    @Test
    void delete_DevraitSupprimerLUtilisateur() {
        // GIVEN
        Long id = utilisateur1.getId();

        // WHEN
        utilisateurRepository.deleteById(id);

        // THEN
        Optional<Utilisateur> found = utilisateurRepository.findById(id);
        assertThat(found).isEmpty();
    }

    // ------------------------------------------------------------
    // TEST 36 : findAll() - Récupérer tous les utilisateurs
    // ------------------------------------------------------------

    @Test
    void findAll_DevraitRetournerTousLesUtilisateurs() {
        // WHEN
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        // THEN
        assertThat(utilisateurs).hasSize(2);
    }

    // ------------------------------------------------------------
    // TEST 37 : findById() - Utilisateur existe
    // ------------------------------------------------------------

    @Test
    void findById_DevraitRetournerLUtilisateur_QuandIlExiste() {
        // WHEN
        Optional<Utilisateur> found = utilisateurRepository.findById(utilisateur1.getId());

        // THEN
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jean.dupont@email.com");
    }

    // ------------------------------------------------------------
    // TEST 38 : findById() - Utilisateur n'existe pas
    // ------------------------------------------------------------

    @Test
    void findById_DevraitRetournerVide_QuandUtilisateurNExistePas() {
        // WHEN
        Optional<Utilisateur> found = utilisateurRepository.findById(999L);

        // THEN
        assertThat(found).isEmpty();
    }
}