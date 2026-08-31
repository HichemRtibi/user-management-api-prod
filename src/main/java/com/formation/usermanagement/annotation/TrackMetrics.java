package com.formation.usermanagement.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})      // S'applique UNIQUEMENT aux méthodes
@Retention(RetentionPolicy.RUNTIME) // Disponible à l'exécution (nécessaire pour AOP)
@Documented
public @interface TrackMetrics {
    // Nom personnalisé pour la métrique (optionnel)
    String value() default "";

    // Mesurer le temps ou non (par défaut oui)
    boolean recordTime() default true;
    /*
    @Target({ElementType.METHOD})	Cette annotation ne peut être mise que sur des méthodes
@Retention(RetentionPolicy.RUNTIME)	L'annotation est disponible à l'exécution (pour AOP)
@Documented
     */
}
