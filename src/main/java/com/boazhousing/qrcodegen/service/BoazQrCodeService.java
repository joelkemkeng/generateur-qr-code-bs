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
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class BoazQrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(BoazQrCodeService.class);

    // Configuration technique pour le style Boaz-Housing
    private static final int QR_VERSION = 2;  // Version modérée pour équilibrer qualité et lisibilité
    private static final int BOX_SIZE = 10;   // Taille appropriée pour les détails
    private static final int BORDER_SIZE = 40; // Border size équivalent à border=4 * box_size
    private static final int LOGO_RADIUS = 6; // Zone optimisée pour logo visible + scannable

    // Couleurs officielles Boaz-Housing
    @Value("${app.colors.primary-blue:#0140ff}")
    private String primaryBlue;

    @Value("${app.colors.orange-dark:#f88206}")
    private String orangeDark;

    @Value("${app.colors.orange-medium:#fa9000}")
    private String orangeMedium;

    @Value("${app.colors.orange-light:#ffa94d}")
    private String orangeLight;

    @Value("${app.colors.background:#ffffff}")
    private String backgroundColor;

    @Value("${app.qr.base-url:https://housing.boaz-study.tech}")
    private String baseUrl;

    @Value("${app.qr.verification-path:/verif_doc}")
    private String verificationPath;

    /**
     * Génère un QR code avec le style exact Boaz-Housing
     * Style: points bleus circulaires + coins orange + logo central
     */
    public String generateBoazStyleQrCode(String reference) throws Exception {
        try {
            String verificationUrl = baseUrl + verificationPath + "?ref=" + reference;
            logger.info("Génération QR code style points bleus pour: {}", verificationUrl);

            // Configuration QR code avec correction d'erreur élevée
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 30% récupération - permet logo central
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // Désactiver la quiet zone ZXing pour gérer manuellement les bordures
            hints.put(EncodeHintType.MARGIN, 0);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, 0, 0, hints);

            int moduleCount = bitMatrix.getWidth();
            int imgSize = moduleCount * BOX_SIZE + 2 * BORDER_SIZE;

            // Créer l'image finale
            BufferedImage qrImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = qrImage.createGraphics();

            // Activer l'antialiasing pour des cercles parfaits
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Fond blanc
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, imgSize, imgSize);

            // Couleurs Boaz-Housing définies dans la configuration
            Color dotColor = Color.decode(primaryBlue);   // #0140ff
            Color cornerColorDark   = Color.decode(orangeDark);   // #f88206
            Color cornerColorMedium = Color.decode(orangeMedium); // #fa9000
            Color cornerColorLight  = Color.decode(orangeLight);  // #ffa94d

            // Dessiner le QR code avec style points circulaires
            for (int row = 0; row < moduleCount; row++) {
                for (int col = 0; col < moduleCount; col++) {
                    int x = BORDER_SIZE + col * BOX_SIZE;
                    int y = BORDER_SIZE + row * BOX_SIZE;

                    if (bitMatrix.get(col, row)) { // Module noir dans le QR original
                        if (isFinderPattern(row, col, moduleCount)) {
                            // Coins de détection : carrés orange dégradé (remplace le noir par orange)
                            Color cornerColor = getCornerColor(row, col, moduleCount, cornerColorDark, cornerColorMedium, cornerColorLight);
                            graphics.setColor(cornerColor);
                            graphics.fillRect(x, y, BOX_SIZE, BOX_SIZE);
                        } else if (!isLogoArea(row, col, moduleCount)) {
                            // Points bleus circulaires pour les données
                            graphics.setColor(dotColor);
                            int centerX = x + BOX_SIZE / 2;
                            int centerY = y + BOX_SIZE / 2;
                            int radius = BOX_SIZE / 3; // Points plus petits que les carrés
                            graphics.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                        }
                        // Si c'est dans la zone logo, on ne dessine rien (laisse blanc pour le logo)
                    }
                }
            }

            // Ajouter le logo central Boaz-Housing
            addCentralLogo(graphics, imgSize, dotColor);

            graphics.dispose();

            // Convertir en base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            logger.info("QR code style points bleus généré avec succès pour: {}", reference);
            return base64;

        } catch (Exception e) {
            logger.error("Erreur génération QR code stylé: {}", e.getMessage());
            throw new Exception("Erreur lors de la génération du QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifier si on est dans un coin de détection (finder pattern)
     */
    private boolean isFinderPattern(int row, int col, int moduleCount) {
        // Coins: (0,0), (0, moduleCount-7), (moduleCount-7, 0)
        int[][] patterns = {
                {0, 0},
                {0, moduleCount - 7},
                {moduleCount - 7, 0}
        };

        for (int[] pattern : patterns) {
            int pr = pattern[0];
            int pc = pattern[1];
            if (pr <= row && row < pr + 7 && pc <= col && col < pc + 7) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifier si on est dans la zone du logo central
     */
    private boolean isLogoArea(int row, int col, int moduleCount) {
        int center = moduleCount / 2;
        return Math.abs(row - center) <= LOGO_RADIUS && Math.abs(col - center) <= LOGO_RADIUS;
    }

    /**
     * Calculer la couleur du coin selon la distance du centre
     * Applique un dégradé orange selon la position
     */
    private Color getCornerColor(int row, int col, int moduleCount, Color darkColor, Color mediumColor, Color lightColor) {
        // Trouver la position dans le coin de détection (0-6)
        int finderRow = 0, finderCol = 0;

        int[][] patterns = {
                {0, 0},
                {0, moduleCount - 7},
                {moduleCount - 7, 0}
        };

        for (int[] pattern : patterns) {
            int pr = pattern[0];
            int pc = pattern[1];
            if (pr <= row && row < pr + 7 && pc <= col && col < pc + 7) {
                finderRow = row - pr;
                finderCol = col - pc;
                break;
            }
        }

        // Dégradé basé sur la distance du centre du coin
        int distance = Math.max(Math.abs(finderRow - 3), Math.abs(finderCol - 3));
        if (distance <= 1) { // Centre du coin
            return darkColor;
        } else if (distance <= 2) { // Milieu
            return mediumColor;
        } else { // Bordure
            return lightColor;
        }
    }

    /**
     * Ajouter le logo central Boaz-Housing avec fond circulaire
     */
    private void addCentralLogo(Graphics2D graphics, int imgSize, Color dotColor) {
        int centerX = imgSize / 2;
        int centerY = imgSize / 2;
        int logoSize = 180; // Taille optimisée pour la lisibilité

        try {
            // Charger le logo Boaz-Housing depuis les ressources
            var logoResource = getClass().getResourceAsStream("/static/assets/logo-simplifier-bh.png");
            if (logoResource != null) {
                BufferedImage logoImg = ImageIO.read(logoResource);

                // Redimensionner le logo en gardant les proportions
                int logoWidth = logoImg.getWidth();
                int logoHeight = logoImg.getHeight();
                int newWidth, newHeight;

                if (logoWidth > logoHeight) {
                    newWidth = logoSize;
                    newHeight = (logoHeight * logoSize) / logoWidth;
                } else {
                    newHeight = logoSize;
                    newWidth = (logoWidth * logoSize) / logoHeight;
                }

                // Créer un fond blanc circulaire optimisé pour le logo
                int bgRadius = Math.min(newWidth, newHeight) / 2 - 25; // Rayon optimisé

                // Fond blanc circulaire minimal avec bordure subtile
                graphics.setColor(Color.WHITE);
                graphics.fillOval(centerX - bgRadius, centerY - bgRadius, bgRadius * 2, bgRadius * 2);
                graphics.setColor(dotColor);
                graphics.setStroke(new BasicStroke(1));
                graphics.drawOval(centerX - bgRadius, centerY - bgRadius, bgRadius * 2, bgRadius * 2);

                // Calculer position pour centrer le logo
                int logoX = centerX - newWidth / 2;
                int logoY = centerY - newHeight / 2;

                // Dessiner le logo redimensionné
                graphics.drawImage(logoImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH),
                                 logoX, logoY, null);

                logger.info("Logo Boaz-Housing intégré avec succès dans le QR code");
                logoResource.close();
                return;
            }
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du logo: {}", e.getMessage());
        }

        // Fallback : fond blanc avec texte "BH" si logo introuvable
        int bgRadius = 25;
        graphics.setColor(Color.WHITE);
        graphics.fillOval(centerX - bgRadius, centerY - bgRadius, bgRadius * 2, bgRadius * 2);
        graphics.setColor(dotColor);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawOval(centerX - bgRadius, centerY - bgRadius, bgRadius * 2, bgRadius * 2);

        // Texte "BH" au centre
        graphics.setColor(dotColor);
        graphics.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = graphics.getFontMetrics();
        String text = "BH";
        int textX = centerX - fm.stringWidth(text) / 2;
        int textY = centerY + fm.getAscent() / 2;
        graphics.drawString(text, textX, textY);

        logger.info("Logo Boaz-Housing 'BH' fallback intégré avec succès dans le QR code");
    }
}