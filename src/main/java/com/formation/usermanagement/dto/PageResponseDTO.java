package com.formation.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO GÉNÉRIQUE POUR LA PAGINATION
 *
 * Utilisé pour toutes les réponses paginées.
 *
 * @param <T> Le type des éléments (UtilisateurResponseDTO, RoleResponseDTO, etc.)
 *
 * Exemple de réponse JSON :
 * {
 *   "content": [...],
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    /**
     * Les données de la page courante
     */
    private List<T> content;

    /**
     * Nombre total d'éléments (dans toute la base, pas seulement la page)
     */
    private long totalElements;

    /**
     * Nombre total de pages
     */
    private int totalPages;

    /**
     * Taille de la page (nombre d'éléments par page)
     */
    private int size;

    /**
     * Numéro de la page courante (commence à 0)
     */
    private int number;

    /**
     * Nombre d'éléments dans la page courante
     */
    private int numberOfElements;

    /**
     * Est-ce la première page ?
     */
    private boolean first;

    /**
     * Est-ce la dernière page ?
     */
    private boolean last;

    /**
     * Est-ce que la page est vide ?
     */
    private boolean empty;

    /**
     * ⚠️ UNIQUE CONSTRUCTEUR UTILE
     *
     * Construit un PageResponseDTO à partir d'une Page Spring.
     * Tous les champs sont remplis automatiquement.
     *
     * @param page La page Spring Data
     */
    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.numberOfElements = page.getNumberOfElements();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.empty = page.isEmpty();
    }
}