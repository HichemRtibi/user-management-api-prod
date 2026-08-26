package com.formation.usermanagement.repository;

import com.formation.usermanagement.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY PERMISSION
 *
 * Gère les opérations sur les permissions.
 *
 * ⚠️ Permission n'a PAS de relations (pas de @ManyToMany côté Permission)
 * Donc pas besoin d'@EntityGraph ici !
 *
 * Méthodes disponibles :
 * - findByName()              : Recherche par nom
 * - findByCategory()          : Recherche par catégorie
 * - findAllByOrderBy...()     : Recherche triée
 * - countPermissionsByCategory() : Statistiques
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    // ============================================================
    // 1. MÉTHODES DE RECHERCHE DE BASE
    // ============================================================
    Optional<Permission> findByCategoryAndName(String category, String name);
    /**
     * Trouve une permission par son nom.
     *
     * Requête SQL générée :
     * SELECT * FROM permissions WHERE name = ?
     *
     * Utilisé pour :
     * - Vérifier l'existence d'une permission
     * - Assigner une permission à un rôle
     * - Vérifier dans @PreAuthorize
     *
     * @param name Le nom de la permission (ex: "USER_READ")
     * @return Optional contenant la permission ou vide
     */
    Optional<Permission> findByName(String name);

    /**
     * Vérifie si une permission existe par son nom.
     *
     * Requête SQL générée :
     * SELECT COUNT(*) > 0 FROM permissions WHERE name = ?
     *
     * Utilisé pour valider l'existence avant assignation.
     *
     * @param name Le nom de la permission
     * @return true si la permission existe, false sinon
     */
    boolean existsByName(String name);

    /**
     * Vérifie si une combinaison category/name existe.
     *
     * Requête SQL générée :
     * SELECT COUNT(*) > 0 FROM permissions WHERE category = ? AND name = ?
     *
     * @param category La catégorie (ex: "USER")
     * @param name Le nom (ex: "USER_READ")
     * @return true si la combinaison existe, false sinon
     */
    boolean existsByCategoryAndName(String category, String name);

    // ============================================================
    // 2. MÉTHODES DE RECHERCHE PAR CATÉGORIE
    // ============================================================

    /**
     * Trouve toutes les permissions d'une catégorie spécifique.
     *
     * Requête SQL générée :
     * SELECT * FROM permissions WHERE category = ?
     *
     * Utilisé pour :
     * - Lister les permissions disponibles sur une ressource
     * - Interface d'administration
     *
     * @param category La catégorie (ex: "USER")
     * @return Liste des permissions de cette catégorie
     */
    List<Permission> findByCategory(String category);

    /**
     * Trouve toutes les permissions d'une catégorie, triées par nom.
     *
     * Requête SQL générée :
     * SELECT * FROM permissions WHERE category = ? ORDER BY name ASC
     *
     * Utilisé pour l'affichage ordonné dans l'interface admin.
     *
     * @param category La catégorie
     * @return Liste triée des permissions
     */
    List<Permission> findByCategoryOrderByNameAsc(String category);

    /**
     * Trouve toutes les permissions, triées par catégorie puis par nom.
     *
     * Requête SQL générée :
     * SELECT * FROM permissions ORDER BY category ASC, name ASC
     *
     * Utilisé pour les listes complètes dans l'admin.
     *
     * @return Liste triée des permissions
     */
    List<Permission> findAllByOrderByCategoryAscNameAsc();

    // ============================================================
    // 3. MÉTHODES DE RECHERCHE AVEC REQUÊTES JPQL
    // ============================================================

    /**
     * Compte le nombre de permissions par catégorie.
     *
     * Requête JPQL :
     * SELECT p.category, COUNT(p) FROM Permission p GROUP BY p.category
     *
     * Utilisé pour les statistiques du dashboard.
     *
     * @return Liste d'objets [category, count]
     *         Exemple : [["USER", 6], ["ROLE", 1], ["PERMISSION", 1]]
     */
    @Query("SELECT p.category, COUNT(p) FROM Permission p GROUP BY p.category")
    List<Object[]> countPermissionsByCategory();

    /**
     * Recherche des permissions par mot-clé (dans category ou name).
     *
     * Requête JPQL :
     * SELECT p FROM Permission p
     * WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
     *    OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
     *
     * Utilisé pour la recherche rapide dans l'interface admin.
     *
     * @param keyword Le mot-clé à rechercher
     * @return Liste des permissions correspondantes
     */
    @Query("SELECT p FROM Permission p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Permission> searchByKeyword(@Param("keyword") String keyword);
}