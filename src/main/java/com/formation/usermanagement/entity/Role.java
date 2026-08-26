package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * ENTITÉ ROLE
 *
 * Représente un regroupement de permissions.
 *
 * Exemples de rôles :
 * - ROLE_ADMIN : Toutes les permissions
 * - ROLE_MANAGER : USER_READ, USER_WRITE, USER_ACTIVATE
 * - ROLE_USER : USER_READ
 *
 * ⚠️ RELATION 1 : ROLE → PERMISSION (UNIDIRECTIONNELLE)
 * - Role connaît ses Permissions
 * - Permission ne connaît PAS ses Rôles
 * - fetch = EAGER : Chargement immédiat pour Spring Security
 *
 * ⚠️ RELATION 2 : ROLE ↔ UTILISATEUR (BIDIRECTIONNELLE)
 * - Role connaît ses Utilisateurs
 * - Utilisateur connaît ses Rôles
 * - mappedBy = "roles" : Le côté propriétaire est Utilisateur
 */
@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Role extends AbstractAuditableEntity {

    // ============================================================
    // CHAMPS PRINCIPAUX
    // ============================================================

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(max = 50, message = "Le nom du rôle ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50, unique = true)
    private String name;  // "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"

    @Size(max = 100, message = "La description ne peut pas dépasser 100 caractères")
    @Column(length = 100)
    private String description;

    // ============================================================
    // RELATION 1 : ROLE → PERMISSION (UNIDIRECTIONNELLE)
    // ============================================================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    // ============================================================
    // RELATION 2 : ROLE ↔ UTILISATEUR (BIDIRECTIONNELLE)
    // ============================================================

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // === Gestion des permissions (unidirectionnelle) ===

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionName) {
        return this.permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    // === Gestion des utilisateurs (bidirectionnelle) ===

    public void addUtilisateur(Utilisateur utilisateur) {
        this.utilisateurs.add(utilisateur);
        utilisateur.getRoles().add(this);
    }

    public void removeUtilisateur(Utilisateur utilisateur) {
        this.utilisateurs.remove(utilisateur);
        utilisateur.getRoles().remove(this);
    }
}