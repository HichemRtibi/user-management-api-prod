package com.formation.usermanagement.config.security;

import com.formation.usermanagement.entity.Utilisateur;
import com.formation.usermanagement.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 Chargement de l'utilisateur : {}", email);

        // 1. Chercher l'utilisateur par email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ Utilisateur non trouvé : {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'email : " + email);
                });

        log.info("✅ Utilisateur trouvé : {}", utilisateur.getEmail());

        // 2. Récupérer les rôles et permissions
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // Ajouter les rôles (avec préfixe ROLE_)
        utilisateur.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            // Ajouter les permissions
            role.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });

        log.info("🔑 Autorités : {}", authorities);

        // 3. Construire l'objet UserDetails
        return new User(
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                utilisateur.isEnabled(),
                utilisateur.isCompteNonExpire(),
                utilisateur.isCredentialsNonExpire(),
                utilisateur.isCompteNonVerrouille(),
                authorities
        );
    }
}