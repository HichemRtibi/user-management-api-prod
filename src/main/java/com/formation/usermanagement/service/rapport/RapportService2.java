package com.formation.usermanagement.service.rapport;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RapportService2 {


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

        // 1. Créer le PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
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
        document.close();
        pdfDoc.close();

        byte[] pdfBytes = outputStream.toByteArray();
        log.info("✅ PDF généré - Taille : {} octets", pdfBytes.length);

        return pdfBytes;
    }
}
