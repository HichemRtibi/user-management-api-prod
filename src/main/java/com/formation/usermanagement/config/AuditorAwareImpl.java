package com.formation.usermanagement.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * IMPÉRATIF : Ce bean permet à Spring de remplir automatiquement
 * les champs @CreatedBy et @LastModifiedBy dans nos entités.
 *
 * Fonctionnement :
 * - Si un utilisateur est connecté, on prend son email (username).
 * - Si personne n'est connecté (ex: tâche batch, import), on met "SYSTEM".
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Récupère l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Cas 1 : Pas d'utilisateur connecté → on met "SYSTEM"
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        // Cas 2 : Utilisateur connecté → on retourne son email
        return Optional.of(authentication.getName());
    }
}