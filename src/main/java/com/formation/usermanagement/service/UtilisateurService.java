package com.formation.usermanagement.service;

import com.formation.usermanagement.dto.PageResponseDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import org.springframework.data.domain.Pageable;

/**
 * SERVICE UTILISATEUR
 *
 * Contient toute la logique métier pour la gestion des utilisateurs.
 *
 * Méthodes disponibles :
 * - CRUD complet (créer, lire, mettre à jour, supprimer)
 * - Gestion des états (activer, désactiver, verrouiller, etc.)
 * - Gestion des rôles et permissions
 * - Recherche et pagination
 */
public interface UtilisateurService {

    // ============================================================
    // 1. CRUD
    // ============================================================

    /**
     * Crée un nouvel utilisateur
     *
     * @param dto Les données de l'utilisateur
     * @return L'utilisateur créé
     * @throws // EmailDejaExistantException si l'email est déjà utilisé
     */
    UtilisateurResponseDTO creerUtilisateur(UtilisateurRequestDTO dto);

    /**
     * Récupère un utilisateur par son ID
     *
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé
     * @throws // UtilisateurNotFoundException si l'utilisateur n'existe pas
     */
    UtilisateurResponseDTO getUtilisateur(Long id);

    /**
     * Récupère un utilisateur par son email
     *
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé
     * @throws // UtilisateurNotFoundException si l'utilisateur n'existe pas
     */
    UtilisateurResponseDTO getUtilisateurByEmail(String email);

    /**
     * Récupère tous les utilisateurs avec pagination
     *
     * @param pageable Les paramètres de pagination
     * @return PageResponseDTO contenant les utilisateurs et métadonnées
     */
    PageResponseDTO<UtilisateurResponseDTO> getAllUtilisateurs(Pageable pageable);

    /**
     * Met à jour un utilisateur existant
     *
     * @param id L'ID de l'utilisateur
     * @param dto Les nouvelles données
     * @return L'utilisateur mis à jour
     * @throws // UtilisateurNotFoundException si l'utilisateur n'existe pas
     */
    UtilisateurResponseDTO updateUtilisateur(Long id, UtilisateurRequestDTO dto);

    /**
     * Supprime un utilisateur
     *
     * @param id L'ID de l'utilisateur
     * @throws // UtilisateurNotFoundException si l'utilisateur n'existe pas
     */
    void supprimerUtilisateur(Long id);

    // ============================================================
    // 2. GESTION DES ÉTATS
    // ============================================================

    void desactiverUtilisateur(Long id);
    void activerUtilisateur(Long id);
    void verrouillerUtilisateur(Long id);
    void deverrouillerUtilisateur(Long id);
    void expirerUtilisateur(Long id);
    void renouvelerUtilisateur(Long id);

    // ============================================================
    // 3. GESTION DES RÔLES ET PERMISSIONS
    // ============================================================

    void assignerRole(Long userId, String roleName);
    void retirerRole(Long userId, String roleName);
    void ajouterPermission(Long userId, String permissionName);
    void retirerPermission(Long userId, String permissionName);

    // ============================================================
    // 4. RECHERCHE AVANCÉE
    // ============================================================

    PageResponseDTO<UtilisateurResponseDTO> rechercherUtilisateurs(String keyword, Pageable pageable);
    PageResponseDTO<UtilisateurResponseDTO> getUtilisateursByRole(String roleName, Pageable pageable);

    // ============================================================
    // 5. VALIDATION
    // ============================================================

    boolean existeParEmail(String email);
    boolean estActif(Long id);
}