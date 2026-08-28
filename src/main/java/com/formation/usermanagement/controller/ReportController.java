package com.formation.usermanagement.controller;

import com.formation.usermanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ================================================================
 * CONTROLLER POUR LA GESTION DES RAPPORTS
 * ================================================================
 *
 * 🎯 OBJECTIF : Exposer les endpoints pour télécharger les rapports
 *
 * 📋 ENDPOINTS DISPONIBLES :
 * - GET /api/reports/statistics → Télécharger le rapport de statistiques
 *
 * 🔐 SÉCURITÉ : REPORT_READ requis
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Rapports", description = "API pour générer et exporter des rapports")
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/statistics
     *
     * 🎯 OBJECTIF : Télécharger un rapport PDF avec les statistiques de l'application
     *
     * 📋 EXEMPLE DE RÉPONSE :
     * - Status 200 OK
     * - Content-Type: application/pdf
     * - Content-Disposition: attachment; filename=rapport-statistiques_2026-08-26.pdf
     * - Body: (fichier PDF)
     *
     * 🔐 PERMISSION REQUISE : REPORT_READ
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Télécharger un rapport de statistiques (PDF)")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> downloadStatisticsReport() {
        try {
            log.info("📊 Demande de téléchargement du rapport de statistiques");

            // 1. Générer le PDF
            byte[] pdfBytes = reportService.generateStatisticsReport();

            // 2. Créer le nom du fichier avec la date
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filename = "rapport-statistiques_" + date + ".pdf";

            // 3. Retourner la réponse
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du rapport : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }
}