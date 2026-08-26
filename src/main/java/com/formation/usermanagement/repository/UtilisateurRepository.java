package com.formation.usermanagement.repository;

import com.formation.usermanagement.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY UTILISATEUR (Sans @EntityGraph)
 *
 * Gère toutes les opérations sur les utilisateurs.
 *
 * ⚠️ Les relations sont chargées selon le fetch défini dans l'entité :
 * - roles : fetch = EAGER (défini dans Utilisateur)
 * - Donc findByEmail() charge automatiquement les rôles
 *
 * Méthodes disponibles :
 *
 * RECHERCHE :
 * - findByEmail()              : Authentification (chargement EAGER)
 * - existsByEmail()            : Validation unicité
 * - searchByKeyword()          : Recherche avancée
 * - findByEnabledTrue/False()  : Filtrer par état
 *
 * GESTION DES ÉTATS (@Modifying) :
 * - desactiverUtilisateur()
 * - activerUtilisateur()
 * - verrouillerUtilisateur()
 * - deverrouillerUtilisateur()
 * - expirerUtilisateur()
 * - renouvelerUtilisateur()
 * - updateDerniereConnexion()
 *
 * STATISTIQUES :
 * - countUtilisateursActifs()
 * - countUtilisateursDesactives()
 * - findUtilisateursInactifs()
 * - countUtilisateursByRole()
 */
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // ============================================================
    // 1. MÉTHODES DE RECHERCHE DE BASE (Spring Data)
    // ============================================================

    /**
     * Trouve un utilisateur par son email.
     *
     * ⚠️ Les rôles sont chargés automatiquement car fetch = EAGER
     * dans l'entité Utilisateur sur la relation roles.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE email = ?
     *
     * @param email L'email de l'utilisateur
     * @return Optional contenant l'utilisateur ou vide
     */
    Optional<Utilisateur> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà en base.
     *
     * Requête SQL générée :
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les utilisateurs actifs.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE enabled = true
     *
     * @return Liste des utilisateurs actifs
     */
    List<Utilisateur> findByEnabledTrue();

    /**
     * Trouve tous les utilisateurs désactivés.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE enabled = false
     *
     * @return Liste des utilisateurs désactivés
     */
    List<Utilisateur> findByEnabledFalse();

    /**
     * Trouve les utilisateurs dont le compte est verrouillé.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE compte_non_verrouille = false
     *
     * @return Liste des utilisateurs verrouillés
     */
    List<Utilisateur> findByCompteNonVerrouilleFalse();

    /**
     * Trouve les utilisateurs dont le compte a expiré.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE compte_non_expire = false
     *
     * @return Liste des utilisateurs expirés
     */
    List<Utilisateur> findByCompteNonExpireFalse();

    /**
     * Trouve les utilisateurs dont le mot de passe a expiré.
     *
     * Requête SQL générée :
     * SELECT * FROM users WHERE credentials_non_expire = false
     *
     * @return Liste des utilisateurs avec mot de passe expiré
     */
    List<Utilisateur> findByCredentialsNonExpireFalse();

    // ============================================================
    // 2. RECHERCHE AVANCÉE (Requêtes JPQL avec JOIN FETCH)
    // ============================================================

    /**
     * Trouve un utilisateur par email avec chargement FORCÉ des rôles.
     *
     * ⚠️ Utilise JOIN FETCH pour forcer le chargement des rôles
     * même si le fetch est LAZY.
     *
     * Requête JPQL :
     * SELECT u FROM Utilisateur u JOIN FETCH u.roles WHERE u.email = :email
     *
     * @param email L'email de l'utilisateur
     * @return Optional contenant l'utilisateur avec ses rôles
     */
    @Query("SELECT u FROM Utilisateur u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithRoles(@Param("email") String email);

    /**
     * Trouve un utilisateur par ID avec chargement FORCÉ des rôles.
     *
     * Requête JPQL :
     * SELECT u FROM Utilisateur u JOIN FETCH u.roles WHERE u.id = :id
     *
     * @param id L'ID de l'utilisateur
     * @return Optional contenant l'utilisateur avec ses rôles
     */
    @Query("SELECT u FROM Utilisateur u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithRoles(@Param("id") Long id);

    /**
     * Recherche des utilisateurs par prénom ou nom.
     *
     * Requête JPQL :
     * SELECT u FROM Utilisateur u
     * WHERE LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))
     *    OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))
     *
     * @param keyword Le mot-clé à rechercher
     * @return Liste des utilisateurs correspondants
     */
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Utilisateur> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Recherche des utilisateurs par email.
     *
     * Requête JPQL :
     * SELECT u FROM Utilisateur u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
     *
     * @param keyword Le mot-clé à rechercher
     * @return Liste des utilisateurs correspondants
     */
    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Utilisateur> searchByEmailKeyword(@Param("keyword") String keyword);

    /**
     * Recherche des utilisateurs avec filtres multiples.
     *
     * Requête JPQL :
     * SELECT u FROM Utilisateur u WHERE
     * (:prenom IS NULL OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :prenom, '%'))) AND
     * (:nom IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND
     * (:enabled IS NULL OR u.enabled = :enabled)
     *
     * @param prenom Le prénom (peut être null)
     * @param nom Le nom (peut être null)
     * @param enabled L'état (peut être null)
     * @return Liste des utilisateurs correspondants
     */
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "(:prenom IS NULL OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :prenom, '%'))) AND " +
            "(:nom IS NULL OR LOWER(u.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
            "(:enabled IS NULL OR u.enabled = :enabled)")
    List<Utilisateur> searchWithFilters(
            @Param("prenom") String prenom,
            @Param("nom") String nom,
            @Param("enabled") Boolean enabled
    );

    // ============================================================
    // 3. MÉTHODES DE GESTION DES ÉTATS (@Modifying)
    // ============================================================

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.enabled = false WHERE u.id = :id")
    int desactiverUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.enabled = true WHERE u.id = :id")
    int activerUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.compteNonVerrouille = false WHERE u.id = :id")
    int verrouillerUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.compteNonVerrouille = true WHERE u.id = :id")
    int deverrouillerUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.compteNonExpire = false WHERE u.id = :id")
    int expirerUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.compteNonExpire = true WHERE u.id = :id")
    int renouvelerUtilisateur(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Utilisateur u SET u.derniereConnexion = :date WHERE u.id = :id")
    int updateDerniereConnexion(@Param("id") Long id, @Param("date") LocalDateTime date);

    // ============================================================
    // 4. MÉTHODES STATISTIQUES
    // ============================================================

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.enabled = true")
    long countUtilisateursActifs();

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.enabled = false")
    long countUtilisateursDesactives();

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.compteNonVerrouille = false")
    long countUtilisateursVerrouilles();

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.compteNonExpire = false")
    long countUtilisateursExpires();

    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion IS NULL")
    List<Utilisateur> findUtilisateursJamaisConnectes();

    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion < :dateThreshold")
    List<Utilisateur> findUtilisateursInactifs(@Param("dateThreshold") LocalDateTime dateThreshold);

    @Query("SELECT u FROM Utilisateur u JOIN u.roles r WHERE r.name = :roleName")
    List<Utilisateur> findUtilisateursByRole(@Param("roleName") String roleName);

    @Query("SELECT r.name, COUNT(u) FROM Utilisateur u JOIN u.roles r GROUP BY r.name")
    List<Object[]> countUtilisateursByRole();

    @Query("SELECT u FROM Utilisateur u WHERE u.derniereConnexion IS NOT NULL ORDER BY u.derniereConnexion DESC")
    List<Utilisateur> findDerniersConnectes();
}