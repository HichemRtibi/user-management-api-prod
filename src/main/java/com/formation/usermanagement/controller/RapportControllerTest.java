package com.formation.usermanagement.controller;


import com.formation.usermanagement.service.rapport.RapportService2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports_test")
@RequiredArgsConstructor
@Slf4j
public class RapportControllerTest {
    private final RapportService2 reportService;

    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadStatisticsReport() {
        try {
            byte[] pdfBytes = reportService.generateStatisticsReport();

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            String filename = "rapport-test_" + date + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            log.error("❌ Erreur : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur", e);
        }
    }

}
