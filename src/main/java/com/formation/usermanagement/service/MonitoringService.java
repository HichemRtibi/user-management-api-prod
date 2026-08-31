package com.formation.usermanagement.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
}