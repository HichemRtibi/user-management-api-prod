package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * CLASSE PARENTE POUR TOUTES LES ENTITÉS
 *
 * Pourquoi cette classe ?
 * - Évite de répéter les champs d'audit dans User, Role, Permission
 * - Centralise la gestion des dates et des créateurs
 * - Ajoute un mécanisme de version (optimistic locking)
 *
 * Les annotations :
 * - @MappedSuperclass : Les classes filles héritent des champs, mais cette classe n'est pas une table
 * - @EntityListeners : Active les callbacks pour remplir automatiquement les dates
 * - @CreatedDate / @LastModifiedDate : Remplis automatiquement par Spring
 * - @CreatedBy / @LastModifiedBy : Remplis automatiquement par l'AuditorAware
 * - @Version : Incrémenté à chaque modification, protège contre les conflits
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractAuditableEntity {
    /**
     * ID technique auto-généré par MySQL (AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ============================================================
    // CHAMPS D'AUDIT - DATES
    // ============================================================

    /**
     * Date de création.
     * Rempli automatiquement par Spring lors du premier enregistrement.
     * updatable = false → Ne peut pas être modifié après création.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * Date de dernière modification.
     * Mis à jour automatiquement à chaque modification.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    /**
     * Email de l'utilisateur qui a créé l'enregistrement.
     * Rempli automatiquement par l'AuditorAware.
     * updatable = false → Ne peut pas être modifié après création.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    /**
     * Email de l'utilisateur qui a fait la dernière modification.
     * Rempli automatiquement par l'AuditorAware.
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    /**
     * VERSION : Protège contre les modifications simultanées.
     *
     * Principe :
     * 1. Lors de la lecture, on récupère la version (ex: 0)
     * 2. Lors de la sauvegarde, Hibernate vérifie que la version en base est la même
     * 3. Si elle a changé (ex: un autre utilisateur a modifié entre temps)
     *    → Exception OptimisticLockException
     *
     * Exemple :
     * - User A lit un utilisateur (version 0)
     * - User B lit le même utilisateur (version 0)
     * - User A modifie et sauvegarde → version passe à 1
     * - User B modifie et sauvegarde → Erreur (version en base = 1, pas 0)
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;
}
