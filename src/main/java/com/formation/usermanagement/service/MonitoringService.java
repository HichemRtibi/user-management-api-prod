package com.formation.usermanagement.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final MeterRegistry meterRegistry;

    // Notre premier compteur : les inscriptions
    private Counter userRegistrationCounter;
    private Counter userLoginCounter;        // ← AJOUTER
    private Counter userLoginFailedCounter;  // ← AJOUTER
    private Timer loginDurationTimer;
    private Timer userCreationDurationTimer;

    @PostConstruct
    public void init() {
        log.info("📊 Initialisation des métriques personnalisées...");

        // Création du compteur
        userRegistrationCounter = Counter.builder("user.registrations.total")
                .description("Nombre total d'inscriptions")
                .tag("service", "user-management")
                .register(meterRegistry);

        log.info("✅ Compteur user.registrations.total créé !");
        userLoginCounter = Counter.builder("user.logins.total")
                .description("Nombre total de connexions réussies")
                .tag("service", "user-management")
                .register(meterRegistry);

        userLoginFailedCounter = Counter.builder("user.logins.failed")
                .description("Nombre total de connexions échouées")
                .tag("service", "user-management")
                .register(meterRegistry);

        log.info("✅ Tous les compteurs sont initialisés !");

        // ============================================================
        // AJOUTER LE TIMER ICI
        // ============================================================
        loginDurationTimer = Timer.builder("user.login.duration")
                .description("Durée des connexions")
                .tag("service", "user-management")
                .register(meterRegistry);
        userCreationDurationTimer = Timer.builder("user.creation.duration")
                .description("Durée de création des utilisateurs")
                .tag("service", "user-management")
                .register(meterRegistry);

        log.info("✅ Tous les compteurs et timers sont initialisés !");

        log.info("✅ Tous les compteurs et timers sont initialisés !");

    }

    // Méthode pour incrémenter le compteur
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
        log.debug("📈 +1 nouvelle inscription");
    }

    public void incrementUserLogin() {
        userLoginCounter.increment();
        log.debug("📈 +1 connexion réussie");
    }

    public void incrementUserLoginFailed() {
        userLoginFailedCounter.increment();
        log.debug("📈 +1 échec de connexion");
    }

    // Démarrer le chronomètre
    public Timer.Sample startLoginTimer() {
        return Timer.start(meterRegistry);
    }

    // Arrêter le chronomètre et enregistrer la durée
    public void stopLoginTimer(Timer.Sample sample) {
        sample.stop(loginDurationTimer);
        log.debug("⏱️ Durée de connexion enregistrée");
    }
    public Timer.Sample startUserCreationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopUserCreationTimer(Timer.Sample sample) {
        sample.stop(userCreationDurationTimer);
        log.debug("⏱️ Durée de création utilisateur enregistrée");
    }
}