package com.formation.usermanagement.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseDTOTest {

    @Test
    void constructeur_DevraitCreerUnPageResponseDTOCorrect() {
        // GIVEN - Création d'une Page Spring
        List<String> data = List.of("A", "B", "C", "D", "E");
        Pageable pageable = PageRequest.of(0, 3);
        Page<String> page = new PageImpl<>(data.subList(0, 3), pageable, data.size());

        // WHEN - Création du DTO avec le constructeur
        PageResponseDTO<String> response = new PageResponseDTO<>(page);

        // THEN - Vérification
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent()).contains("A", "B", "C");
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getSize()).isEqualTo(3);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getNumberOfElements()).isEqualTo(3);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    void constructeur_DevraitFonctionnerAvecPageVide() {
        // GIVEN - Page vide
        Pageable pageable = PageRequest.of(0, 10);
        Page<String> page = new PageImpl<>(List.of(), pageable, 0);

        // WHEN
        PageResponseDTO<String> response = new PageResponseDTO<>(page);

        // THEN
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.isEmpty()).isTrue();
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void constructeur_DevraitFonctionnerAvecPageSuivante() {
        // GIVEN - Page 1 (la deuxième page)
        List<String> data = List.of("A", "B", "C", "D", "E");
        Pageable pageable = PageRequest.of(1, 2);  // Page 1, taille 2
        Page<String> page = new PageImpl<>(data.subList(2, 4), pageable, data.size());

        // WHEN
        PageResponseDTO<String> response = new PageResponseDTO<>(page);

        // THEN
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent()).contains("C", "D");
        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isFalse();
    }
}