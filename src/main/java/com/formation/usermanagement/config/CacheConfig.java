package com.formation.usermanagement.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * ================================================================
 * CONFIGURATION DU CACHE AVEC CAFFEINE
 * ================================================================
 *
 * 📖 POURQUOI CE FICHIER ?
 *
 * Ce fichier dit à Spring : "Utilise Caffeine comme système de cache".
 *
 * 📋 CE QU'ON CONFIGURE :
 * 1. Activer le cache (@EnableCaching)
 * 2. Créer le gestionnaire de cache (CacheManager)
 * 3. Paramétrer Caffeine (durée de vie, taille max)
 *
 * 🔧 PARAMÈTRES :
 * - expireAfterWrite : Les données expirent après X minutes
 * - maximumSize : Nombre maximum d'éléments dans le cache
 * - recordStats : Activer les statistiques (pour le monitoring)
 */
@Configuration
@EnableCaching  // ← ACTIVER LE CACHE : Sans ça, @Cacheable ne fonctionne pas !
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheCaffeineManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // ⚙️ Configuration de Caffeine
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // Les données restent 10 minutes dans le cache
                .expireAfterWrite(10, TimeUnit.MINUTES)

                // Maximum 1000 entrées dans le cache
                .maximumSize(1000)

                // Activer les statistiques (hits, misses, etc.)
                .recordStats()
        );

        // ✅ Retourner le gestionnaire de cache configuré
        return cacheManager;
    }
}