package com.formation.usermanagement.repository;

import com.formation.usermanagement.entity.Role;
import com.formation.usermanagement.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY ROLE (Sans @EntityGraph)
 *
 * Gère les opérations sur les rôles.
 *
 * ⚠️ Role a deux relations :
 * 1. permissions (ManyToMany) → UNIDIRECTIONNELLE, EAGER (défini dans l'entité)
 * 2. utilisateurs (ManyToMany) → BIDIRECTIONNELLE, LAZY (défini dans l'entité)
 *
 * ⚠️ Pour charger les relations LAZY, on utilise des requêtes JPQL avec JOIN FETCH
 *
 * Méthodes disponibles :
 * - findByName()                    : Recherche simple (permissions EAGER, utilisateurs LAZY)
 * - findByNameWithPermissions()     : Avec permissions (JOIN FETCH)
 * - findByNameWithUtilisateurs()    : Avec utilisateurs (JOIN FETCH)
 * - findAllWithUtilisateurs()       : Tous avec utilisateurs (JOIN FETCH)
 * - countUsersWithRole()            : Statistiques
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // ============================================================
    // 1. MÉTHODES DE RECHERCHE DE BASE
    // ============================================================

    /**
     * Trouve un rôle par son nom.
     *
     * Requête SQL générée :
     * SELECT * FROM roles WHERE name = ?
     *
     * ⚠️ Les permissions sont chargées automatiquement (EAGER dans l'entité).
     * Les utilisateurs restent LAZY (non chargés).
     *
     * @param name Le nom du rôle (ex: "ROLE_ADMIN")
     * @return Optional contenant le rôle ou vide
     */
    Optional<Role> findByName(String name);

    /**
     * Vérifie si un rôle existe par son nom.
     *
     * Requête SQL générée :
     * SELECT COUNT(*) > 0 FROM roles WHERE name = ?
     *
     * @param name Le nom du rôle
     * @return true si le rôle existe, false sinon
     */
    boolean existsByName(String name);

    /**
     * Trouve tous les rôles triés par nom.
     *
     * Requête SQL générée :
     * SELECT * FROM roles ORDER BY name ASC
     *
     * @return Liste triée des rôles
     */
    List<Role> findAllByOrderByNameAsc();

    // ============================================================
    // 2. MÉTHODES AVEC JOIN FETCH (Pour charger les relations)
    // ============================================================

    /**
     * Trouve un rôle avec ses permissions (chargement forcé).
     *
     * ⚠️ Utilise JOIN FETCH pour forcer le chargement des permissions
     * même si le fetch était LAZY.
     *
     * Requête JPQL :
     * SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name
     *
     * @param name Le nom du rôle
     * @return Optional contenant le rôle avec ses permissions
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);

    /**
     * Trouve un rôle avec ses utilisateurs (chargement forcé).
     *
     * ⚠️ Utilise JOIN FETCH pour forcer le chargement des utilisateurs
     * car ils sont LAZY dans l'entité.
     *
     * Requête JPQL :
     * SELECT r FROM Role r LEFT JOIN FETCH r.utilisateurs WHERE r.name = :name
     *
     * Utilisé quand on a besoin de savoir quels utilisateurs ont ce rôle.
     *
     * @param name Le nom du rôle
     * @return Optional contenant le rôle avec ses utilisateurs
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.utilisateurs WHERE r.name = :name")
    Optional<Role> findByNameWithUtilisateurs(@Param("name") String name);

    /**
     * Trouve tous les rôles avec leurs utilisateurs.
     *
     * ⚠️ Utilise JOIN FETCH pour charger les utilisateurs en une seule requête.
     *
     * Requête JPQL :
     * SELECT r FROM Role r LEFT JOIN FETCH r.utilisateurs
     *
     * Utilisé pour :
     * - Statistiques
     * - Interface d'administration
     * - Rapports
     *
     * ⚠️ ATTENTION : Peut charger beaucoup de données si beaucoup de rôles !
     *
     * @return Liste des rôles avec leurs utilisateurs
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.utilisateurs")
    List<Role> findAllWithUtilisateurs();

    /**
     * Trouve un rôle avec TOUTES ses relations.
     *
     * ⚠️ Charge les permissions ET les utilisateurs en une seule requête.
     *
     * Requête JPQL :
     * SELECT r FROM Role r
     * LEFT JOIN FETCH r.permissions
     * LEFT JOIN FETCH r.utilisateurs
     * WHERE r.name = :name
     *
     * ⚠️ ATTENTION : Cette requête peut être lourde !
     * À utiliser uniquement quand c'est vraiment nécessaire.
     *
     * @param name Le nom du rôle
     * @return Optional contenant le rôle avec toutes ses relations
     */
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH r.utilisateurs WHERE r.name = :name")
    Optional<Role> findByNameWithAllRelations(@Param("name") String name);

    // ============================================================
    // 3. MÉTHODES DE RECHERCHE AVANCÉE (JPQL)
    // ============================================================

    /**
     * Compte le nombre d'utilisateurs ayant un rôle spécifique.
     *
     * Requête JPQL :
     * SELECT COUNT(u) FROM Utilisateur u JOIN u.roles r WHERE r.name = :roleName
     *
     * @param roleName Le nom du rôle
     * @return Nombre d'utilisateurs ayant ce rôle
     */
    @Query("SELECT COUNT(u) FROM Utilisateur u JOIN u.roles r WHERE r.name = :roleName")
    long countUsersWithRole(@Param("roleName") String roleName);

    /**
     * Trouve les rôles qui ont une permission spécifique.
     *
     * Requête JPQL :
     * SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName
     *
     * @param permissionName Le nom de la permission (ex: "USER_READ")
     * @return Liste des rôles ayant cette permission
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findRolesByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Trouve les rôles qui ont une permission d'une catégorie spécifique.
     *
     * Requête JPQL :
     * SELECT r FROM Role r JOIN r.permissions p WHERE p.category = :category
     *
     * @param category La catégorie (ex: "USER")
     * @return Liste des rôles ayant des permissions de cette catégorie
     */
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.category = :category")
    List<Role> findRolesByPermissionCategory(@Param("category") String category);

    /**
     * Trouve les rôles qui n'ont pas d'utilisateurs (rôles vides).
     *
     * Requête JPQL :
     * SELECT r FROM Role r WHERE SIZE(r.utilisateurs) = 0
     *
     * Utilisé pour le nettoyage et la maintenance.
     *
     * @return Liste des rôles sans utilisateurs
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.utilisateurs) = 0")
    List<Role> findRolesWithoutUsers();

    /**
     * Trouve les rôles les plus populaires (avec le plus d'utilisateurs).
     *
     * Requête JPQL :
     * SELECT r.name, SIZE(r.utilisateurs) FROM Role r ORDER BY SIZE(r.utilisateurs) DESC
     *
     * @return Liste [roleName, count]
     */
    @Query("SELECT r.name, SIZE(r.utilisateurs) FROM Role r ORDER BY SIZE(r.utilisateurs) DESC")
    List<Object[]> findMostPopularRoles();
    @Query("SELECT COUNT(r) FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    long countRolesByPermissionName(@Param("permissionName") String permissionName);
     @Query("select r from Role r LEFT JOIN FETCH r.permissions")
    Page<Role> findAllRoleWithRealations(Pageable pageable);





}