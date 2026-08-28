package com.formation.usermanagement.service;

import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.repository.ProductRepository;
import com.formation.usermanagement.repository.UtilisateurRepository;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProductRepository productRepository;

    /**
     * Génère un rapport PDF avec les statistiques de l'application
     */
    public byte[] generateStatisticsReport() throws Exception {
        log.info("📊 Génération du rapport de statistiques");

        // ============================================================
        // 1. RÉCUPÉRER LES DONNÉES
        // ============================================================

        long totalUsers = utilisateurRepository.count();
        long activeUsers = utilisateurRepository.countUtilisateursActifs();
        long totalProducts = productRepository.count();
        long productsInStock = productRepository.countInStock();
        double averagePrice = productRepository.getAveragePrice() != null
                ? productRepository.getAveragePrice() : 0.0;
        BigDecimal totalStockValue = productRepository.getTotalStockValue() != null
                ? productRepository.getTotalStockValue() : BigDecimal.ZERO;

        List<Product> topProducts = productRepository.findTop10MostStocked();

        // ============================================================
        // 2. PRÉPARER LES DONNÉES
        // ============================================================

        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String activityRate = totalUsers > 0
                ? String.format("%.1f", (double) activeUsers / totalUsers * 100)
                : "0";

        // ============================================================
        // 3. CRÉER LE PDF
        // ============================================================

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

        // ============================================================
        // 4. TITRE
        // ============================================================

        Paragraph title = new Paragraph("RAPPORT DE STATISTIQUES")
                .setFont(boldFont)
                .setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(title);

        document.add(new Paragraph("═══════════════════════════════════════════════════════════════")
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15));

        // ============================================================
        // 5. DATE
        // ============================================================

        document.add(new Paragraph("Date : " + date)
                .setFont(regularFont)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        // ============================================================
        // 6. SECTION 1 : STATISTIQUES GÉNÉRALES
        // ============================================================

        document.add(new Paragraph("1. STATISTIQUES GÉNÉRALES")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginBottom(10));

        Table statsTable = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        Cell header1 = new Cell().add(new Paragraph("Métrique").setFont(boldFont));
        header1.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        header1.setTextAlignment(TextAlignment.CENTER);
        header1.setPadding(5);
        statsTable.addCell(header1);

        Cell header2 = new Cell().add(new Paragraph("Valeur").setFont(boldFont));
        header2.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        header2.setTextAlignment(TextAlignment.CENTER);
        header2.setPadding(5);
        statsTable.addCell(header2);

        // Lignes
        addStatRow(statsTable, "Total utilisateurs", String.valueOf(totalUsers));
        addStatRow(statsTable, "Utilisateurs actifs", String.valueOf(activeUsers));
        addStatRow(statsTable, "Taux d'activité", activityRate + " %");
        addStatRow(statsTable, "Total produits", String.valueOf(totalProducts));
        addStatRow(statsTable, "Produits en stock", String.valueOf(productsInStock));
        addStatRow(statsTable, "Prix moyen", String.format("%.2f €", averagePrice));
        addStatRow(statsTable, "Valeur totale du stock", String.format("%.2f €", totalStockValue));

        document.add(statsTable);
        document.add(new Paragraph(" "));

        // ============================================================
        // 7. SECTION 2 : TOP PRODUITS
        // ============================================================

        int topCount = Math.min(topProducts.size(), 10);
        document.add(new Paragraph("2. TOP " + topCount + " PRODUITS")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(15)
                .setMarginBottom(10));

        Table productTable = new Table(UnitValue.createPercentArray(new float[]{8, 37, 20, 17, 18}))
                .setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        String[] headers = {"N°", "Produit", "Catégorie", "Prix", "Quantité"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(boldFont));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(5);
            productTable.addCell(cell);
        }

        // Lignes
        int index = 1;
        boolean alternate = false;

        // Éliminer les doublons en utilisant un Set
        java.util.Set<String> seenProducts = new java.util.HashSet<>();

        for (Product product : topProducts) {
            String key = product.getName() + product.getPrice().toString();
            if (seenProducts.contains(key)) {
                continue; // Ignorer les doublons
            }
            seenProducts.add(key);

            String categoryName = product.getCategory() != null
                    ? product.getCategory().getName() : "N/A";

            Color bgColor = alternate ? ColorConstants.WHITE : new com.itextpdf.kernel.colors.DeviceRgb(240, 240, 240);
            alternate = !alternate;

            // N°
            Cell cell1 = new Cell().add(new Paragraph(String.valueOf(index++)));
            cell1.setBackgroundColor(bgColor);
            cell1.setTextAlignment(TextAlignment.CENTER);
            cell1.setPadding(4);
            productTable.addCell(cell1);

            // Produit
            Cell cell2 = new Cell().add(new Paragraph(product.getName()));
            cell2.setBackgroundColor(bgColor);
            cell2.setPadding(4);
            productTable.addCell(cell2);

            // Catégorie
            Cell cell3 = new Cell().add(new Paragraph(categoryName));
            cell3.setBackgroundColor(bgColor);
            cell3.setPadding(4);
            productTable.addCell(cell3);

            // Prix
            Cell cell4 = new Cell().add(new Paragraph(String.format("%.2f €", product.getPrice())));
            cell4.setBackgroundColor(bgColor);
            cell4.setTextAlignment(TextAlignment.RIGHT);
            cell4.setPadding(4);
            productTable.addCell(cell4);

            // Quantité
            Cell cell5 = new Cell().add(new Paragraph(String.valueOf(product.getQuantity())));
            cell5.setBackgroundColor(bgColor);
            cell5.setTextAlignment(TextAlignment.RIGHT);
            cell5.setPadding(4);
            productTable.addCell(cell5);
        }

        document.add(productTable);

        // ============================================================
        // 8. PIED DE PAGE
        // ============================================================

        document.add(new Paragraph(" "));
        document.add(new Paragraph("═══════════════════════════════════════════════════════════════")
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Fin du rapport - " + date)
                .setFont(regularFont)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

        // ============================================================
        // 9. FERMER LE DOCUMENT
        // ============================================================

        document.close();
        pdfDoc.close();

        byte[] pdfBytes = outputStream.toByteArray();
        log.info("✅ Rapport PDF généré avec succès - Taille : {} octets", pdfBytes.length);

        return pdfBytes;
    }

    /**
     * Ajoute une ligne au tableau des statistiques
     */
    private void addStatRow(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        Cell valueCell = new Cell().add(new Paragraph(value));
        valueCell.setTextAlignment(TextAlignment.RIGHT);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}