package com.formation.usermanagement.service.rapport;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
@Service
@RequiredArgsConstructor
@Slf4j
public class RapportService2 {
    public byte[] generateStatisticsReport() throws Exception {
        log.info("📊 Génération du rapport PDF");

        // 1. Créer le PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // 2. Fermer le document (pour l'instant, PDF vide)
        document.close();
        pdfDoc.close();

        byte[] pdfBytes = outputStream.toByteArray();
        log.info("✅ PDF généré - Taille : {} octets", pdfBytes.length);

        return pdfBytes;
    }
}
