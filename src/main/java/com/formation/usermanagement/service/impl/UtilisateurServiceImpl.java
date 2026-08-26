package com.formation.usermanagement.service.impl;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.entity.Permission;
import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;
import com.formation.usermanagement.exception.*;
import com.formation.usermanagement.mapper.UtilisateurMapper;
import com.formation.usermanagement.repository.PermissionRepository;
import com.formation.usermanagement.repository.RoleRepository;
import com.formation.usermanagement.repository.UtilisateurRepository;
import com.formation.usermanagement.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================================
 * IMPLÉMENTATION DU SERVICE UTILISATEUR
 * ============================================================
 *
 * 🎯 QU'EST-CE QUE CE SERVICE ?
 *
 * Ce service contient TOUTE la logique métier pour la gestion
 * des utilisateurs. C'est le cœur de l'application.
 *
 * 📋 QUE FAIT-IL ?
 *
 * 1. CRUD (Create, Read, Update, Delete)
 *    → Créer, récupérer, modifier, supprimer des utilisateurs
 *
 * 2. Gestion des états
 *    → Activer, désactiver, verrouiller, expirer des comptes
 *
 * 3. Gestion des rôles
 *    → Assigner et retirer des rôles aux utilisateurs
 *
 * 4. Recherche et pagination
 *    → Lister les utilisateurs avec pagination
 *
 * 5. Validation métier
 *    → Vérifier l'unicité de l'email
 *    → Vérifier les états avant les opérations
 *
 * ⚠️ POURQUOI LES ANNOTATIONS ?
 *
 * @Service          → Déclare que cette classe est un Service Spring
 * @Transactional    → Toutes les méthodes sont dans une transaction
 * @Slf4j            → Active les logs (log.info, log.debug, log.warn, log.error)
 * @RequiredArgsConstructor → Génère le constructeur avec les dépendances
 *
 * ⚠️ POURQUOI @Transactional ?
 *
 * - Les opérations de modification doivent être atomiques
 * - Si une erreur survient, toute la transaction est annulée (rollback)
 * - Exemple : si la sauvegarde échoue, rien n'est modifié
 *
 * ⚠️ POURQUOI DES EXCEPTIONS ?
 *
 * - Permet de gérer proprement les erreurs
 * - Le GlobalExceptionHandler les intercepte et retourne une réponse JSON
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    // ============================================================
    // 1. DÉPENDANCES (Injection par constructeur)
    // ============================================================

    /**
     * 📌 utilisateurRepository : Accès à la BDD pour les utilisateurs
     *
     * Pourquoi final ?
     * → Les dépendances sont injectées une fois et ne changent jamais
     * → Lombok génère le constructeur automatiquement
     *
     * Méthodes disponibles :
     * - save() : Sauvegarder un utilisateur
     * - findById() : Récupérer par ID
     * - findAll() : Récupérer tous
     * - deleteById() : Supprimer
     * - findByEmail() : Récupérer par email
     * - existsByEmail() : Vérifier l'existence
     * - desactiverUtilisateur() : Désactiver (méthode custom)
     * - activerUtilisateur() : Activer (méthode custom)
     * - etc.
     */
    private final UtilisateurRepository utilisateurRepository;

    /**
     * 📌 roleRepository : Accès à la BDD pour les rôles
     *
     * Méthodes disponibles :
     * - findByName() : Récupérer un rôle par son nom
     * - save() : Sauvegarder un rôle
     * - findAll() : Récupérer tous les rôles
     * - countUsersWithRole() : Compter les utilisateurs par rôle
     */
    private final RoleRepository roleRepository;

    /**
     * 📌 passwordEncoder : Encodeur de mot de passe (BCrypt)
     *
     * Pourquoi BCrypt ?
     * → Algorithme de hachage sécurisé
     * → Les mots de passe ne sont JAMAIS stockés en clair
     * → Chaque mot de passe a un "salt" différent
     *
     * Méthodes :
     * - encode() : Encoder un mot de passe
     * - matches() : Vérifier un mot de passe
     */
   private final PasswordEncoder passwordEncoder;

    // ============================================================
    // 2. CONSTANTES
    // ============================================================

    /**
     * 📌 ROLE_DEFAUT : Rôle assigné à tout nouvel utilisateur
     *
     * Pourquoi une constante ?
     * → Évite les "magic strings"
     * → Facile à modifier si le rôle change
     * → Centralisé en un seul endroit
     */
    private static final String ROLE_DEFAUT = "ROLE_USER";
    private final PermissionRepository permissionRepository;

    // ============================================================
    // 3. CRÉATION D'UN UTILISATEUR
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : creerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Créer un nouvel utilisateur
     *
     * 📋 ÉTAPES :
     *
     * 1. Vérifier que l'email n'existe pas déjà
     *    → Si oui → EmailDejaExistantException
     *
     * 2. Convertir le DTO en entité (Mapper)
     *    → UtilisateurMapper.toEntity(dto)
     *
     * 3. Encoder le mot de passe (BCrypt)
     *    → passwordEncoder.encode(dto.getMotDePasse())
     *
     * 4. Assigner le rôle par défaut
     *    → Chercher ROLE_USER en base
     *    → Si non trouvé → RuntimeException
     *
     * 5. Sauvegarder en base
     *    → utilisateurRepository.save(utilisateur)
     *
     * 6. Convertir l'entité sauvegardée en DTO
     *    → UtilisateurMapper.toResponseDTO(saved)
     *
     * 7. Retourner le DTO
     *
     * ⚠️ @Transactional : Si une erreur survient, la transaction est annulée
     *
     * 🔴 EXCEPTIONS POSSIBLES :
     * - EmailDejaExistantException : L'email est déjà utilisé
     * - RuntimeException : Le rôle ROLE_USER n'existe pas en base
     *
     * ✅ SUCCÈS : UtilisateurResponseDTO
     *
     * 📊 EXEMPLE D'APPEL :
     * UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
     *     .prenom("Jean")
     *     .nom("Dupont")
     *     .email("jean.dupont@email.com")
     *     .motDePasse("Password123@")
     *     .build();
     *
     * UtilisateurResponseDTO resultat = utilisateurService.creerUtilisateur(dto);
     * // resultat.getId() → 1
     * // resultat.getEmail() → "jean.dupont@email.com"
     * // resultat.getRoles() → ["ROLE_USER"]
     * // resultat.getMotDePasse() → N'EXISTE PAS (sécurité)
     */
    @Override
    @Transactional
    public UtilisateurResponseDTO creerUtilisateur(UtilisateurRequestDTO dto) {
        log.info("=== DÉBUT création utilisateur ===");
        log.info("📧 Email reçu : {}", dto.getEmail());
        log.info("👤 Prénom reçu : {}", dto.getPrenom());

        // ============================================================
        // ÉTAPE 1 : Vérifier l'unicité de l'email
        // ============================================================
        // Pourquoi ?
        // → L'email est l'identifiant unique de connexion
        // → On ne peut pas avoir deux utilisateurs avec le même email
        // → C'est une contrainte métier ET une contrainte de base de données
        log.debug("🔍 Vérification existence email : {}", dto.getEmail());

        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            log.warn("❌ Email déjà existant : {}", dto.getEmail());
            throw new EmailDejaExistantException(dto.getEmail());
        }
        log.info("✅ Email valide, création en cours...");

        // ============================================================
        // ÉTAPE 2 : Convertir DTO → Entité
        // ============================================================
        // Pourquoi ?
        // → Le DTO est ce que le client envoie (sans ID, sans audit)
        // → L'entité est ce qu'on stocke en base (avec ID, avec audit)
        // → Le Mapper fait la conversion manuelle (pas de magie noire)
        Utilisateur utilisateur = UtilisateurMapper.toEntity(dto);
        log.debug("📦 Entité créée avec email : {}", utilisateur.getEmail());

        // ============================================================
        // ÉTAPE 3 : Encoder le mot de passe
        // ============================================================
        // Pourquoi ?
        // → Les mots de passe ne sont JAMAIS stockés en clair
        // → BCrypt est un algorithme de hachage sécurisé
        // → Même si la BDD est compromise, les mots de passe sont protégés
       String encodedPassword = passwordEncoder.encode(dto.getMotDePasse());
       // String encodedPassword = dto.getMotDePasse();
        utilisateur.setMotDePasse(encodedPassword);
        log.debug("🔐 Mot de passe encodé");

        // ============================================================
        // ÉTAPE 4 : Assigner le rôle par défaut
        // ============================================================
        // Pourquoi ?
        // → Tout nouvel utilisateur doit avoir un rôle
        // → ROLE_USER est le rôle de base (permissions limitées)
        // → On pourrait aussi avoir ROLE_GUEST, ROLE_BASIC, etc.
        log.debug("🔍 Recherche du rôle : {}", ROLE_DEFAUT);

        Role roleUser = roleRepository.findByName(ROLE_DEFAUT)
                .orElseThrow(() -> {
                    log.error("❌ Rôle {} non trouvé en base !", ROLE_DEFAUT);
                    return new RuntimeException("Rôle " + ROLE_DEFAUT + " non trouvé en base de données");
                });

        utilisateur.addRole(roleUser);
        log.info("✅ Rôle {} assigné à l'utilisateur", ROLE_DEFAUT);

        // ============================================================
        // ÉTAPE 5 : Sauvegarder en base
        // ============================================================
        // Pourquoi ?
        // → C'est le moment où l'utilisateur est réellement créé
        // → Spring Data JPA génère l'ID automatiquement
        // → Les champs d'audit (createdAt, updatedAt) sont remplis
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("✅ Utilisateur sauvegardé avec ID : {}", saved.getId());

        // ============================================================
        // ÉTAPE 6 : Convertir en DTO et retourner
        // ============================================================
        // Pourquoi ?
        // → On ne retourne JAMAIS l'entité directement
        // → Le DTO cache le mot de passe et les champs sensibles
        // → Le DTO est ce que le client reçoit (API contract)
        UtilisateurResponseDTO response = UtilisateurMapper.toResponseDTO(saved);
        log.info("=== FIN création utilisateur (succès) ===");
        return response;
    }

    // ============================================================
    // 4. RÉCUPÉRATION D'UN UTILISATEUR
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : getUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer un utilisateur par son ID
     *
     * 📋 ÉTAPES :
     * 1. Chercher l'utilisateur en base (findById)
     * 2. Si non trouvé → UtilisateurNotFoundException
     * 3. Convertir en DTO
     * 4. Retourner le DTO
     *
     * ⚠️ Pas de @Transactional car c'est une opération de lecture
     *
     * 🔴 EXCEPTION : UtilisateurNotFoundException
     *
     * ✅ SUCCÈS : UtilisateurResponseDTO
     */
    @Override
    @Cacheable(value = "utilisateurs",key = "#id")
    public UtilisateurResponseDTO getUtilisateur(Long id) {
        log.info("🔍 🔴 Cache MISS - Accès à la BDD pour l'ID : {}", id);  // ← AJOUTER CECI


        // ============================================================
        // ÉTAPE 1 & 2 : Recherche et vérification d'existence
        // ============================================================
        // Pourquoi Optional ?
        // → findById() retourne Optional (peut être vide)
        // → orElseThrow() : Si vide, on lance une exception
        // → C'est plus propre que de vérifier null
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur avec ID {} non trouvé", id);
                    return new UtilisateurNotFoundException(id);
                });
        log.debug("✅ Utilisateur trouvé : {}", utilisateur.getEmail());

        // ============================================================
        // ÉTAPE 3 & 4 : Conversion et retour
        // ============================================================
        return UtilisateurMapper.toResponseDTO(utilisateur);
    }

    /**
     * ============================================================
     * MÉTHODE : getUtilisateurByEmail()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer un utilisateur par son email
     *
     * 📋 ÉTAPES : Similaire à getUtilisateur() mais avec email
     *
     * 🔴 EXCEPTION : UtilisateurNotFoundException
     *
     * ✅ SUCCÈS : UtilisateurResponseDTO
     */
    @Override
    @Cacheable(value = "utilisateurs",key = "#email")

    public UtilisateurResponseDTO getUtilisateurByEmail(String email) {
        log.debug("🔍 Récupération de l'utilisateur avec email : {}", email);

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur avec email {} non trouvé", email);
                    return new UtilisateurNotFoundException(email);
                });

        return UtilisateurMapper.toResponseDTO(utilisateur);
    }

    // ============================================================
    // 5. LISTE DES UTILISATEURS (PAGINATION)
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : getAllUtilisateurs()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer tous les utilisateurs avec pagination
     *
     * 📋 ÉTAPES :
     * 1. Appeler le repository avec les paramètres de pagination
     * 2. Mapper la page (convertir chaque Utilisateur en DTO)
     * 3. Utiliser le constructeur de PageResponseDTO
     *
     * Pourquoi la pagination ?
     * → Si on a 1000 utilisateurs, on ne peut pas tout retourner d'un coup
     * → Le frontend doit afficher les résultats page par page
     * → Optimisation des performances et de la mémoire
     *
     * 📊 Paramètres de pagination (Pageable) :
     * - page : Numéro de la page (commence à 0)
     * - size : Nombre d'éléments par page (ex: 10, 20, 50)
     * - sort : Tri (ex: "nom,asc")
     *
     * ✅ SUCCÈS : PageResponseDTO<UtilisateurResponseDTO>
     *
     * 📊 EXEMPLE DE RÉPONSE :
     * {
     *   "content": [
     *     { "id": 1, "prenom": "Jean", "nom": "Dupont", ... },
     *     { "id": 2, "prenom": "Marie", "nom": "Martin", ... }
     *   ],
     *   "totalElements": 42,
     *   "totalPages": 5,
     *   "size": 10,
     *   "number": 0,
     *   "numberOfElements": 2,
     *   "first": true,
     *   "last": false,
     *   "empty": false
     * }
     */
    @Override
    public PageResponseDTO<UtilisateurResponseDTO> getAllUtilisateurs(Pageable pageable) {
        log.info("📋 Récupération des utilisateurs - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        // ============================================================
        // ÉTAPE 1 : Récupérer la page depuis le repository
        // ============================================================
        // Page<Utilisateur> contient :
        // - content : List<Utilisateur> (les données)
        // - totalElements : Nombre total d'éléments
        // - totalPages : Nombre total de pages
        // - size : Taille de la page
        // - number : Numéro de la page
        // - first, last, empty, etc.
        Page<Utilisateur> page = utilisateurRepository.findAll(pageable);
        log.debug("📄 Page récupérée : {} éléments sur {}",
                page.getNumberOfElements(), page.getTotalElements());

        // ============================================================
        // ÉTAPE 2 : Mapper la page (Entité → DTO)
        // ============================================================
        // Pourquoi .map() ?
        // → .map() applique une fonction à chaque élément de la page
        // → UtilisateurMapper::toResponseDTO est la fonction de conversion
        // → Le résultat est une Page<UtilisateurResponseDTO>
        Page<UtilisateurResponseDTO> mappedPage = page.map(UtilisateurMapper::toResponseDTO);

        // ============================================================
        // ÉTAPE 3 : Créer le PageResponseDTO
        // ============================================================
        // Le constructeur de PageResponseDTO(Page<T>) remplit automatiquement
        // tous les champs (content, totalElements, totalPages, etc.)
        return new PageResponseDTO<>(mappedPage);
    }

    // ============================================================
    // 6. MISE À JOUR D'UN UTILISATEUR
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : updateUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Mettre à jour un utilisateur existant
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que l'utilisateur existe → UtilisateurNotFoundException
     * 2. Vérifier que l'email n'est pas utilisé par un autre utilisateur
     * 3. Mettre à jour les champs (Mapper.updateEntity)
     * 4. Encoder le mot de passe si modifié
     * 5. Sauvegarder
     * 6. Retourner le DTO
     *
     * ⚠️ @Transactional : La mise à jour est atomique
     *
     * 🔴 EXCEPTIONS POSSIBLES :
     * - UtilisateurNotFoundException : Utilisateur non trouvé
     * - EmailDejaExistantException : Email déjà utilisé par un autre
     *
     * ✅ SUCCÈS : UtilisateurResponseDTO
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER
    public UtilisateurResponseDTO updateUtilisateur(Long id, UtilisateurRequestDTO dto) {
        log.info("=== DÉBUT mise à jour utilisateur ID : {} ===", id);

        // ============================================================
        // ÉTAPE 1 : Vérifier que l'utilisateur existe
        // ============================================================
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur avec ID {} non trouvé", id);
                    return new UtilisateurNotFoundException(id);
                });
        log.debug("✅ Utilisateur trouvé : {}", utilisateur.getEmail());

        // ============================================================
        // ÉTAPE 2 : Vérifier l'unicité de l'email
        // ============================================================
        // ⚠️ On cherche par email, mais on exclut l'utilisateur courant
        // Pourquoi ? On peut garder son propre email
        utilisateurRepository.findByEmail(dto.getEmail())
                .ifPresent(existing -> {
                    // Si l'email existe pour un autre utilisateur → erreur
                    if (!existing.getId().equals(id)) {
                        log.warn("❌ Email {} déjà utilisé par un autre utilisateur", dto.getEmail());
                        throw new EmailDejaExistantException(dto.getEmail());
                    }
                });
        log.debug("✅ Email valide");

        // ============================================================
        // ÉTAPE 3 : Mettre à jour les champs
        // ============================================================
        // updateEntity() met à jour :
        // - prenom, nom, email
        // - motDePasse seulement s'il est fourni
        UtilisateurMapper.updateEntity(dto, utilisateur);

        // ============================================================
        // ÉTAPE 4 : Encoder le mot de passe si modifié
        // ============================================================
        // On vérifie si le mot de passe est présent dans le DTO
        // Si oui, on l'encode et on le met à jour
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(dto.getMotDePasse());
           // String encodedPassword = dto.getMotDePasse();
            utilisateur.setMotDePasse(encodedPassword);
            log.debug("🔐 Mot de passe mis à jour");
        }

        // ============================================================
        // ÉTAPE 5 : Sauvegarder
        // ============================================================
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("✅ Utilisateur {} mis à jour avec succès", saved.getEmail());
        log.info("=== FIN mise à jour utilisateur (succès) ===");

        return UtilisateurMapper.toResponseDTO(saved);
    }

    // ============================================================
    // 7. SUPPRESSION D'UN UTILISATEUR
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : supprimerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Supprimer un utilisateur
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que l'utilisateur existe → UtilisateurNotFoundException
     * 2. Supprimer
     *
     * ⚠️ @Transactional : La suppression est atomique
     *
     * 🔴 EXCEPTION : UtilisateurNotFoundException
     *
     * ✅ SUCCÈS : void (aucune donnée retournée)
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void supprimerUtilisateur(Long id) {
        log.info("🗑️ Suppression de l'utilisateur avec ID : {}", id);

        if (!utilisateurRepository.existsById(id)) {
            log.warn("❌ Utilisateur avec ID {} non trouvé", id);
            throw new UtilisateurNotFoundException(id);
        }

        utilisateurRepository.deleteById(id);
        log.info("✅ Utilisateur {} supprimé avec succès", id);
    }

    // ============================================================
    // 8. GESTION DES ÉTATS
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : desactiverUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Désactiver un utilisateur (enabled = false)
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que l'utilisateur existe → UtilisateurNotFoundException
     * 2. Vérifier qu'il n'est pas déjà désactivé → UtilisateurEtatInvalideException
     * 3. Désactiver (méthode custom du repository)
     *
     * Pourquoi désactiver plutôt que supprimer ?
     * → On garde l'historique des utilisateurs
     * → On peut réactiver facilement
     * → L'utilisateur ne peut plus se connecter
     *
     * 🔴 EXCEPTIONS :
     * - UtilisateurNotFoundException
     * - UtilisateurEtatInvalideException
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void desactiverUtilisateur(Long id) {
        log.info("🔒 Désactivation de l'utilisateur ID : {}", id);

        // 1. Vérifier l'existence
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        // 2. Vérifier l'état
        if (!utilisateur.isEnabled()) {
            log.warn("❌ Utilisateur {} déjà désactivé", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("désactivé", utilisateur.getEmail());
        }

        // 3. Désactiver
        utilisateurRepository.desactiverUtilisateur(id);
        log.info("✅ Utilisateur {} désactivé", utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : activerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Activer un utilisateur (enabled = true)
     *
     * 📋 ÉTAPES : Similaire à desactiverUtilisateur() mais inverse
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void activerUtilisateur(Long id) {
        log.info("🔓 Activation de l'utilisateur ID : {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        if (utilisateur.isEnabled()) {
            log.warn("❌ Utilisateur {} déjà activé", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("activé", utilisateur.getEmail());
        }

        utilisateurRepository.activerUtilisateur(id);
        log.info("✅ Utilisateur {} activé", utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : verrouillerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Verrouiller un utilisateur (compteNonVerrouille = false)
     *
     * Pourquoi verrouiller ?
     * → Après plusieurs tentatives de connexion échouées
     * → Mesure de sécurité
     * → L'utilisateur ne peut pas se connecter
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void verrouillerUtilisateur(Long id) {
        log.info("🔒 Verrouillage de l'utilisateur ID : {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        if (!utilisateur.isCompteNonVerrouille()) {
            log.warn("❌ Utilisateur {} déjà verrouillé", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("verrouillé", utilisateur.getEmail());
        }

        utilisateurRepository.verrouillerUtilisateur(id);
        log.info("✅ Utilisateur {} verrouillé", utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : deverrouillerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Déverrouiller un utilisateur (compteNonVerrouille = true)
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void deverrouillerUtilisateur(Long id) {
        log.info("🔓 Déverrouillage de l'utilisateur ID : {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        if (utilisateur.isCompteNonVerrouille()) {
            log.warn("❌ Utilisateur {} n'est pas verrouillé", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("déverrouillé", utilisateur.getEmail());
        }

        utilisateurRepository.deverrouillerUtilisateur(id);
        log.info("✅ Utilisateur {} déverrouillé", utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : expirerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Expirer un utilisateur (compteNonExpire = false)
     *
     * Pourquoi expirer ?
     * → Contrat de l'utilisateur arrivé à échéance
     * → Compte temporaire qui expire
     * → L'utilisateur ne peut pas se connecter
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void expirerUtilisateur(Long id) {
        log.info("⏰ Expiration de l'utilisateur ID : {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        if (!utilisateur.isCompteNonExpire()) {
            log.warn("❌ Utilisateur {} déjà expiré", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("expiré", utilisateur.getEmail());
        }

        utilisateurRepository.expirerUtilisateur(id);
        log.info("✅ Utilisateur {} expiré", utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : renouvelerUtilisateur()
     * ============================================================
     *
     * 🎯 OBJECTIF : Renouveler un utilisateur (compteNonExpire = true)
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void renouvelerUtilisateur(Long id) {
        log.info("🔄 Renouvellement de l'utilisateur ID : {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurNotFoundException(id));

        if (utilisateur.isCompteNonExpire()) {
            log.warn("❌ Utilisateur {} n'est pas expiré", utilisateur.getEmail());
            throw new UtilisateurEtatInvalideException("renouvelé", utilisateur.getEmail());
        }

        utilisateurRepository.renouvelerUtilisateur(id);
        log.info("✅ Utilisateur {} renouvelé", utilisateur.getEmail());
    }

    // ============================================================
    // 9. GESTION DES RÔLES
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : assignerRole()
     * ============================================================
     *
     * 🎯 OBJECTIF : Assigner un rôle à un utilisateur
     *
     * 📋 ÉTAPES :
     * 1. Vérifier que l'utilisateur existe → UtilisateurNotFoundException
     * 2. Vérifier que le rôle existe → RoleNotFoundException
     * 3. Vérifier que l'utilisateur n'a pas déjà ce rôle → RoleDejaAssignéException
     * 4. Assigner le rôle
     *
     * Pourquoi assigner des rôles ?
     * → Les rôles définissent les permissions de l'utilisateur
     * → ADMIN a toutes les permissions
     * → USER a des permissions limitées
     *
     * 🔴 EXCEPTIONS :
     * - UtilisateurNotFoundException
     * - RoleNotFoundException
     * - RoleDejaAssignéException
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void assignerRole(Long userId, String roleName) {
        log.info("📋 Assignation du rôle {} à l'utilisateur ID : {}", roleName, userId);

        // 1. Vérifier l'existence de l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new UtilisateurNotFoundException(userId));

        // 2. Vérifier l'existence du rôle
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        // 3. Vérifier que l'utilisateur n'a pas déjà ce rôle
        if (utilisateur.hasRole(roleName)) {
            log.warn("❌ Utilisateur {} a déjà le rôle {}", utilisateur.getEmail(), roleName);
            throw new RoleDejaAssignéException(utilisateur.getEmail(), roleName);
        }

        // 4. Assigner le rôle
        utilisateur.addRole(role);
        utilisateurRepository.save(utilisateur);
        log.info("✅ Rôle {} assigné à l'utilisateur {}", roleName, utilisateur.getEmail());
    }

    /**
     * ============================================================
     * MÉTHODE : retirerRole()
     * ============================================================
     *
     * 🎯 OBJECTIF : Retirer un rôle à un utilisateur
     */
    @Override
    @Transactional
    @CacheEvict(value = "utilisateurs", allEntries = true)  // ← AJOUTER

    public void retirerRole(Long userId, String roleName) {
        log.info("🗑️ Retrait du rôle {} de l'utilisateur ID : {}", roleName, userId);

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new UtilisateurNotFoundException(userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException(roleName));

        if (!utilisateur.hasRole(roleName)) {
            log.warn("❌ Utilisateur {} n'a pas le rôle {}", utilisateur.getEmail(), roleName);
            throw new RuntimeException("L'utilisateur n'a pas le rôle " + roleName);
        }

        utilisateur.removeRole(role);
        utilisateurRepository.save(utilisateur);
        log.info("✅ Rôle {} retiré de l'utilisateur {}", roleName, utilisateur.getEmail());
    }
    /**
 * ============================================================
         * MÉTHODE : ajouterPermission()
     * ============================================================
             *
             * 🎯 OBJECTIF : Ajouter une permission à un utilisateur (directement)
     *
             * 📋 ÉTAPES :
            * 1. Vérifier que l'utilisateur existe → UtilisateurNotFoundException
            * 2. Vérifier que la permission existe → PermissionNotFoundException
     * 3. Vérifier que l'utilisateur n'a pas déjà cette permission
     *    → PermissionDejaAssignéeException
     * 4. Ajouter la permission à l'utilisateur
            *
            * Pourquoi ajouter des permissions directement ?
            * → Permet de donner des droits spécifiques à un utilisateur
     * → Sans avoir à créer un rôle dédié
     * → Utile pour des cas exceptionnels
     *  ⚠️ Attention : Une permission ajoutée directement à un utilisateur
     *      *    est indépendante des permissions de ses rôles.
     *      *
     *      * 🔴 EXCEPTIONS :
     *      * - UtilisateurNotFoundException
     *      * - PermissionNotFoundException
     *      * - PermissionDejaAssignéeException
     *      */
    @Override
    @Transactional
    public void ajouterPermission(Long userId, String permissionName) {
        log.info("📋 Ajout de la permission {} à l'utilisateur ID : {}", permissionName, userId);

        // ============================================================
        // ÉTAPE 1 : Vérifier que l'utilisateur existe
        // ============================================================
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur avec ID {} non trouvé", userId);
                    return new UtilisateurNotFoundException(userId);
                });
        log.debug("✅ Utilisateur trouvé : {}", utilisateur.getEmail());
        // ============================================================
        // ÉTAPE 2 : Vérifier que la permission existe
        // ============================================================
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> {
                    log.warn("❌ Permission {} non trouvée", permissionName);
                    return new PermissionNotFoundException(permissionName);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());

        // ============================================================
        // ÉTAPE 3 : Vérifier que l'utilisateur n'a pas déjà cette permission
        // ============================================================
        // ⚠️ On vérifie dans TOUTES les permissions (rôles + directes)
        if (utilisateur.getAllPermissions().contains(permissionName)) {
            log.warn("❌ Utilisateur {} a déjà la permission {}",
                    utilisateur.getEmail(), permissionName);
            throw new PermissionDejaAssignéeException(utilisateur.getEmail(), permissionName);
        }// ============================================================
        // ÉTAPE 4 : Ajouter la permission à l'utilisateur
        // ============================================================
        // ⚠️ On ajoute la permission directement à l'utilisateur
        // Pour cela, on crée un rôle spécial ou on ajoute dans une collection
        // Ici, on va créer un rôle "ROLE_SPECIAL" qui contient cette permission

        // 4.1. Chercher ou créer un rôle spécial pour les permissions directes
        Role roleSpecial = roleRepository.findByName("ROLE_SPECIAL")
                .orElseGet(() -> {
                    log.info("Création du rôle ROLE_SPECIAL pour les permissions directes");
                    Role newRole = new Role("ROLE_SPECIAL", "Rôle pour les permissions directes");
                    return roleRepository.save(newRole);
                });

        // 4.2. Ajouter la permission au rôle spécial
        roleSpecial.addPermission(permission);
        roleRepository.save(roleSpecial);

        // 4.3. Ajouter le rôle spécial à l'utilisateur
        if (!utilisateur.hasRole("ROLE_SPECIAL")) {
            utilisateur.addRole(roleSpecial);
        }
        utilisateurRepository.save(utilisateur);

        log.info("✅ Permission {} ajoutée à l'utilisateur {}",
                permissionName, utilisateur.getEmail());
    }

    @Override
    @Transactional
    public void retirerPermission(Long userId, String permissionName) {
        log.info("🗑️ Retrait de la permission {} de l'utilisateur ID : {}", permissionName, userId);

        // ============================================================
        // ÉTAPE 1 : Vérifier que l'utilisateur existe
        // ============================================================
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur avec ID {} non trouvé", userId);
                    return new UtilisateurNotFoundException(userId);
                });
        log.debug("✅ Utilisateur trouvé : {}", utilisateur.getEmail());

    // ============================================================
    // 10. RECHERCHE ET VALIDATION
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : existeParEmail()
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier si un email existe déjà
     *
     * Utilisé par :
     * - Le Controller pour vérifier avant la création
     * - L'authentification pour vérifier l'existence
     *
     * ✅ RETOUR : boolean (true si l'email existe)
     */
        // ============================================================
        // ÉTAPE 2 : Vérifier que la permission existe
        // ============================================================
        Permission permission = permissionRepository.findByName(permissionName)
                .orElseThrow(() -> {
                    log.warn("❌ Permission {} non trouvée", permissionName);
                    return new PermissionNotFoundException(permissionName);
                });
        log.debug("✅ Permission trouvée : {}", permission.getName());
        // ============================================================
        // ÉTAPE 3 : Vérifier que l'utilisateur a cette permission
        // ============================================================
        if (!utilisateur.getAllPermissions().contains(permissionName)) {
            log.warn("❌ Utilisateur {} n'a pas la permission {}",
                    utilisateur.getEmail(), permissionName);
            throw new PermissionNonAssignéeException(utilisateur.getEmail(), permissionName);
        }
        // ============================================================
        // ÉTAPE 4 : Retirer la permission
        // ============================================================
        // 4.1. Trouver le rôle spécial qui contient cette permission
        Role roleSpecial = roleRepository.findByName("ROLE_SPECIAL")
                .orElseThrow(() -> new RuntimeException("ROLE_SPECIAL non trouvé"));

        // 4.2. Retirer la permission du rôle spécial
        roleSpecial.removePermission(permission);
        roleRepository.save(roleSpecial);

        // 4.3. Si le rôle spécial n'a plus de permissions, le retirer de l'utilisateur
        if (roleSpecial.getPermissions().isEmpty()) {
            utilisateur.removeRole(roleSpecial);
            log.debug("ROLE_SPECIAL vidé, retiré de l'utilisateur");
        }
        // 4.4. Sauvegarder l'utilisateur
        utilisateurRepository.save(utilisateur);

        log.info("✅ Permission {} retirée de l'utilisateur {}",
                permissionName, utilisateur.getEmail());
    }

    @Override
    public boolean existeParEmail(String email) {
        return utilisateurRepository.existsByEmail(email);
    }

    /**
     * ============================================================
     * MÉTHODE : estActif()
     * ============================================================
     *
     * 🎯 OBJECTIF : Vérifier si un utilisateur est actif (enabled = true)
     *
     * ✅ RETOUR : boolean (true si l'utilisateur est actif)
     */
    @Override
    public boolean estActif(Long id) {
        return utilisateurRepository.findById(id)
                .map(Utilisateur::isEnabled)
                .orElse(false);
    }

    // ============================================================
    // 11. RECHERCHE AVANCÉE
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : rechercherUtilisateurs()
     * ============================================================
     *
     * 🎯 OBJECTIF : Rechercher des utilisateurs par mot-clé
     *
     * 📋 ÉTAPES :
     * 1. Récupérer les utilisateurs correspondant au mot-clé
     * 2. Paginer manuellement les résultats
     * 3. Mapper en DTO
     * 4. Retourner un PageResponseDTO
     *
     * ⚠️ Note : À améliorer avec une méthode paginée dans le repository
     *
     * @param keyword Le mot-clé à rechercher (dans le prénom ou le nom)
     * @param pageable Les paramètres de pagination
     * @return PageResponseDTO avec les résultats
     */
    @Override
    public PageResponseDTO<UtilisateurResponseDTO> rechercherUtilisateurs(String keyword, Pageable pageable) {
        log.info("🔍 Recherche d'utilisateurs - Mot-clé: {}, Page: {}",
                keyword, pageable.getPageNumber());

        // 1. Récupérer les utilisateurs (non paginé)
        List<Utilisateur> utilisateurs = utilisateurRepository.searchByKeyword(keyword);

        // 2. Pagination manuelle
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), utilisateurs.size());
        List<Utilisateur> pageContent = utilisateurs.subList(start, end);

        // 3. Mapper en DTO
        List<UtilisateurResponseDTO> mappedContent = pageContent.stream()
                .map(UtilisateurMapper::toResponseDTO)
                .collect(Collectors.toList());

        // 4. Créer la page
        Page<UtilisateurResponseDTO> page = new PageImpl<>(
                mappedContent,
                pageable,
                utilisateurs.size()
        );

        return new PageResponseDTO<>(page);
    }

    /**
     * ============================================================
     * MÉTHODE : getUtilisateursByRole()
     * ============================================================
     *
     * 🎯 OBJECTIF : Récupérer les utilisateurs ayant un rôle spécifique
     *
     * @param roleName Le nom du rôle
     * @param pageable Les paramètres de pagination
     * @return PageResponseDTO avec les résultats
     */
    @Override
    public PageResponseDTO<UtilisateurResponseDTO> getUtilisateursByRole(String roleName, Pageable pageable) {
        log.info("📋 Récupération des utilisateurs avec le rôle: {}", roleName);

        List<Utilisateur> utilisateurs = utilisateurRepository.findUtilisateursByRole(roleName);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), utilisateurs.size());
        List<Utilisateur> pageContent = utilisateurs.subList(start, end);

        List<UtilisateurResponseDTO> mappedContent = pageContent.stream()
                .map(UtilisateurMapper::toResponseDTO)
                .collect(Collectors.toList());

        Page<UtilisateurResponseDTO> page = new PageImpl<>(
                mappedContent,
                pageable,
                utilisateurs.size()
        );

        return new PageResponseDTO<>(page);
    }

    // ============================================================
    // 12. MÉTHODE DE CONNEXION (Login)
    // ============================================================

    /**
     * ============================================================
     * MÉTHODE : updateDerniereConnexion()
     * ============================================================
     *
     * 🎯 OBJECTIF : Mettre à jour la dernière date de connexion
     *
     * 📋 ÉTAPES :
     * 1. Trouver l'utilisateur par email
     * 2. Mettre à jour la date de connexion
     *
     * Utilisé par :
     * - AuthController après un login réussi
     * - Pour le suivi des connexions
     * - Pour les rapports d'activité
     *
     * @param email L'email de l'utilisateur qui s'est connecté
     */
    @Transactional
    public void updateDerniereConnexion(String email) {
        utilisateurRepository.findByEmail(email)
                .ifPresent(utilisateur -> {
                    utilisateurRepository.updateDerniereConnexion(
                            utilisateur.getId(),
                            LocalDateTime.now()
                    );
                    log.debug("🕐 Dernière connexion mise à jour pour : {}", email);
                });
    }
}