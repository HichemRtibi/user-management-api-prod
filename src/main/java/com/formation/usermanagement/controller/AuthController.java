package com.formation.usermanagement.controller;

import com.formation.usermanagement.dto.auth.LoginRequestDTO;
import com.formation.usermanagement.dto.auth.LoginResponseDTO;
import com.formation.usermanagement.dto.auth.RegisterRequestDTO;
import com.formation.usermanagement.dto.utilisateur.UtilisateurResponseDTO;
import com.formation.usermanagement.entity.Utilisateur;
import com.formation.usermanagement.exception.EmailDejaExistantException;
import com.formation.usermanagement.mapper.UtilisateurMapper;
import com.formation.usermanagement.repository.UtilisateurRepository;
import com.formation.usermanagement.config.security.JwtTokenProvider;
import com.formation.usermanagement.service.MonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentification", description = "API pour l'authentification des utilisateurs")

public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final MonitoringService monitoringService;  // ← AJOUTER CETTE LIGNE


    @PostMapping("/login")
    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur et retourne un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Email ou mot de passe invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("🔐 Tentative de connexion : {}", loginRequest.getEmail());
        try {


            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getMotDePasse()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = tokenProvider.generateToken(authentication);

            Utilisateur utilisateur = utilisateurRepository.findByEmailWithAllRelations(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // ✅ RECHARGER l'utilisateur depuis la base pour avoir createdAt
//        Utilisateur utilisateurReload = utilisateurRepository.findById(utilisateur.getId())
//                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            UtilisateurResponseDTO utilisateurDTO = UtilisateurMapper.toResponseDTO(utilisateur);

            log.info("✅ Connexion réussie pour : {}", loginRequest.getEmail());

            return ResponseEntity.ok(new LoginResponseDTO(token, utilisateurDTO));
        } catch (AuthenticationException e) {
            monitoringService.incrementUserLoginFailed();

            log.warn("❌ Échec de connexion : {}", loginRequest.getEmail());
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("📝 Inscription : {}", registerRequest.getEmail());

        // ✅ Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("❌ Email déjà utilisé : {}", registerRequest.getEmail());
            throw new EmailDejaExistantException(registerRequest.getEmail());
        }

        // Créer l'utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPrenom(registerRequest.getPrenom());
        utilisateur.setNom(registerRequest.getNom());
        utilisateur.setEmail(registerRequest.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(registerRequest.getMotDePasse()));
        utilisateur.setEnabled(true);
        utilisateur.setCompteNonVerrouille(true);
        utilisateur.setCompteNonExpire(true);
        utilisateur.setCredentialsNonExpire(true);

        // Sauvegarder
        Utilisateur saved = utilisateurRepository.save(utilisateur);
        log.info("✅ Utilisateur créé : {}", registerRequest.getEmail());

        monitoringService.incrementUserRegistration();

        // Récupérer l'utilisateur pour avoir createdAt
        Utilisateur utilisateurReload = utilisateurRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Authentifier
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getMotDePasse()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        UtilisateurResponseDTO utilisateurDTO = UtilisateurMapper.toResponseDTO(utilisateurReload);

        return ResponseEntity.ok(new LoginResponseDTO(token, utilisateurDTO));
    }
}