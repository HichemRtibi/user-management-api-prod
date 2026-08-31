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

    @PostConstruct
    public void init() {
        log.info("📊 Initialisation des métriques personnalisées...");

        // Création du compteur
        userRegistrationCounter = Counter.builder("user.registrations.total")
                .description("Nombre total d'inscriptions")
                .tag("service", "user-management")
                .register(meterRegistry);

        log.info("✅ Compteur user.registrations.total créé !");
    }

    // Méthode pour incrémenter le compteur
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
        log.debug("📈 +1 nouvelle inscription");
    }
}