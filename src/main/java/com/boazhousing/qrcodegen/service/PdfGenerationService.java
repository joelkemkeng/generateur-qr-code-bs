package com.boazhousing.qrcodegen.service;

import com.boazhousing.qrcodegen.model.QrGenerationResult;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerationService.class);

    @Autowired
    private BoazQrCodeService qrCodeService;

    @Value("${app.qr.output-directory:document-qr-code-generer}")
    private String outputDirectory;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Génère un PDF avec titre de référence et QR code Boaz-Housing
     */
    public QrGenerationResult generateQrCodePdf(String reference) throws Exception {
        try {
            logger.info("Génération PDF pour référence: {}", reference);

            // Créer le répertoire de sortie s'il n'existe pas
            ensureOutputDirectoryExists();

            // Générer le QR code avec style Boaz-Housing
            String qrCodeBase64 = qrCodeService.generateBoazStyleQrCode(reference);

            // Créer le nom de fichier avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("QR_%s_%s.pdf", reference, timestamp);
            String filePath = Paths.get(outputDirectory, fileName).toString();

            // Générer le PDF
            createPdfWithQrCode(reference, qrCodeBase64, filePath);

            // Calculer la taille du fichier
            long fileSize = Files.size(Paths.get(filePath));

            // Créer l'URL pour téléchargement
            String pdfUrl = String.format("http://localhost:%s/download/%s", serverPort, fileName);

            QrGenerationResult result = new QrGenerationResult(reference, pdfUrl, fileName, filePath, fileSize);

            logger.info("PDF généré avec succès: {} (taille: {} bytes)", fileName, fileSize);
            return result;

        } catch (Exception e) {
            logger.error("Erreur lors de la génération PDF pour référence {}: {}", reference, e.getMessage());
            throw new Exception("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Crée le répertoire de sortie s'il n'existe pas
     */
    private void ensureOutputDirectoryExists() throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            logger.info("Répertoire de sortie créé: {}", outputPath.toAbsolutePath());
        }
    }

    /**
     * Crée le PDF avec le titre de référence et le QR code
     */
    private void createPdfWithQrCode(String reference, String qrCodeBase64, String filePath) throws Exception {
        try {
            // Créer le document PDF
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Marges
            document.setMargins(50, 50, 50, 50);

            // Titre principal
            Paragraph title = new Paragraph("QR Code Generator - Boaz Housing")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(title);

            // Référence
            Paragraph refParagraph = new Paragraph("Référence: " + reference)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(40);
            document.add(refParagraph);

            // QR Code
            byte[] qrCodeBytes = Base64.getDecoder().decode(qrCodeBase64);
            Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes));

            // Redimensionner le QR code (taille optimale pour visualisation)
            qrImage.setWidth(300);
            qrImage.setHeight(300);
            qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
            qrImage.setMarginBottom(30);

            document.add(qrImage);

            // Description
            Paragraph description = new Paragraph("Ce QR code a été généré avec le style unique Boaz-Housing.")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(description);

            // Informations de génération
            String generationInfo = String.format("Généré le: %s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")));
            Paragraph infoParagraph = new Paragraph(generationInfo)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic();
            document.add(infoParagraph);

            // Fermer le document
            document.close();

            logger.info("Document PDF créé avec succès: {}", filePath);

        } catch (Exception e) {
            logger.error("Erreur lors de la création du PDF: {}", e.getMessage());
            throw new Exception("Erreur lors de la création du document PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si un fichier PDF existe
     */
    public boolean pdfExists(String fileName) {
        Path filePath = Paths.get(outputDirectory, fileName);
        return Files.exists(filePath);
    }

    /**
     * Récupère le chemin complet d'un fichier PDF
     */
    public String getPdfPath(String fileName) {
        return Paths.get(outputDirectory, fileName).toString();
    }
}