package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * ENTITÉ PERMISSION (Version améliorée)
 *
 * Représente une permission atomique.
 *
 * Structure :
 * - category  : La catégorie (ex: "USER", "ROLE", "PRODUCT")
 * - name      : Le nom complet (ex: "USER_READ", "USER_DELETE")
 * - description : Description lisible
 *
 * Exemples :
 * - category = "USER", name = "USER_READ"     → Lire les utilisateurs
 * - category = "USER", name = "USER_DELETE"   → Supprimer des utilisateurs
 * - category = "ROLE", name = "ROLE_ASSIGN"   → Assigner des rôles
 *
 * Avantages :
 * - Plus clair pour les développeurs
 * - Plus simple pour Spring Security (une seule chaîne)
 * - Plus facile à grouper par catégorie
 *
 * ⚠️ La combinaison (category, name) est UNIQUE
 */
@Entity
@Table(
        name = "permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category", "name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Permission extends AbstractAuditableEntity {

    // ============================================================
    // CHAMPS PRINCIPAUX
    // ============================================================

    /**
     * category : La catégorie de la permission.
     * Exemples : "USER", "ROLE", "PRODUCT", "PERMISSION"
     *
     * Validation : @NotBlank (ne peut pas être vide)
     *              @Size(max = 50) (limite de caractères)
     *
     * ⚠️ @EqualsAndHashCode.Include : Identifie la permission
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "La catégorie est obligatoire")
    @Size(max = 50, message = "La catégorie ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * name : Le nom complet de la permission.
     * Format : {CATEGORY}_{ACTION}
     * Exemples : "USER_READ", "USER_WRITE", "ROLE_ASSIGN"
     *
     * ⚠️ C'est cette chaîne qui sera utilisée dans @PreAuthorize
     *
     * Validation : @NotBlank (ne peut pas être vide)
     *              @Size(max = 50) (limite de caractères)
     *
     * ⚠️ @EqualsAndHashCode.Include : Identifie la permission
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom de la permission est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * description : Description lisible de la permission.
     * Exemple : "Permet de consulter la liste des utilisateurs"
     * Non obligatoire, mais utile pour l'interface admin.
     */
    @Size(max = 100, message = "La description ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String description;

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Retourne le nom de la permission (utilisé par Spring Security).
     * C'est la chaîne utilisée dans @PreAuthorize("hasAuthority('USER_READ')")
     *
     * @return Le nom de la permission (ex: "USER_READ")
     */
    public String getAuthority() {
        return this.name;
    }

}