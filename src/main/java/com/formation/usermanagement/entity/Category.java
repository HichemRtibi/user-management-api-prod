package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 * ENTITÉ CATEGORY
 * ================================================================
 *
 * 📖 EXPLICATION MÉTIER :
 *
 * Une catégorie permet de regrouper des produits par type.
 * Exemples : Électronique, Vêtements, Livres, Alimentation, etc.
 *
 * 🔗 RELATION :
 * - Une catégorie a plusieurs produits (OneToMany)
 * - Un produit appartient à une seule catégorie (ManyToOne)
 *
 * 🔐 PERMISSIONS ASSOCIÉES :
 * - CATEGORY_READ   : Consulter les catégories
 * - CATEGORY_CREATE : Créer une catégorie
 * - CATEGORY_UPDATE : Modifier une catégorie
 * - CATEGORY_DELETE : Supprimer une catégorie
 *
 * 📋 EXEMPLE DE DONNÉES :
 * - id: 1, name: "Électronique", description: "Appareils électroniques"
 * - id: 2, name: "Vêtements", description: "Vêtements et accessoires"
 * - id: 3, name: "Livres", description: "Livres et publications"
 */
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Category extends AbstractAuditableEntity {

    // ============================================================
    // CHAMPS PRINCIPAUX
    // ============================================================

    /**
     * Nom de la catégorie (ex: "Électronique", "Vêtements")
     * - @NotBlank : Ne peut pas être vide
     * - @Size(max = 50) : Maximum 50 caractères
     * - unique = true : Le nom doit être unique
     * - @EqualsAndHashCode.Include : Identifie la catégorie
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    /**
     * Description de la catégorie (optionnelle)
     * - @Size(max = 200) : Maximum 200 caractères
     */
    @Size(max = 200, message = "La description ne peut pas dépasser 200 caractères")
    @Column(length = 200)
    private String description;

    // ============================================================
    // RELATION : Category → Product (OneToMany)
    // ============================================================

    /**
     * Liste des produits de cette catégorie
     *
     * - mappedBy = "category" : Le côté propriétaire est Product
     * - fetch = FetchType.LAZY : Les produits ne sont chargés qu'à la demande
     * - cascade = CascadeType.ALL : Si on supprime la catégorie, tous les produits sont supprimés
     *
     * ⚠️ @ToString.Exclude : Évite la boucle infinie
     * ⚠️ @EqualsAndHashCode.Exclude : Évite la boucle infinie
     */
    @OneToMany(mappedBy = "category",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Product> products = new ArrayList<>();

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Ajoute un produit à la catégorie
     * ⚠️ Maintient la cohérence des deux côtés de la relation
     */
    public void addProduct(Product product) {
        this.products.add(product);
        product.setCategory(this);
    }

    /**
     * Retire un produit de la catégorie
     * ⚠️ Maintient la cohérence des deux côtés de la relation
     */
    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCategory(null);
    }
}