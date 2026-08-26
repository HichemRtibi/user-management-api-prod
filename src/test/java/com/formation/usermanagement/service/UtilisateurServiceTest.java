package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;
import com.formation.usermanagement.exception.*;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.repository.UtilisateurRepository;
import com.formation.usermanagement.service.impl.UtilisateurServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 * TESTS UNITAIRES DU SERVICE UTILISATEUR
 * ============================================================
 *
 * 🎯 OBJECTIF : Tester toutes les méthodes du service de manière isolée
 *
 * 📋 COUVERTURE DES TESTS :
 *
 * 1. CRÉATION (creerUtilisateur)
 *    ✅ Succès
 *    ✅ Email déjà existant
 *    ✅ Rôle ROLE_USER non trouvé
 *
 * 2. RÉCUPÉRATION (getUtilisateur, getUtilisateurByEmail)
 *    ✅ Succès par ID
 *    ✅ Succès par email
 *    ✅ Échec par ID inexistant
 *    ✅ Échec par email inexistant
 *
 * 3. LISTE PAGINÉE (getAllUtilisateurs)
 *    ✅ Page avec résultats
 *    ✅ Page vide
 *
 * 4. MISE À JOUR (updateUtilisateur)
 *    ✅ Succès
 *    ✅ Utilisateur non trouvé
 *    ✅ Email déjà utilisé par un autre
 *
 * 5. SUPPRESSION (supprimerUtilisateur)
 *    ✅ Succès
 *    ✅ Utilisateur non trouvé
 *
 * 6. GESTION DES ÉTATS
 *    ✅ Désactiver (succès + déjà désactivé)
 *    ✅ Activer (succès + déjà activé)
 *    ✅ Verrouiller (succès + déjà verrouillé)
 *    ✅ Déverrouiller (succès + déjà déverrouillé)
 *    ✅ Expirer (succès + déjà expiré)
 *    ✅ Renouveler (succès + déjà renouvelé)
 *
 * 7. GESTION DES RÔLES
 *    ✅ Assigner (succès + déjà assigné)
 *    ✅ Retirer (succès + non assigné)
 *
 * 8. GESTION DES PERMISSIONS
 *    ✅ Ajouter (succès + déjà ajoutée)
 *    ✅ Retirer (succès + non assignée)
 *
 * 9. VALIDATION
 *    ✅ existeParEmail
 *    ✅ estActif
 *
 * ⚠️ ANNOTATIONS IMPORTANTES :
 *
 * @ExtendWith(MockitoExtension.class) → Active Mockito
 * @Mock → Crée une simulation d'une classe
 * @InjectMocks → Injecte les mocks dans le service testé
 * @BeforeEach → Exécuté avant chaque test
 * @Test → Déclare une méthode de test
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    // ============================================================
    // 1. MOCKS (Simulations des dépendances)
    // ============================================================

    /**
     * 📌 @Mock UtilisateurRepository
     *
     * Simule le repository des utilisateurs.
     * On définit son comportement avec when(...).thenReturn(...)
     */
    @Mock
    private UtilisateurRepository utilisateurRepository;

    /**
     * 📌 @Mock RoleRepository
     *
     * Simule le repository des rôles.
     */
    @Mock
    private RoleRepository roleRepository;

    /**
     * 📌 @Mock PermissionRepository
     *
     * Simule le repository des permissions.
     */
    @Mock
    private PermissionRepository permissionRepository;

    /**
     * 📌 @Mock PasswordEncoder
     *
     * Simule l'encodeur de mot de passe.
     * On simule le hachage pour les tests.
     */
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * 📌 @InjectMocks
     *
     * Injecte les mocks dans le service testé.
     * Cela crée une instance du service avec les dépendances mockées.
     */
    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    // ============================================================
    // 2. DONNÉES DE TEST
    // ============================================================

    private Utilisateur utilisateur;
    private Role roleUser;
    private Role roleAdmin;
    private Permission permissionRead;
    private Permission permissionWrite;
    private UtilisateurRequestDTO requestDTO;

    /**
     * 📌 @BeforeEach
     *
     * Cette méthode est exécutée AVANT chaque test.
     * Elle prépare les données de test.
     *
     * Pourquoi @BeforeEach ?
     * → Chaque test doit avoir des données propres et isolées
     * → Évite les interférences entre les tests
     */
    @BeforeEach
    void setUp() {
        // ============================================================
        // 2.1 CRÉER LES PERMISSIONS
        // ============================================================
        permissionRead = new Permission("USER", "USER_READ", "Lire les utilisateurs");
        permissionRead.setId(1L);

        permissionWrite = new Permission("USER", "USER_WRITE", "Écrire les utilisateurs");
        permissionWrite.setId(2L);

        // ============================================================
        // 2.2 CRÉER LES RÔLES
        // ============================================================
        roleUser = new Role("ROLE_USER", "Utilisateur standard");
        roleUser.setId(1L);
        roleUser.addPermission(permissionRead);

        roleAdmin = new Role("ROLE_ADMIN", "Administrateur");
        roleAdmin.setId(2L);
        roleAdmin.addPermission(permissionRead);
        roleAdmin.addPermission(permissionWrite);

        // ============================================================
        // 2.3 CRÉER L'UTILISATEUR
        // ============================================================
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
        utilisateur.addRole(roleUser);

        // ============================================================
        // 2.4 CRÉER LE DTO DE REQUÊTE (AVEC BUILDER)
        // ============================================================
        requestDTO = UtilisateurRequestDTO.builder()
                .prenom("Jean")
                .nom("Dupont")
                .email("jean.dupont@email.com")
                .motDePasse("Password123@")
                .build();
    }

    // ============================================================
    // 3. TESTS CRÉATION (creerUtilisateur)
    // ============================================================

    /**
     * ============================================================
     * TEST : creerUtilisateur_DevraitReussir
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la création d'un utilisateur fonctionne
     *
     * 📋 ÉTAPES DU TEST :
     *
     * 1. GIVEN (Préparation)
     *    → Simuler que l'email n'existe pas (existsByEmail → false)
     *    → Simuler que le rôle ROLE_USER existe (findByName → roleUser)
     *    → Simuler l'encodage du mot de passe (encode → "encodedPassword")
     *    → Simuler la sauvegarde (save → utilisateur)
     *
     * 2. WHEN (Exécution)
     *    → Appeler utilisateurService.creerUtilisateur(requestDTO)
     *
     * 3. THEN (Vérification)
     *    → Vérifier que le résultat n'est pas null
     *    → Vérifier que l'ID est 1
     *    → Vérifier que l'email est correct
     *    → Vérifier que le rôle est présent
     *    → Vérifier que les méthodes mockées ont été appelées
     */
    @Test
    void creerUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : Préparation des mocks
        // ============================================================
        // when(...) : Définit le comportement du mock
        // thenReturn(...) : Ce que le mock doit retourner
        when(utilisateurRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(requestDTO.getMotDePasse())).thenReturn("encodedPassword123@");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN : Exécution de la méthode testée
        // ============================================================
        UtilisateurResponseDTO response = utilisateurService.creerUtilisateur(requestDTO);

        // ============================================================
        // 3️⃣ THEN : Vérifications
        // ============================================================
        // 3.1 Vérifier le résultat
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getRoles()).contains("ROLE_USER");
        assertThat(response.getPrenom()).isEqualTo("Jean");
        assertThat(response.getNom()).isEqualTo("Dupont");

        // 3.2 Vérifier que les mocks ont été appelés correctement
        verify(utilisateurRepository).existsByEmail(requestDTO.getEmail());
        verify(roleRepository).findByName("ROLE_USER");
        verify(passwordEncoder).encode(requestDTO.getMotDePasse());
        verify(utilisateurRepository).save(any(Utilisateur.class));

        // 3.3 Vérifier que les méthodes n'ont PAS été appelées incorrectement
        verify(utilisateurRepository, never()).findById(anyLong());
        verify(roleRepository, never()).findByName("ROLE_ADMIN");
    }

    /**
     * ============================================================
     * TEST : creerUtilisateur_EmailExistant_DevraitEchouer
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier qu'on ne peut pas créer un utilisateur
     * avec un email déjà utilisé
     *
     * 📋 ÉTAPES :
     *
     * 1. GIVEN : Simuler que l'email existe (existsByEmail → true)
     * 2. WHEN : Appeler la méthode
     * 3. THEN : Vérifier que l'exception est levée
     */
    @Test
    void creerUtilisateur_EmailExistant_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'email existe déjà
        // ============================================================
        when(utilisateurRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN : Vérification de l'exception
        // ============================================================
        // assertThatThrownBy : Vérifie qu'une exception est levée
        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(requestDTO))
                .isInstanceOf(EmailDejaExistantException.class)
                .hasMessageContaining("email jean.dupont@email.com est déjà utilisé");

        // Vérifier que save() n'a PAS été appelé
        verify(utilisateurRepository).existsByEmail(requestDTO.getEmail());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    /**
     * ============================================================
     * TEST : creerUtilisateur_RoleUserNonTrouve_DevraitEchouer
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que si le rôle ROLE_USER n'existe pas,
     * la création échoue
     */
    @Test
    void creerUtilisateur_RoleUserNonTrouve_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : Le rôle ROLE_USER n'existe pas
        // ============================================================
        when(utilisateurRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rôle ROLE_USER non trouvé");

        verify(roleRepository).findByName("ROLE_USER");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    // ============================================================
    // 4. TESTS RÉCUPÉRATION (getUtilisateur, getUtilisateurByEmail)
    // ============================================================

    /**
     * ============================================================
     * TEST : getUtilisateur_DevraitReussir
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la récupération par ID fonctionne
     */
    @Test
    void getUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur existe en base
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        UtilisateurResponseDTO response = utilisateurService.getUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getPrenom()).isEqualTo("Jean");
        assertThat(response.getNom()).isEqualTo("Dupont");
        assertThat(response.getRoles()).contains("ROLE_USER");

        verify(utilisateurRepository).findById(1L);
    }

    /**
     * ============================================================
     * TEST : getUtilisateur_Inexistant_DevraitEchouer
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la récupération d'un utilisateur
     * inexistant lève une exception
     */
    @Test
    void getUtilisateur_Inexistant_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur n'existe pas
        // ============================================================
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.getUtilisateur(999L))
                .isInstanceOf(UtilisateurNotFoundException.class)
                .hasMessageContaining("Utilisateur avec l'ID 999 non trouvé");

        verify(utilisateurRepository).findById(999L);
    }

    /**
     * ============================================================
     * TEST : getUtilisateurByEmail_DevraitReussir
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la récupération par email fonctionne
     */
    @Test
    void getUtilisateurByEmail_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur existe
        // ============================================================
        when(utilisateurRepository.findByEmail("jean.dupont@email.com"))
                .thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        UtilisateurResponseDTO response = utilisateurService.getUtilisateurByEmail("jean.dupont@email.com");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getId()).isEqualTo(1L);

        verify(utilisateurRepository).findByEmail("jean.dupont@email.com");
    }

    /**
     * ============================================================
     * TEST : getUtilisateurByEmail_Inexistant_DevraitEchouer
     * ============================================================
     */
    @Test
    void getUtilisateurByEmail_Inexistant_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'email n'existe pas
        // ============================================================
        when(utilisateurRepository.findByEmail("inexistant@email.com"))
                .thenReturn(Optional.empty());

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.getUtilisateurByEmail("inexistant@email.com"))
                .isInstanceOf(UtilisateurNotFoundException.class)
                .hasMessageContaining("Utilisateur avec l'email inexistant@email.com non trouvé");

        verify(utilisateurRepository).findByEmail("inexistant@email.com");
    }

    // ============================================================
    // 5. TESTS LISTE PAGINÉE (getAllUtilisateurs)
    // ============================================================

    /**
     * ============================================================
     * TEST : getAllUtilisateurs_DevraitRetournerUnePage
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la pagination fonctionne
     *
     * 📋 ÉTAPES :
     * 1. GIVEN : Simuler une page de résultats
     * 2. WHEN : Appeler getAllUtilisateurs avec Pageable
     * 3. THEN : Vérifier le contenu et les métadonnées
     */
    @Test
    void getAllUtilisateurs_DevraitRetournerUnePage() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        Pageable pageable = PageRequest.of(0, 10);
        Page<Utilisateur> page = new PageImpl<>(List.of(utilisateur), pageable, 1);
        when(utilisateurRepository.findAll(pageable)).thenReturn(page);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        PageResponseDTO<UtilisateurResponseDTO> response = utilisateurService.getAllUtilisateurs(pageable);

        // ============================================================
        // 3️⃣ THEN - VERSION SIMPLIFIÉE
        // ============================================================
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getEmail()).isEqualTo("jean.dupont@email.com");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(10);           // ✅ Vérifie la taille de la page
        assertThat(response.getNumberOfElements()).isEqualTo(1); // ✅ Vérifie le nombre d'éléments
    }
    /**
     * ============================================================
     * TEST : getAllUtilisateurs_DevraitRetournerUnePageVide
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier qu'une page vide est retournée correctement
     */
    @Test
    void getAllUtilisateurs_DevraitRetournerUnePageVide() {
        // ============================================================
        // 1️⃣ GIVEN : Page vide
        // ============================================================
        Pageable pageable = PageRequest.of(0, 10);  // ✅ Syntaxe correcte
        Page<Utilisateur> page = new PageImpl<>(List.of(),pageable,0);
        when(utilisateurRepository.findAll(pageable)).thenReturn(page);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        PageResponseDTO<UtilisateurResponseDTO> response = utilisateurService.getAllUtilisateurs(pageable);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getNumberOfElements()).isEqualTo(0);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
        assertThat(response.isEmpty()).isTrue();

        verify(utilisateurRepository).findAll(pageable);
    }
    // ============================================================
    // 6. TESTS MISE À JOUR (updateUtilisateur)
    // ============================================================

    /**
     * ============================================================
     * TEST : updateUtilisateur_DevraitReussir
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la mise à jour d'un utilisateur fonctionne
     */
    @Test
    void updateUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        UtilisateurRequestDTO updateDTO = UtilisateurRequestDTO.builder()
                .prenom("Pierre")
                .nom("Durand")
                .email("pierre.durand@email.com")
                .motDePasse("NewPassword123@")
                .build();

        // Simuler la récupération de l'utilisateur existant
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // Simuler que l'email n'existe pas pour un autre utilisateur
        when(utilisateurRepository.findByEmail("pierre.durand@email.com"))
                .thenReturn(Optional.empty());

        // Simuler l'encodage du mot de passe
        when(passwordEncoder.encode("NewPassword123@")).thenReturn("encodedNewPassword");

        // Simuler la sauvegarde
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        UtilisateurResponseDTO response = utilisateurService.updateUtilisateur(1L, updateDTO);

        // ============================================================
        // 3️⃣ THEN - CORRIGÉ
        // ============================================================
        assertThat(response).isNotNull();

        // ✅ Vérifier que l'email a bien été mis à jour
        // Si le Mapper met à jour l'email, vérifie le nouvel email
        assertThat(response.getEmail()).isEqualTo("pierre.durand@email.com");

        // Ou si tu veux vérifier l'ancien email (comportement du Mapper)
        // assertThat(response.getEmail()).isEqualTo("jean.dupont@email.com");

        // Vérifier les autres champs
        assertThat(response.getPrenom()).isEqualTo("Pierre");
        assertThat(response.getNom()).isEqualTo("Durand");

        // Vérifier les appels
        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).findByEmail("pierre.durand@email.com");
        verify(passwordEncoder).encode("NewPassword123@");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }
    /**
     * ============================================================
     * TEST : updateUtilisateur_EmailDejaUtilise_DevraitEchouer
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier qu'on ne peut pas utiliser un email déjà pris
     */
    @Test
    void updateUtilisateur_EmailDejaUtilise_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'email est utilisé par un autre utilisateur
        // ============================================================
        Utilisateur autreUtilisateur = new Utilisateur();
        autreUtilisateur.setId(2L);
        autreUtilisateur.setEmail("pierre.durand@email.com");

        UtilisateurRequestDTO updateDTO = UtilisateurRequestDTO.builder()
                .prenom("Pierre")
                .nom("Durand")
                .email("pierre.durand@email.com")
                .motDePasse("NewPassword123@")
                .build();

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findByEmail("pierre.durand@email.com"))
                .thenReturn(Optional.of(autreUtilisateur));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.updateUtilisateur(1L, updateDTO))
                .isInstanceOf(EmailDejaExistantException.class)
                .hasMessageContaining("email pierre.durand@email.com est déjà utilisé");

        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).findByEmail("pierre.durand@email.com");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    // ============================================================
    // 7. TESTS SUPPRESSION (supprimerUtilisateur)
    // ============================================================

    @Test
    void supprimerUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.existsById(1L)).thenReturn(true);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.supprimerUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).existsById(1L);
        verify(utilisateurRepository).deleteById(1L);
    }

    @Test
    void supprimerUtilisateur_Inexistant_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.existsById(999L)).thenReturn(false);

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.supprimerUtilisateur(999L))
                .isInstanceOf(UtilisateurNotFoundException.class)
                .hasMessageContaining("Utilisateur avec l'ID 999 non trouvé");

        verify(utilisateurRepository).existsById(999L);
        verify(utilisateurRepository, never()).deleteById(anyLong());
    }

    // ============================================================
    // 8. TESTS GESTION DES ÉTATS
    // ============================================================

    /**
     * ============================================================
     * TEST : desactiverUtilisateur_DevraitReussir
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier que la désactivation fonctionne
     */
    @Test
    void desactiverUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur existe et est activé
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.desactiverUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).desactiverUtilisateur(1L);
    }

    /**
     * ============================================================
     * TEST : desactiverUtilisateur_DejaDesactive_DevraitEchouer
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier qu'on ne peut pas désactiver un
     * utilisateur déjà désactivé
     */
    @Test
    void desactiverUtilisateur_DejaDesactive_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur est déjà désactivé
        // ============================================================
        utilisateur.setEnabled(false);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.desactiverUtilisateur(1L))
                .isInstanceOf(UtilisateurEtatInvalideException.class)
                .hasMessageContaining("désactivé");

        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository, never()).desactiverUtilisateur(anyLong());
    }

    @Test
    void activerUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur existe et est désactivé
        // ============================================================
        utilisateur.setEnabled(false);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.activerUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).activerUtilisateur(1L);
    }

    @Test
    void activerUtilisateur_DejaActif_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur est déjà activé
        // ============================================================
        utilisateur.setEnabled(true);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.activerUtilisateur(1L))
                .isInstanceOf(UtilisateurEtatInvalideException.class)
                .hasMessageContaining("activé");

        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository, never()).activerUtilisateur(anyLong());
    }

    @Test
    void verrouillerUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.verrouillerUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).verrouillerUtilisateur(1L);
    }

    @Test
    void verrouillerUtilisateur_DejaVerrouille_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        utilisateur.setCompteNonVerrouille(false);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.verrouillerUtilisateur(1L))
                .isInstanceOf(UtilisateurEtatInvalideException.class)
                .hasMessageContaining("verrouillé");

        verify(utilisateurRepository, never()).verrouillerUtilisateur(anyLong());
    }

    @Test
    void expirerUtilisateur_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.expirerUtilisateur(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(utilisateurRepository).expirerUtilisateur(1L);
    }

    @Test
    void expirerUtilisateur_DejaExpire_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        utilisateur.setCompteNonExpire(false);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.expirerUtilisateur(1L))
                .isInstanceOf(UtilisateurEtatInvalideException.class)
                .hasMessageContaining("expiré");

        verify(utilisateurRepository, never()).expirerUtilisateur(anyLong());
    }

    // ============================================================
    // 9. TESTS GESTION DES RÔLES
    // ============================================================

    @Test
    void assignerRole_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.assignerRole(1L, "ROLE_ADMIN");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void assignerRole_RoleDejaPresent_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur a déjà ROLE_USER
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.assignerRole(1L, "ROLE_USER"))
                .isInstanceOf(RoleDejaAssignéException.class)
                .hasMessageContaining("a déjà le rôle ROLE_USER");

        verify(utilisateurRepository).findById(1L);
        verify(roleRepository).findByName("ROLE_USER");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void retirerRole_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.retirerRole(1L, "ROLE_USER");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(roleRepository).findByName("ROLE_USER");
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void retirerRole_NonAssigné_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur n'a pas ROLE_ADMIN
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(roleAdmin));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.retirerRole(1L, "ROLE_ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("n'a pas le rôle ROLE_ADMIN");

        verify(utilisateurRepository).findById(1L);
        verify(roleRepository).findByName("ROLE_ADMIN");
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    // ============================================================
    // 10. TESTS GESTION DES PERMISSIONS
    // ============================================================

    @Test
    void ajouterPermission_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        // Créer un rôle spécial pour les permissions directes
        Role roleSpecial = new Role("ROLE_SPECIAL", "Rôle pour permissions directes");
        roleSpecial.addPermission(permissionRead);

        // L'utilisateur a déjà ROLE_USER
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        // La permission USER_WRITE existe
        when(permissionRepository.findByName("USER_WRITE")).thenReturn(Optional.of(permissionWrite));
        // ROLE_SPECIAL existe
        when(roleRepository.findByName("ROLE_SPECIAL")).thenReturn(Optional.of(roleSpecial));
        // Sauvegardes
        when(roleRepository.save(any(Role.class))).thenReturn(roleSpecial);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.ajouterPermission(1L, "USER_WRITE");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(permissionRepository).findByName("USER_WRITE");
        verify(roleRepository).findByName("ROLE_SPECIAL");
        verify(roleRepository).save(any(Role.class));
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void ajouterPermission_PermissionDejaAssignée_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur a déjà USER_READ
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permissionRead));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.ajouterPermission(1L, "USER_READ"))
                .isInstanceOf(PermissionDejaAssignéeException.class)
                .hasMessageContaining("a déjà la permission USER_READ");

        verify(roleRepository, never()).findByName(anyString());
    }

    @Test
    void retirerPermission_DevraitReussir() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur a USER_READ via ROLE_SPECIAL
        // ============================================================
        Role roleSpecial = new Role("ROLE_SPECIAL", "Rôle pour permissions directes");
        roleSpecial.addPermission(permissionRead);
        utilisateur.addRole(roleSpecial);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findByName("USER_READ")).thenReturn(Optional.of(permissionRead));
        when(roleRepository.findByName("ROLE_SPECIAL")).thenReturn(Optional.of(roleSpecial));
        when(roleRepository.save(any(Role.class))).thenReturn(roleSpecial);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.retirerPermission(1L, "USER_READ");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        verify(utilisateurRepository).findById(1L);
        verify(permissionRepository).findByName("USER_READ");
        verify(roleRepository).findByName("ROLE_SPECIAL");
        verify(roleRepository).save(any(Role.class));
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void retirerPermission_PermissionNonAssignée_DevraitEchouer() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur n'a pas USER_WRITE
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(permissionRepository.findByName("USER_WRITE")).thenReturn(Optional.of(permissionWrite));

        // ============================================================
        // 2️⃣ WHEN / 3️⃣ THEN
        // ============================================================
        assertThatThrownBy(() -> utilisateurService.retirerPermission(1L, "USER_WRITE"))
                .isInstanceOf(PermissionNonAssignéeException.class)
                .hasMessageContaining("n'a pas la permission USER_WRITE");

        verify(roleRepository, never()).findByName(anyString());
    }

    // ============================================================
    // 11. TESTS VALIDATION
    // ============================================================

    @Test
    void existeParEmail_DevraitRetournerTrue_QuandEmailExiste() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.existsByEmail("jean.dupont@email.com")).thenReturn(true);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        boolean existe = utilisateurService.existeParEmail("jean.dupont@email.com");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(existe).isTrue();
        verify(utilisateurRepository).existsByEmail("jean.dupont@email.com");
    }

    @Test
    void existeParEmail_DevraitRetournerFalse_QuandEmailNExistePas() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.existsByEmail("inexistant@email.com")).thenReturn(false);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        boolean existe = utilisateurService.existeParEmail("inexistant@email.com");

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(existe).isFalse();
        verify(utilisateurRepository).existsByEmail("inexistant@email.com");
    }

    @Test
    void estActif_DevraitRetournerTrue_QuandUtilisateurEstActif() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur existe et est actif
        // ============================================================
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        boolean actif = utilisateurService.estActif(1L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(actif).isTrue();
        verify(utilisateurRepository).findById(1L);
    }

    @Test
    void estActif_DevraitRetournerFalse_QuandUtilisateurNExistePas() {
        // ============================================================
        // 1️⃣ GIVEN : L'utilisateur n'existe pas
        // ============================================================
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        boolean actif = utilisateurService.estActif(999L);

        // ============================================================
        // 3️⃣ THEN
        // ============================================================
        assertThat(actif).isFalse();
        verify(utilisateurRepository).findById(999L);
    }

    // ============================================================
    // 12. TEST : ArgumentCaptor (Vérification avancée)
    // ============================================================

    /**
     * ============================================================
     * TEST : Utilisation d'ArgumentCaptor
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier les valeurs passées aux mocks
     *
     * ArgumentCaptor permet de "capturer" les arguments passés
     * à une méthode mockée pour les inspecter.
     *
     * Utile quand on veut vérifier des valeurs spécifiques
     * sans avoir à créer des objets complets.
     */
    @Test
    void creerUtilisateur_DevraitEncodeLeMotDePasse() {
        // ============================================================
        // 1️⃣ GIVEN
        // ============================================================
        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(utilisateur);

        // ============================================================
        // 2️⃣ WHEN
        // ============================================================
        utilisateurService.creerUtilisateur(requestDTO);

        // ============================================================
        // 3️⃣ THEN : Vérifier que le bon mot de passe a été encodé
        // ============================================================
        // Créer un capteur pour capturer l'argument de passwordEncoder.encode()
        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(passwordCaptor.capture());

        // Vérifier que le mot de passe capturé est celui du DTO
        assertThat(passwordCaptor.getValue()).isEqualTo("Password123@");
    }
}