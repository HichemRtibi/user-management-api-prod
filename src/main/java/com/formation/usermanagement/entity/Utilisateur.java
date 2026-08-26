package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * ENTITÉ UTILISATEUR
 *
 * Représente un utilisateur de l'application.
 *
 * Pourquoi "Utilisateur" et pas "User" ?
 * - Évite le conflit avec org.springframework.security.core.userdetails.User
 * - Nom plus explicite en français
 * - Spring Security utilisera sa propre classe User pour UserDetails
 *
 * ⚠️ RELATION : UTILISATEUR ↔ ROLE (BIDIRECTIONNELLE)
 * - Utilisateur connaît ses Rôles
 * - Role connaît ses Utilisateurs
 * - Côté propriétaire : Utilisateur (gère la table user_roles)
 * - fetch = EAGER : Pour Spring Security (charge immédiatement les rôles)
 *
 * ⚠️ ANNOTATIONS LOMBOK :
 * - @ToString.Exclude sur roles : Évite la boucle infinie !
 * - @EqualsAndHashCode.Exclude sur roles : Évite la boucle infinie !
 * - @EqualsAndHashCode.Include sur email, prenom, nom : Identifie l'utilisateur
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Utilisateur extends AbstractAuditableEntity {

    // ============================================================
    // CHAMPS PRINCIPAUX
    // ============================================================

    /**
     * Prénom de l'utilisateur.
     * Validation : @NotBlank (ne peut pas être vide)
     *              @Size(max = 50) (limite de caractères)
     *
     * ⚠️ @EqualsAndHashCode.Include : Identifie l'utilisateur
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String prenom;

    /**
     * Nom de l'utilisateur.
     * Validation : @NotBlank (ne peut pas être vide)
     *              @Size(max = 50) (limite de caractères)
     *
     * ⚠️ @EqualsAndHashCode.Include : Identifie l'utilisateur
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50)
    private String nom;

    /**
     * Email de l'utilisateur.
     * Sert d'identifiant unique pour la connexion.
     *
     * Validation :
     * - @NotBlank : ne peut pas être vide
     * - @Email : doit respecter le format email (ex: user@domain.com)
     * - @Size(max = 100) : limite de caractères
     * - @Column(unique = true) : contrainte d'unicité en base
     *
     * ⚠️ @EqualsAndHashCode.Include : Identifie l'utilisateur
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide (ex: user@domain.com)")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    /**
     * Mot de passe de l'utilisateur.
     * Sera stocké sous forme de hash (BCrypt), jamais en clair.
     *
     * Validation :
     * - @NotBlank : ne peut pas être vide
     * - @Size(min = 8) : minimum 8 caractères pour la sécurité
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Column(nullable = false)
    private String motDePasse;

    // ============================================================
    // GESTION DES ÉTATS DU COMPTE (Utilisés par Spring Security)
    // ============================================================

    /**
     * enabled : Compte activé ou désactivé.
     * - true : L'utilisateur peut se connecter
     * - false : L'utilisateur ne peut pas se connecter (ex: compte désactivé par l'admin)
     * Valeur par défaut : true
     *
     * Utilisé par Spring Security : si false, throws DisabledException
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * compteNonVerrouille : Compte verrouillé ou déverrouillé.
     * - true : Le compte est accessible
     * - false : Le compte est verrouillé (ex: trop de tentatives échouées)
     * Valeur par défaut : true
     *
     * Utilisé par Spring Security : si false, throws LockedException
     */
    @Column(name = "compte_non_verrouille", nullable = false)
    private boolean compteNonVerrouille = true;

    /**
     * compteNonExpire : Compte expiré ou non.
     * - true : Le compte est actif
     * - false : Le compte a expiré (ex: contrat terminé, date de fin dépassée)
     * Valeur par défaut : true
     *
     * Utilisé par Spring Security : si false, throws AccountExpiredException
     */
    @Column(name = "compte_non_expire", nullable = false)
    private boolean compteNonExpire = true;

    /**
     * credentialsNonExpire : Mot de passe expiré ou non.
     * - true : Le mot de passe est valide
     * - false : Le mot de passe a expiré (forcer le changement)
     * Valeur par défaut : true
     *
     * Utilisé par Spring Security : si false, throws CredentialsExpiredException
     */
    @Column(name = "credentials_non_expire", nullable = false)
    private boolean credentialsNonExpire = true;

    // ============================================================
    // INFORMATIONS DE CONNEXION
    // ============================================================

    /**
     * derniereConnexion : Dernière date de connexion.
     * Mis à jour à chaque login réussi.
     * Nullable (peut être null si l'utilisateur ne s'est jamais connecté).
     */
    @Column(name = "last_login_date")
    private LocalDateTime derniereConnexion;

    // ============================================================
    // RELATION : UTILISATEUR ↔ ROLE (BIDIRECTIONNELLE)
    // ============================================================

    /**
     * Relations Many-to-Many avec Role.
     *
     * ⚠️ BIDIRECTIONNELLE : Utilisateur connaît ses Rôles
     *
     * Pourquoi fetch = FetchType.EAGER ?
     * - Spring Security a BESOIN des rôles et permissions immédiatement
     * - Les rôles chargent leurs permissions (FetchType.EAGER dans Role)
     * - Tout est chargé en une seule requête
     *
     * Pourquoi @JoinTable ?
     * - Définit la table de liaison user_roles
     * - C'est LE côté propriétaire de la relation
     * - name = "user_roles" : Nom de la table de liaison
     * - joinColumns = @JoinColumn(name = "user_id") : Colonne pour l'utilisateur
     * - inverseJoinColumns = @JoinColumn(name = "role_id") : Colonne pour le rôle
     *
     * Pourquoi @ToString.Exclude ?
     * - ÉVITE LA BOUCLE INFINIE !
     * - Utilisateur.toString() → Role.toString() → Utilisateur.toString()...
     * - Sans cette annotation → StackOverflowError
     *
     * Pourquoi @EqualsAndHashCode.Exclude ?
     * - ÉVITE LA BOUCLE INFINIE !
     * - Utilisateur.equals() → Role.equals() → Utilisateur.equals()...
     * - Sans cette annotation → StackOverflowError
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude      // ← IMPÉRATIF : Évite la boucle dans toString()
    @EqualsAndHashCode.Exclude  // ← IMPÉRATIF : Évite la boucle dans equals()
    private Set<Role> roles = new HashSet<>();

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Retourne le nom complet de l'utilisateur.
     */
    public String getNomComplet() {
        return this.prenom + " " + this.nom;
    }

    // ============================================================
    // MÉTHODES POUR LA RELATION UTILISATEUR ↔ ROLE (Bidirectionnelle)
    // ============================================================

    /**
     * Ajoute un rôle à l'utilisateur.
     * ⚠️ Gère les DEUX côtés de la relation pour maintenir la cohérence !
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUtilisateurs().add(this);  // ← MAINTIENT LA COHÉRENCE
    }

    /**
     * Retire un rôle de l'utilisateur.
     * ⚠️ Gère les DEUX côtés de la relation pour maintenir la cohérence !
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUtilisateurs().remove(this);  // ← MAINTIENT LA COHÉRENCE
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique.
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Récupère toutes les permissions de l'utilisateur.
     * Parcourt tous les rôles et agrège toutes les permissions.
     *
     * ⚠️ Cette méthode est CRUCIALE pour Spring Security !
     * Elle sera utilisée pour construire les GrantedAuthority.
     *
     * @return Set de chaînes comme "UTILISATEUR_LIRE", "UTILISATEUR_SUPPRIMER"
     */
    public Set<String> getAllPermissions() {
        Set<String> allPermissions = new HashSet<>();
        for (Role role : this.roles) {
            for (Permission permission : role.getPermissions()) {
                allPermissions.add(permission.getAuthority());
            }
        }
        return allPermissions;
    }

    /**
     * Récupère tous les noms des rôles de l'utilisateur.
     * Utile pour les logs et le debug.
     */
    public Set<String> getRoleNames() {
        Set<String> roleNames = new HashSet<>();
        for (Role role : this.roles) {
            roleNames.add(role.getName());
        }
        return roleNames;
    }
}