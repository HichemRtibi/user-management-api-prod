package com.formation.usermanagement.service.rapport;

import com.formation.usermanagement.entity.Product;
import com.formation.usermanagement.repository.ProductRepository;
import com.formation.usermanagement.repository.UtilisateurRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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
public class RapportService2 {

    private final UtilisateurRepository utilisateurRepository1;
    private final ProductRepository productRepository1;



    /*
    1. Chercher sur Google :
   "itext [ce que je veux faire] example java"

2. Aller sur Stack Overflow :
   "itext pdf [fonctionnalité]"

3. Regarder les exemples officiels :
   https://github.com/itext/itext-publications-examples-java

4. Lire la documentation API :
   https://itextpdf.com/resources/api-documentation/itext-7-java


   ✅ RÉSUMÉ
Ce que vous voulez faire	Recherche Google
Ajouter un titre	itext pdf add title example
Ajouter un tableau	itext pdf table example java
Ajouter des accents	itext pdf utf-8 french
Remplir un formulaire	itext pdf fill form fields
Ajouter une image	itext pdf add image
     */

    /*
    PdfWriter writer = new PdfWriter("output.pdf");
PdfDocument pdfDoc = new PdfDocument(writer);
Document document = new Document(pdfDoc);
document.add(new Paragraph("Hello, World!"));
document.close();

Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
table.addCell("Colonne 1");
table.addCell("Colonne 2");
table.addCell("Valeur A");
table.addCell("Valeur B");
document.add(table);

PdfFont font = PdfFontFactory.createFont("Helvetica");
// OU pour les caractères spéciaux :
FontProvider fontProvider = new FontProvider();
fontProvider.addFont("path/to/font.ttf");

Paragraph paragraph = new Paragraph("Texte avec bordure");
paragraph.setBorder(new SolidBorder(1));
document.add(paragraph);


PdfReader reader = new PdfReader("template.pdf");
PdfStamper stamper = new PdfStamper(reader, outputStream);
AcroFields form = stamper.getAcroFields();
form.setField("nom", "Dupont");
form.setField("prenom", "Jean");
stamper.close();
     */
    public byte[] generateStatisticsReport() throws Exception {
        log.info("📊 Génération du rapport PDF");
        long totalUsers = utilisateurRepository1.count();
        long activeUsers = utilisateurRepository1.countUtilisateursActifs();
        long totalProducts = productRepository1.count();
        long productsInStock = productRepository1.countInStock();
        double averagePrice = productRepository1.getAveragePrice() != null
                ? productRepository1.getAveragePrice() : 0.0;
        BigDecimal totalStockValue = productRepository1.getTotalStockValue() != null
                ? productRepository1.getTotalStockValue() : BigDecimal.ZERO;

        List<Product> topProducts = productRepository1.findTop10ByOrderByQuantityDesc();



        // 1. Créer le PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        PdfFont boldFont = PdfFontFactory.createFont("Helvetica-Bold");

/*

        ajouter un titre
       */
        Paragraph title = new Paragraph("RAPPORT STATSTIQUES").
                setBold()//ajouter en gras
                .setFontSize(22).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20);
        //ajouter le titre
        document.add(title);
        //========================================================
        //3 Date
        //==============================================
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        //gerre le date
        Paragraph dateParag = new Paragraph("Date :" + date);
        dateParag.setFontSize(11).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20);
        document.add(dateParag);

        // 2. Fermer le document (pour l'instant, PDF vide)


        //=======
        //4 ajouter titre sections
        //============================

        Paragraph sectionTitle = new Paragraph("1. STATISTIQUES GÉNÉRALES")
                .setFont(boldFont)           // En gras
                .setFontSize(16)              // Taille 16
                .setMarginBottom(10);         // Marge en bas
        document.add(sectionTitle);

        // ajouter tableau ===============
        Table table = new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .setWidth(UnitValue.createPercentValue(100));
        Cell header1 = new Cell()
                .add(new Paragraph("Métrique").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(header1);

        Cell header2 = new Cell()
                .add(new Paragraph("Valeur").setFont(boldFont))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(header2);


        addStatRow(table, "Total utilisateurs",String.valueOf(totalUsers));
        addStatRow(table, "Utilisateurs actifs", String.valueOf(activeUsers));
        String activityRate = totalUsers > 0
                ? String.format("%.1f", (double) activeUsers / totalUsers * 100) + " %"
                : "0 %";
        addStatRow(table, "Taux d'activité", activityRate);

        addStatRow(table, "Total produits", String.valueOf(totalProducts));
        addStatRow(table, "Produits en stock", String.valueOf(productsInStock));
        addStatRow(table, "Prix moyen", String.format("%.2f €", averagePrice));
        addStatRow(table, "Valeur totale du stock", String.format("%.2f €", totalStockValue));

        document.add(table);
        document.add(new Paragraph(" ")); // Espace

        int topCount = Math.min(topProducts.size(), 10);
        Paragraph section2 = new Paragraph("2. TOP " + topCount + " PRODUITS")
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(15)
                .setMarginBottom(10);
        document.add(section2);

        // Tableau des produits (5 colonnes)
        Table productTable = new Table(UnitValue.createPercentArray(new float[]{10, 35, 20, 17, 18}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] headers = {"N°", "Produit", "Catégorie", "Prix", "Quantité"};
        for (String h : headers) {
            Cell cell = new Cell().add(new Paragraph(h).setFont(boldFont));
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.CENTER);
            cell.setPadding(5);
            productTable.addCell(cell);
        }
        int index = 1;
        for (Product product : topProducts) {
            String categoryName = product.getCategory() != null
                    ? product.getCategory().getName() : "N/A";

            // N°
            Cell cell1 = new Cell().add(new Paragraph(String.valueOf(index++)));
            cell1.setTextAlignment(TextAlignment.CENTER);
            productTable.addCell(cell1);

            // Produit
            productTable.addCell(new Cell().add(new Paragraph(product.getName())));

            // Catégorie
            productTable.addCell(new Cell().add(new Paragraph(categoryName)));

            // Prix
            Cell priceCell = new Cell().add(new Paragraph(String.format("%.2f €", product.getPrice())));
            priceCell.setTextAlignment(TextAlignment.RIGHT);
            productTable.addCell(priceCell);

            // Quantité
            Cell qtyCell = new Cell().add(new Paragraph(String.valueOf(product.getQuantity())));
            qtyCell.setTextAlignment(TextAlignment.RIGHT);
            productTable.addCell(qtyCell);
        }

        document.add(productTable);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("═══════════════════════════════════════════════════════════════")
                .setFont(boldFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Fin du rapport - " + date)
                .setFont(boldFont).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginTop(10));
        document.close();
        pdfDoc.close();

        byte[] pdfBytes = outputStream.toByteArray();
        log.info("✅ PDF généré - Taille : {} octets", pdfBytes.length);

        return pdfBytes;
    }




    private void addStatRow(Table table, String label, String value) {
        // Colonne 1 : Label
        Cell labelCell = new Cell()
                .add(new Paragraph(label))
                .setPadding(5);
        table.addCell(labelCell);

        // Colonne 2 : Valeur
        Cell valueCell = new Cell()
                .add(new Paragraph(value))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
        table.addCell(valueCell);
    }
}
