package com.formation.usermanagement.controller;

import com.formation.usermanagement.service.RapportService3;
import com.formation.usermanagement.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
 *
 * 1. Rapports standards :
 *    - GET /api/reports/statistics  → Rapport de statistiques générales
 *    - GET /api/reports/sales       → Rapport des ventes
 *    - GET /api/reports/users       → Rapport des utilisateurs
 *    - GET /api/reports/products    → Catalogue des produits
 *
 * 2. Rapports avancés :
 *    - GET /api/reports/export?type=xxx  → Export personnalisé
 *
 * 🔐 SÉCURITÉ : REPORT_READ requis pour tous les endpoints
 */
@RestController
@RequestMapping("/api/report_test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Rapports", description = "API pour générer et exporter des rapports")
public class RapportControllerTest {

    private final RapportService3 reportService;

    // ============================================================
    // 1. RAPPORT STATISTIQUES
    // ============================================================

    /**
     * GET /api/reports/statistics
     *
     * Télécharge un rapport PDF avec les statistiques générales
     * (utilisateurs, produits, stock, etc.)
     *
     * Exemple de réponse : Fichier PDF
     */
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Télécharger le rapport de statistiques générales",
            description = "Génère un rapport PDF contenant les statistiques des utilisateurs et produits"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé (REPORT_READ requis)")
    })
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> downloadStatisticsReport() {
        try {
            log.info("📊 Demande de téléchargement du rapport de statistiques");
            byte[] pdfBytes = reportService.generateStatisticsReport();
            return buildResponse(pdfBytes, "rapport-statistiques");
        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }

    // ============================================================
    // 2. RAPPORT VENTES
    // ============================================================

    /**
     * GET /api/reports/sales
     *
     * Télécharge un rapport PDF des ventes
     * (nombre de ventes, chiffre d'affaires, top produits vendus)
     */
    @GetMapping(value = "/sales", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Télécharger le rapport des ventes",
            description = "Génère un rapport PDF contenant les statistiques des ventes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> downloadSalesReport() {
        try {
            log.info("💰 Demande de téléchargement du rapport des ventes");
            byte[] pdfBytes = reportService.generateSalesReport();
            return buildResponse(pdfBytes, "rapport-ventes");
        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }

    // ============================================================
    // 3. RAPPORT UTILISATEURS
    // ============================================================

    /**
     * GET /api/reports/users
     *
     * Télécharge un rapport PDF des utilisateurs
     * (total, actifs, inactifs, jamais connectés)
     */
    @GetMapping(value = "/users", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Télécharger le rapport des utilisateurs",
            description = "Génère un rapport PDF contenant les statistiques des utilisateurs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> downloadUserReport() {
        try {
            log.info("👤 Demande de téléchargement du rapport des utilisateurs");
            byte[] pdfBytes = reportService.generateUserReport();
            return buildResponse(pdfBytes, "rapport-utilisateurs");
        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }

    // ============================================================
    // 4. CATALOGUE PRODUITS
    // ============================================================

    /**
     * GET /api/reports/products
     *
     * Télécharge un catalogue PDF de tous les produits
     * (nom, catégorie, prix, stock, description)
     */
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Télécharger le catalogue des produits",
            description = "Génère un PDF avec la liste complète de tous les produits"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalogue généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> downloadProductReport() {
        try {
            log.info("📦 Demande de téléchargement du catalogue des produits");
            byte[] pdfBytes = reportService.generateStatisticsReport();
            return buildResponse(pdfBytes, "catalogue-produits");
        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }

    // ============================================================
    // 5. EXPORT PERSONNALISÉ
    // ============================================================

    /**
     * GET /api/reports/export?type=statistics|sales|users|products
     *
     * Télécharge un rapport en fonction du type spécifié
     *
     * Exemple : GET /api/reports/export?type=sales
     */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Exporter un rapport personnalisé",
            description = "Exporte un rapport PDF en fonction du type spécifié"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "400", description = "Type de rapport invalide"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> exportReport(
            @Parameter(description = "Type de rapport (statistics, sales, users, products)",
                    example = "statistics")
            @RequestParam String type) {

        try {
            log.info("📊 Demande d'export du rapport : {}", type);

            byte[] pdfBytes;
            String filename;

            switch (type.toLowerCase()) {
                case "statistics":
                    pdfBytes = reportService.generateStatisticsReport();
                    filename = "rapport-statistiques";
                    break;
                case "sales":
                    pdfBytes = reportService.generateSalesReport();
                    filename = "rapport-ventes";
                    break;
                case "users":
                    pdfBytes = reportService.generateUserReport();
                    filename = "rapport-utilisateurs";
                    break;
                case "products":
                    pdfBytes = reportService.generateStatisticsReport();
                    filename = "catalogue-produits";
                    break;
                default:
                    throw new IllegalArgumentException("Type de rapport invalide : " + type);
            }

            return buildResponse(pdfBytes, filename);

        } catch (IllegalArgumentException e) {
            log.error("❌ Type invalide : {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport", e);
        }
    }

    // ============================================================
    // MÉTHODE UTILITAIRE
    // ============================================================

    /**
     * Construit la réponse HTTP pour le téléchargement du PDF
     */
    private ResponseEntity<byte[]> buildResponse(byte[] pdfBytes, String baseName) {
        // Ajouter la date au nom du fichier
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String filename = baseName + "_" + date + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}