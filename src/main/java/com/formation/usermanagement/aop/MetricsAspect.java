package com.formation.usermanagement.aop;

import com.formation.usermanagement.annotation.TrackMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(com.formation.usermanagement.annotation.TrackMetrics)")
    public Object trackMetrics(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. Récupérer les informations de la méthode
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TrackMetrics annotation = method.getAnnotation(TrackMetrics.class);

        // 2. Construire le nom de la métrique
        String methodName = method.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String metricName = "method." + className + "." + methodName + ".duration";

        // 3. Démarrer le chronomètre
        Timer.Sample sample = Timer.start(meterRegistry);

        // 4. Exécuter la méthode
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            // 5. Arrêter le chronomètre et enregistrer
            Timer timer = Timer.builder(metricName)
                    .description("Durée d'exécution de " + className + "." + methodName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry);
            sample.stop(timer);

            log.debug("⏱️ {} - {} ms", metricName, timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
    }
}