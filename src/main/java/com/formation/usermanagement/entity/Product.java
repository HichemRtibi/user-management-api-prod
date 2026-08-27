package com.formation.usermanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * ================================================================
 * ENTITÉ PRODUCT
 * ================================================================
 *
 * 📖 EXPLICATION MÉTIER :
 *
 * Un produit est un article vendu dans le catalogue.
 * Exemples : iPhone 15, T-shirt Blanc, Spring Boot Guide, etc.
 *
 * 🔗 RELATION :
 * - Un produit appartient à une seule catégorie (ManyToOne)
 * - Une catégorie a plusieurs produits (OneToMany)
 *
 * 🔐 PERMISSIONS ASSOCIÉES :
 * - PRODUCT_READ   : Consulter les produits
 * - PRODUCT_CREATE : Créer un produit
 * - PRODUCT_UPDATE : Modifier un produit
 * - PRODUCT_DELETE : Supprimer un produit
 *
 * 📋 EXEMPLE DE DONNÉES :
 * - id: 1, name: "iPhone 15", price: 999.99, quantity: 10, category: Électronique
 * - id: 2, name: "T-shirt Blanc", price: 19.99, quantity: 50, category: Vêtements
 * - id: 3, name: "Spring Boot Guide", price: 39.99, quantity: 100, category: Livres
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Product extends AbstractAuditableEntity {

    // ============================================================
    // CHAMPS PRINCIPAUX
    // ============================================================

    /**
     * Nom du produit (ex: "iPhone 15", "T-shirt Blanc")
     * - @NotBlank : Ne peut pas être vide
     * - @Size(max = 100) : Maximum 100 caractères
     * - @EqualsAndHashCode.Include : Identifie le produit
     */
    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Description détaillée du produit (optionnelle)
     * - @Size(max = 500) : Maximum 500 caractères
     */
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;

    /**
     * Prix unitaire du produit
     * - @NotNull : Ne peut pas être null
     * - @Positive : Doit être positif (> 0)
     * - precision = 10, scale = 2 : 10 chiffres dont 2 décimales
     */
    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Quantité en stock
     * - @NotNull : Ne peut pas être null
     * - @Positive : Doit être positive (>= 0)
     * - default = 0 : Par défaut, 0 en stock
     */
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    @Column(nullable = false)
    private Integer quantity = 0;

    /**
     * URL de l'image du produit (optionnelle)
     */
    @Column(length = 255)
    private String imageUrl;

    // ============================================================
    // RELATION : Product → Category (ManyToOne)
    // ============================================================

    /**
     * La catégorie du produit
     *
     * - fetch = FetchType.LAZY : La catégorie est chargée à la demande
     * - @JoinColumn(name = "category_id") : Clé étrangère dans products
     *
     * ⚠️ @ToString.Exclude : Évite la boucle infinie
     * ⚠️ @EqualsAndHashCode.Exclude : Évite la boucle infinie
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public Product(String name, String description, BigDecimal price, Integer quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Vérifie si le produit est en stock
     * @return true si la quantité > 0
     */
    public boolean isInStock() {
        return this.quantity > 0;
    }

    /**
     * Réduit la quantité en stock
     * @param amount Quantité à réduire
     * @throws IllegalArgumentException si quantité insuffisante
     */
    public void reduceQuantity(Integer amount) {
        if (this.quantity < amount) {
            throw new IllegalArgumentException("Quantité insuffisante en stock");
        }
        this.quantity -= amount;
    }

    /**
     * Augmente la quantité en stock
     * @param amount Quantité à ajouter
     */
    public void increaseQuantity(Integer amount) {
        this.quantity += amount;
    }
}