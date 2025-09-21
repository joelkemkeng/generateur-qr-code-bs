package com.boazhousing.qrcodegen.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service isolé et réutilisable pour la génération d'images QR code avec le style Boaz-Housing.
 *
 * Ce service peut être facilement intégré dans d'autres applications Spring Boot.
 * Il génère des QR codes avec:
 * - Points bleus circulaires pour les données
 * - Coins orange dégradé pour les finder patterns
 * - Logo central Boaz-Housing
 * - Support de sauvegarde d'images et conversion base64
 *
 * @author Boaz Housing Development Team
 * @version 1.0.0
 * @since 2024
 */
@Service
public class ImageQrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(ImageQrCodeService.class);

    // Configuration technique pour le style Boaz-Housing
    private static final int BOX_SIZE = 10;           // Taille des modules QR en pixels
    private static final int BORDER_SIZE = 40;        // Bordure blanche autour du QR (4 * box_size)
    private static final int LOGO_RADIUS = 6;         // Rayon de la zone logo au centre (en modules)
    private static final int LOGO_DISPLAY_SIZE = 180; // Taille d'affichage du logo en pixels

    // Couleurs officielles Boaz-Housing selon les spécifications du design
    @Value("${app.colors.primary-blue:#0140ff}")
    private String primaryBlue;        // Couleur des points de données

    @Value("${app.colors.orange-dark:#f88206}")
    private String orangeDark;         // Couleur centre des corners

    @Value("${app.colors.orange-medium:#fa9000}")
    private String orangeMedium;       // Couleur milieu des corners

    @Value("${app.colors.orange-light:#ffa94d}")
    private String orangeLight;        // Couleur bordure des corners

    // Configuration des URLs et chemins
    @Value("${app.qr.base-url:https://housing.boaz-study.tech}")
    private String baseUrl;

    @Value("${app.qr.verification-path:/verif_doc}")
    private String verificationPath;

    @Value("${app.images.output-directory:qr-images}")
    private String outputDirectory;

    @Value("${app.images.base-access-url:http://localhost:8080/images}")
    private String baseAccessUrl;

    /**
     * Génère un QR code image avec le style Boaz-Housing et le sauvegarde.
     *
     * Cette méthode est le point d'entrée principal du service. Elle:
     * 1. Génère l'image QR code stylée
     * 2. Sauvegarde l'image sur le système de fichiers
     * 3. Retourne toutes les informations nécessaires (chemin, nom, base64)
     *
     * @param reference Référence unique pour générer l'URL de vérification
     * @return QrImageResult contenant toutes les informations de l'image générée
     * @throws Exception En cas d'erreur lors de la génération ou sauvegarde
     */
    public QrImageResult generateAndSaveQrCodeImage(String reference) throws Exception {
        try {
            logger.info("Démarrage génération image QR pour référence: {}", reference);

            // 1. Génération de l'image QR code stylée
            BufferedImage qrImage = generateStyledQrCodeImage(reference);

            // 2. Génération du nom de fichier unique
            String filename = generateUniqueFilename(reference);

            // 3. Sauvegarde de l'image
            String savedFilePath = saveImageToFileSystem(qrImage, filename);

            // 4. Conversion en base64 pour utilisation directe
            String base64Image = convertImageToBase64(qrImage);

            // 5. Construction de l'URL d'accès public
            String accessUrl = buildPublicAccessUrl(filename);

            // 6. Construction du résultat complet
            QrImageResult result = new QrImageResult(
                accessUrl,
                filename,
                base64Image,
                savedFilePath,
                reference
            );

            logger.info("Image QR générée avec succès: {} (taille: {} bytes)",
                       filename, base64Image.length());

            return result;

        } catch (Exception e) {
            logger.error("Erreur lors de la génération image QR pour référence {}: {}",
                        reference, e.getMessage(), e);
            throw new Exception("Erreur lors de la génération de l'image QR: " + e.getMessage(), e);
        }
    }

    /**
     * Génère l'image QR code avec le style visuel exact Boaz-Housing.
     *
     * Implémente l'algorithme de génération QR stylisé:
     * - Configuration ZXing sans quiet zone automatique
     * - Points bleus circulaires pour les données
     * - Corners orange avec dégradé selon la distance du centre
     * - Logo central avec fond blanc circulaire
     *
     * @param reference Référence pour construire l'URL de vérification
     * @return BufferedImage de l'image QR stylée
     * @throws Exception En cas d'erreur de génération
     */
    private BufferedImage generateStyledQrCodeImage(String reference) throws Exception {
        // Construction de l'URL de vérification finale
        String verificationUrl = baseUrl + verificationPath + "?ref=" + reference;
        logger.debug("URL de vérification générée: {}", verificationUrl);

        // Configuration ZXing pour génération optimisée
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 30% récupération (permet logo)
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); // CRITIQUE: Désactive la quiet zone automatique ZXing

        // Génération de la matrice QR brute
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, 0, 0, hints);

        int moduleCount = bitMatrix.getWidth();
        int totalImageSize = moduleCount * BOX_SIZE + 2 * BORDER_SIZE;

        logger.debug("Matrice QR: {}x{} modules, image finale: {}x{} pixels",
                    moduleCount, moduleCount, totalImageSize, totalImageSize);

        // Création de l'image finale avec antialiasing
        BufferedImage qrImage = new BufferedImage(totalImageSize, totalImageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = qrImage.createGraphics();

        // Configuration pour qualité maximale du rendu
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Fond blanc uniforme
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, totalImageSize, totalImageSize);

        // Préparation des couleurs Boaz-Housing
        Color dotColor = Color.decode(primaryBlue);
        Color cornerDark = Color.decode(orangeDark);
        Color cornerMedium = Color.decode(orangeMedium);
        Color cornerLight = Color.decode(orangeLight);

        // Parcours et stylisation de chaque module de la matrice QR
        for (int row = 0; row < moduleCount; row++) {
            for (int col = 0; col < moduleCount; col++) {
                int pixelX = BORDER_SIZE + col * BOX_SIZE;
                int pixelY = BORDER_SIZE + row * BOX_SIZE;

                // Traitement uniquement des modules "noirs" (actifs) du QR original
                if (bitMatrix.get(col, row)) {
                    if (isFinderPattern(row, col, moduleCount)) {
                        // Finder patterns: remplacement par orange dégradé
                        Color cornerColor = calculateCornerColor(row, col, moduleCount,
                                                               cornerDark, cornerMedium, cornerLight);
                        graphics.setColor(cornerColor);
                        graphics.fillRect(pixelX, pixelY, BOX_SIZE, BOX_SIZE);

                    } else if (!isLogoZone(row, col, moduleCount)) {
                        // Points de données: cercles bleus stylisés
                        graphics.setColor(dotColor);
                        int centerX = pixelX + BOX_SIZE / 2;
                        int centerY = pixelY + BOX_SIZE / 2;
                        int circleRadius = BOX_SIZE / 3;
                        graphics.fillOval(centerX - circleRadius, centerY - circleRadius,
                                        circleRadius * 2, circleRadius * 2);
                    }
                    // Zone logo: laissée blanche pour intégration du logo
                }
            }
        }

        // Intégration du logo central Boaz-Housing
        addCentralLogo(graphics, totalImageSize, dotColor);

        graphics.dispose();
        return qrImage;
    }

    /**
     * Détermine si un module fait partie d'un finder pattern (coin de détection).
     *
     * Les finder patterns sont des carrés 7x7 situés aux trois coins:
     * - Coin supérieur gauche: (0,0)
     * - Coin supérieur droit: (0, moduleCount-7)
     * - Coin inférieur gauche: (moduleCount-7, 0)
     *
     * @param row Position ligne du module
     * @param col Position colonne du module
     * @param moduleCount Taille totale de la matrice
     * @return true si le module fait partie d'un finder pattern
     */
    private boolean isFinderPattern(int row, int col, int moduleCount) {
        int[][] finderPositions = {
            {0, 0},                    // Coin supérieur gauche
            {0, moduleCount - 7},      // Coin supérieur droit
            {moduleCount - 7, 0}       // Coin inférieur gauche
        };

        for (int[] position : finderPositions) {
            int startRow = position[0];
            int startCol = position[1];

            if (startRow <= row && row < startRow + 7 &&
                startCol <= col && col < startCol + 7) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si un module se trouve dans la zone réservée au logo central.
     *
     * La zone logo est un carré centré de rayon LOGO_RADIUS modules.
     * Cette zone reste blanche pour permettre l'intégration du logo.
     *
     * @param row Position ligne du module
     * @param col Position colonne du module
     * @param moduleCount Taille totale de la matrice
     * @return true si le module est dans la zone logo
     */
    private boolean isLogoZone(int row, int col, int moduleCount) {
        int center = moduleCount / 2;
        return Math.abs(row - center) <= LOGO_RADIUS &&
               Math.abs(col - center) <= LOGO_RADIUS;
    }

    /**
     * Calcule la couleur orange à appliquer selon la position dans le finder pattern.
     *
     * Applique un dégradé radial du centre vers l'extérieur:
     * - Centre (distance <= 1): Orange foncé
     * - Milieu (distance <= 2): Orange moyen
     * - Bordure (distance > 2): Orange clair
     *
     * @param row Position ligne globale du module
     * @param col Position colonne globale du module
     * @param moduleCount Taille totale de la matrice
     * @param darkColor Couleur orange foncée (centre)
     * @param mediumColor Couleur orange moyenne (milieu)
     * @param lightColor Couleur orange claire (bordure)
     * @return Color à appliquer pour ce module
     */
    private Color calculateCornerColor(int row, int col, int moduleCount,
                                     Color darkColor, Color mediumColor, Color lightColor) {
        // Détermination du finder pattern et position locale dans ce pattern
        int localRow = 0, localCol = 0;

        int[][] finderPositions = {
            {0, 0},
            {0, moduleCount - 7},
            {moduleCount - 7, 0}
        };

        for (int[] position : finderPositions) {
            int startRow = position[0];
            int startCol = position[1];

            if (startRow <= row && row < startRow + 7 &&
                startCol <= col && col < startCol + 7) {
                localRow = row - startRow;
                localCol = col - startCol;
                break;
            }
        }

        // Calcul de la distance au centre du finder pattern (position 3,3)
        int distanceFromCenter = Math.max(Math.abs(localRow - 3), Math.abs(localCol - 3));

        // Application du dégradé selon la distance
        if (distanceFromCenter <= 1) {
            return darkColor;    // Centre: Orange foncé
        } else if (distanceFromCenter <= 2) {
            return mediumColor;  // Milieu: Orange moyen
        } else {
            return lightColor;   // Bordure: Orange clair
        }
    }

    /**
     * Ajoute le logo central Boaz-Housing sur l'image QR.
     *
     * Tente de charger le logo depuis les ressources et l'intègre avec:
     * - Redimensionnement proportionnel
     * - Fond blanc circulaire minimal
     * - Bordure subtile de la couleur des points
     * - Fallback texte "BH" si logo indisponible
     *
     * @param graphics Contexte graphique de l'image QR
     * @param imageSize Taille totale de l'image en pixels
     * @param borderColor Couleur pour la bordure du logo
     */
    private void addCentralLogo(Graphics2D graphics, int imageSize, Color borderColor) {
        int centerX = imageSize / 2;
        int centerY = imageSize / 2;

        try {
            // Tentative de chargement du logo depuis les ressources
            var logoStream = getClass().getResourceAsStream("/static/assets/logo-simplifier-bh.png");

            if (logoStream != null) {
                BufferedImage logoImage = ImageIO.read(logoStream);

                // Calcul du redimensionnement proportionnel
                int originalWidth = logoImage.getWidth();
                int originalHeight = logoImage.getHeight();
                int newWidth, newHeight;

                if (originalWidth > originalHeight) {
                    newWidth = LOGO_DISPLAY_SIZE;
                    newHeight = (originalHeight * LOGO_DISPLAY_SIZE) / originalWidth;
                } else {
                    newHeight = LOGO_DISPLAY_SIZE;
                    newWidth = (originalWidth * LOGO_DISPLAY_SIZE) / originalHeight;
                }

                // Fond blanc circulaire optimisé pour le logo
                int backgroundRadius = Math.min(newWidth, newHeight) / 2 - 25;

                // Dessin du fond blanc avec bordure
                graphics.setColor(Color.WHITE);
                graphics.fillOval(centerX - backgroundRadius, centerY - backgroundRadius,
                                backgroundRadius * 2, backgroundRadius * 2);
                graphics.setColor(borderColor);
                graphics.setStroke(new BasicStroke(1));
                graphics.drawOval(centerX - backgroundRadius, centerY - backgroundRadius,
                                backgroundRadius * 2, backgroundRadius * 2);

                // Centrage et dessin du logo redimensionné
                int logoX = centerX - newWidth / 2;
                int logoY = centerY - newHeight / 2;
                graphics.drawImage(logoImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
                                 logoX, logoY, null);

                logoStream.close();
                logger.debug("Logo Boaz-Housing intégré avec succès ({}x{} -> {}x{})",
                           originalWidth, originalHeight, newWidth, newHeight);
                return;
            }
        } catch (IOException e) {
            logger.warn("Impossible de charger le logo Boaz-Housing: {}", e.getMessage());
        }

        // Fallback: affichage "BH" si logo indisponible
        drawFallbackLogo(graphics, centerX, centerY, borderColor);
    }

    /**
     * Dessine un logo de secours "BH" si le logo principal est indisponible.
     *
     * @param graphics Contexte graphique
     * @param centerX Position X du centre
     * @param centerY Position Y du centre
     * @param textColor Couleur du texte et bordure
     */
    private void drawFallbackLogo(Graphics2D graphics, int centerX, int centerY, Color textColor) {
        int fallbackRadius = 25;

        // Fond blanc circulaire
        graphics.setColor(Color.WHITE);
        graphics.fillOval(centerX - fallbackRadius, centerY - fallbackRadius,
                         fallbackRadius * 2, fallbackRadius * 2);

        // Bordure colorée
        graphics.setColor(textColor);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawOval(centerX - fallbackRadius, centerY - fallbackRadius,
                         fallbackRadius * 2, fallbackRadius * 2);

        // Texte "BH" centré
        graphics.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fontMetrics = graphics.getFontMetrics();
        String fallbackText = "BH";
        int textX = centerX - fontMetrics.stringWidth(fallbackText) / 2;
        int textY = centerY + fontMetrics.getAscent() / 2;
        graphics.drawString(fallbackText, textX, textY);

        logger.info("Logo fallback 'BH' appliqué avec succès");
    }

    /**
     * Génère un nom de fichier unique pour l'image QR basé sur la référence et timestamp.
     *
     * Format: QR_IMG_{reference}_{yyyyMMdd}_{HHmmss}.png
     *
     * @param reference Référence du document
     * @return Nom de fichier unique
     */
    private String generateUniqueFilename(String reference) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        return String.format("QR_IMG_%s_%s.png", reference, timestamp);
    }

    /**
     * Sauvegarde l'image sur le système de fichiers.
     *
     * @param image Image à sauvegarder
     * @param filename Nom du fichier
     * @return Chemin complet du fichier sauvegardé
     * @throws IOException En cas d'erreur de sauvegarde
     */
    private String saveImageToFileSystem(BufferedImage image, String filename) throws IOException {
        // Création du répertoire de sortie si nécessaire
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            logger.info("Répertoire de sortie créé: {}", outputPath);
        }

        // Sauvegarde de l'image
        Path filePath = outputPath.resolve(filename);
        File outputFile = filePath.toFile();
        ImageIO.write(image, "PNG", outputFile);

        logger.info("Image sauvegardée: {} (taille: {} bytes)", filePath, outputFile.length());
        return filePath.toString();
    }

    /**
     * Convertit une BufferedImage en string base64.
     *
     * @param image Image à convertir
     * @return String base64 de l'image (sans préfixe data:image)
     * @throws IOException En cas d'erreur de conversion
     */
    private String convertImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Construit l'URL d'accès public à l'image.
     *
     * @param filename Nom du fichier
     * @return URL complète d'accès
     */
    private String buildPublicAccessUrl(String filename) {
        return baseAccessUrl + "/" + filename;
    }

    /**
     * Classe de résultat contenant toutes les informations de l'image QR générée.
     *
     * Cette classe encapsule toutes les données nécessaires pour utiliser
     * l'image QR générée dans différents contextes.
     */
    public static class QrImageResult {
        private final String accessUrl;      // URL publique d'accès à l'image
        private final String filename;       // Nom du fichier généré
        private final String base64Image;    // Image encodée en base64
        private final String filePath;       // Chemin complet sur le système de fichiers
        private final String reference;      // Référence utilisée pour la génération

        public QrImageResult(String accessUrl, String filename, String base64Image,
                           String filePath, String reference) {
            this.accessUrl = accessUrl;
            this.filename = filename;
            this.base64Image = base64Image;
            this.filePath = filePath;
            this.reference = reference;
        }

        // Getters avec documentation

        /** @return URL publique pour accéder à l'image via HTTP */
        public String getAccessUrl() { return accessUrl; }

        /** @return Nom du fichier généré (unique) */
        public String getFilename() { return filename; }

        /** @return Image encodée en base64 (prête pour intégration HTML/JSON) */
        public String getBase64Image() { return base64Image; }

        /** @return Chemin complet du fichier sur le système */
        public String getFilePath() { return filePath; }

        /** @return Référence utilisée pour générer le QR code */
        public String getReference() { return reference; }
    }
}