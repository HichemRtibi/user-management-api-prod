package com.formation.usermanagement.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RepositoryMetricsAspect {

    private final MeterRegistry meterRegistry;

    // ⚠️ Pointcut spécifique pour les repositories
    @Around("execution(* com.formation.usermanagement.repository.*.*(..))")
    public Object trackRepository(ProceedingJoinPoint joinPoint) throws Throwable {

        // Récupérer le nom de la classe (sans le package)
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // Récupérer le nom de la méthode
        String methodName = joinPoint.getSignature().getName();

        // ⚠️ NOM DIFFÉRENT : "repository." au lieu de "method."
        String metricName = "repository." + className + "." + methodName + ".duration";

        log.info("🔍 REPOSITORY INTERCEPTÉ : {}.{}", className, methodName);

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            Timer timer = Timer.builder(metricName)
                    .description("Durée d'exécution du repository " + className + "." + methodName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry);
            sample.stop(timer);

            log.debug("⏱️ {} - {} ms", metricName, timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
    }
}