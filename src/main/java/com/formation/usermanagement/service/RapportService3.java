package com.formation.usermanagement.service;

import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.entity.Utilisateur;
import com.formation.usermanagement.repository.ProductRepository;
import com.formation.usermanagement.repository.UtilisateurRepository;
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
public class RapportService3 {

    private final UtilisateurRepository utilisateurRepository;
    private final ProductRepository productRepository;

    // ============================================================
    // MÉTHODE COMMUNE : Créer un document PDF
    // ============================================================

    private Document createDocument(PdfDocument pdfDoc) {
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(50, 50, 50, 50);
        return document;
    }

    // ============================================================
    // MÉTHODE COMMUNE : Ajouter le titre et la date
    // ============================================================

    private void addHeader(Document document, String title, String date) throws Exception {
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

        // Titre
        document.add(new Paragraph(title)
                .setFont(boldFont).setFontSize(22)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(5));

        // Date
        document.add(new Paragraph("Date : " + date)
                .setFont(regularFont).setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));
    }

    // ============================================================
    // MÉTHODE COMMUNE : Ajouter le pied de page
    // ============================================================

    private void addFooter(Document document, String date) throws Exception {
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

        document.add(new Paragraph(" "));
        document.add(new Paragraph("═══════════════════════════════════════════════════════════════")
                .setFont(regularFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Fin du rapport - " + date)
                .setFont(regularFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));
    }

    // ============================================================
    // 1. RAPPORT STATISTIQUES
    // ============================================================

    public byte[] generateStatisticsReport() throws Exception {
        log.info("📊 Génération du rapport de statistiques");

        // Données
        long totalUsers = utilisateurRepository.count();
        long activeUsers = utilisateurRepository.countUtilisateursActifs();
        long totalProducts = productRepository.count();
        long productsInStock = productRepository.countInStock();
        double averagePrice = productRepository.getAveragePrice() != null
                ? productRepository.getAveragePrice() : 0.0;
        BigDecimal totalStockValue = productRepository.getTotalStockValue() != null
                ? productRepository.getTotalStockValue() : BigDecimal.ZERO;

        List<Product> topProducts = productRepository.findTop10ByOrderByQuantityDesc();

        // Créer le PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = createDocument(pdfDoc);

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Header
        addHeader(document, "RAPPORT DE STATISTIQUES", date);

        // Section 1 : Statistiques
        document.add(new Paragraph("1. STATISTIQUES GÉNÉRALES")
                .setFont(boldFont).setFontSize(16).setMarginBottom(10));

        Table statsTable = createStatsTable();
        addStatRow(statsTable, "Total utilisateurs", String.valueOf(totalUsers));
        addStatRow(statsTable, "Utilisateurs actifs", String.valueOf(activeUsers));
        addStatRow(statsTable, "Taux d'activité",
                totalUsers > 0 ? String.format("%.1f %%", (double) activeUsers / totalUsers * 100) : "0 %");
        addStatRow(statsTable, "Total produits", String.valueOf(totalProducts));
        addStatRow(statsTable, "Produits en stock", String.valueOf(productsInStock));
        addStatRow(statsTable, "Prix moyen", String.format("%.2f €", averagePrice));
        addStatRow(statsTable, "Valeur totale du stock", String.format("%.2f €", totalStockValue));
        document.add(statsTable);

        document.add(new Paragraph(" "));

        // Section 2 : Top produits
        document.add(new Paragraph("2. TOP 10 PRODUITS")
                .setFont(boldFont).setFontSize(16).setMarginTop(15).setMarginBottom(10));
        document.add(createProductTable(topProducts));

        // Footer
        addFooter(document, date);

        document.close();
        pdfDoc.close();

        return outputStream.toByteArray();
    }

    // ============================================================
    // 2. RAPPORT VENTES
    // ============================================================

    public byte[] generateSalesReport() throws Exception {
        log.info("💰 Génération du rapport des ventes");

        // Données (simulées pour l'exemple)
        long totalSales = 150L;
        BigDecimal totalRevenue = new BigDecimal("12500.50");
        BigDecimal averageBasket = totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, java.math.RoundingMode.HALF_UP);
        List<Product> topProducts = productRepository.findTop10ByOrderByQuantityDesc();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = createDocument(pdfDoc);

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        addHeader(document, "RAPPORT DES VENTES", date);

        document.add(new Paragraph("1. RÉSUMÉ DES VENTES")
                .setFont(boldFont).setFontSize(16).setMarginBottom(10));

        Table statsTable = createStatsTable();
        addStatRow(statsTable, "Nombre total de ventes", String.valueOf(totalSales));
        addStatRow(statsTable, "Chiffre d'affaires total", String.format("%.2f €", totalRevenue));
        addStatRow(statsTable, "Panier moyen", String.format("%.2f €", averageBasket));
        document.add(statsTable);

        document.add(new Paragraph(" "));

        document.add(new Paragraph("2. TOP 10 PRODUITS VENDUS")
                .setFont(boldFont).setFontSize(16).setMarginTop(15).setMarginBottom(10));

        Table salesTable = new Table(UnitValue.createPercentArray(new float[]{10, 45, 20, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        String[] headers = {"N°", "Produit", "Quantité vendue", "CA"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(boldFont));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(5);
            salesTable.addCell(cell);
        }

        int index = 1;
        for (Product product : topProducts) {
            int qtySold = product.getQuantity() * 2;
            double revenue = product.getPrice().doubleValue() * qtySold;
            salesTable.addCell(new Cell().add(new Paragraph(String.valueOf(index++)))
                    .setTextAlignment(TextAlignment.CENTER));
            salesTable.addCell(new Cell().add(new Paragraph(product.getName())));
            salesTable.addCell(new Cell().add(new Paragraph(String.valueOf(qtySold)))
                    .setTextAlignment(TextAlignment.RIGHT));
            salesTable.addCell(new Cell().add(new Paragraph(String.format("%.2f €", revenue)))
                    .setTextAlignment(TextAlignment.RIGHT));
        }
        document.add(salesTable);

        addFooter(document, date);

        document.close();
        pdfDoc.close();

        return outputStream.toByteArray();
    }

    // ============================================================
    // 3. RAPPORT UTILISATEURS
    // ============================================================

    public byte[] generateUserReport() throws Exception {
        log.info("👤 Génération du rapport des utilisateurs");

        long totalUsers = utilisateurRepository.count();
        long activeUsers = utilisateurRepository.countUtilisateursActifs();
        long neverConnected = utilisateurRepository.findUtilisateursJamaisConnectes().size();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = createDocument(pdfDoc);

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        addHeader(document, "RAPPORT DES UTILISATEURS", date);

        document.add(new Paragraph("1. STATISTIQUES UTILISATEURS")
                .setFont(boldFont).setFontSize(16).setMarginBottom(10));

        Table statsTable = createStatsTable();
        addStatRow(statsTable, "Total utilisateurs", String.valueOf(totalUsers));
        addStatRow(statsTable, "Utilisateurs actifs", String.valueOf(activeUsers));
        addStatRow(statsTable, "Utilisateurs inactifs", String.valueOf(totalUsers - activeUsers));
        addStatRow(statsTable, "Taux d'activité",
                totalUsers > 0 ? String.format("%.1f %%", (double) activeUsers / totalUsers * 100) : "0 %");
        addStatRow(statsTable, "Jamais connectés", String.valueOf(neverConnected));
        document.add(statsTable);

        document.add(new Paragraph(" "));

        document.add(new Paragraph("2. DERNIERS UTILISATEURS CONNECTÉS")
                .setFont(boldFont).setFontSize(16).setMarginTop(15).setMarginBottom(10));

        Table userTable = new Table(UnitValue.createPercentArray(new float[]{10, 45, 45}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"N°", "Email", "Nom"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(boldFont));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(5);
            userTable.addCell(cell);
        }

        List<Utilisateur> latestUsers = utilisateurRepository.findDerniersConnectes();
        int index = 1;
        for (Utilisateur user : latestUsers.stream().limit(10).toList()) {
            userTable.addCell(new Cell().add(new Paragraph(String.valueOf(index++)))
                    .setTextAlignment(TextAlignment.CENTER));
            userTable.addCell(new Cell().add(new Paragraph(user.getEmail())));
            userTable.addCell(new Cell().add(new Paragraph(user.getNomComplet())));
        }
        document.add(userTable);

        addFooter(document, date);

        document.close();
        pdfDoc.close();

        return outputStream.toByteArray();
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    private Table createStatsTable() {
        return new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private void addStatRow(Table table, String label, String value) throws Exception {
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");
        table.addCell(new Cell().add(new Paragraph(label).setFont(regularFont)).setPadding(5));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regularFont))
                .setTextAlignment(TextAlignment.RIGHT).setPadding(5));
    }

    private Table createProductTable(List<Product> products) throws Exception {
        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont regularFont = PdfFontFactory.createFont("Helvetica");

        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 35, 20, 17, 18}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"N°", "Produit", "Catégorie", "Prix", "Quantité"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(boldFont));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        int index = 1;
        for (Product product : products) {
            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "N/A";

            table.addCell(new Cell().add(new Paragraph(String.valueOf(index++)).setFont(regularFont))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph(product.getName()).setFont(regularFont)));
            table.addCell(new Cell().add(new Paragraph(categoryName).setFont(regularFont)));
            table.addCell(new Cell().add(new Paragraph(String.format("%.2f €", product.getPrice())).setFont(regularFont))
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getQuantity())).setFont(regularFont))
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        return table;
    }
}