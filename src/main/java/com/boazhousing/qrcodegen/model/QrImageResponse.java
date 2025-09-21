package com.boazhousing.qrcodegen.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Modèle de réponse pour la génération d'images QR code.
 *
 * Cette classe encapsule toutes les informations retournées lors de la génération
 * d'une image QR code, permettant une utilisation flexible dans différents contextes:
 * - Affichage web via URL d'accès
 * - Intégration directe via base64
 * - Téléchargement par nom de fichier
 *
 * @author Boaz Housing Development Team
 * @version 1.0.0
 */
@Schema(description = "Réponse complète pour une image QR code générée avec toutes les informations d'accès")
public class QrImageResponse {

    @Schema(description = "URL publique d'accès direct à l'image générée",
            example = "http://localhost:8080/images/QR_IMG_ATT-DOC-001_20240321_143022.png")
    private String accessUrl;

    @Schema(description = "Nom unique du fichier image généré",
            example = "QR_IMG_ATT-DOC-001_20240321_143022.png")
    private String imageName;

    @Schema(description = "Image encodée en base64 (format PNG, prête à utiliser dans HTML ou reconvertir)",
            example = "iVBORw0KGgoAAAANSUhEUgAAAZAAAAGQCAYAAACAvzbMAAAABHNCSVQICAgIfAhkiAAAAAlwSFlz...")
    private String base64Image;

    @Schema(description = "Référence du document utilisée pour générer le QR code",
            example = "ATT-DOC-001")
    private String reference;

    @Schema(description = "Taille de l'image base64 en caractères (pour information)",
            example = "45678")
    private int base64Size;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON.
     */
    public QrImageResponse() {}

    /**
     * Constructeur principal avec tous les paramètres essentiels.
     *
     * @param accessUrl URL publique d'accès à l'image
     * @param imageName Nom unique du fichier généré
     * @param base64Image Image encodée en base64
     * @param reference Référence du document
     */
    public QrImageResponse(String accessUrl, String imageName, String base64Image, String reference) {
        this.accessUrl = accessUrl;
        this.imageName = imageName;
        this.base64Image = base64Image;
        this.reference = reference;
        this.base64Size = base64Image != null ? base64Image.length() : 0;
    }

    // Getters et setters avec documentation

    /**
     * @return URL complète pour accéder directement à l'image via HTTP GET
     */
    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    /**
     * @return Nom unique du fichier image (peut être utilisé pour téléchargement)
     */
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * @return Image complète encodée en base64 (format PNG)
     *         Peut être directement utilisée dans une balise img HTML ou reconvertie
     */
    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
        this.base64Size = base64Image != null ? base64Image.length() : 0;
    }

    /**
     * @return Référence du document ayant servi à générer le QR code
     */
    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * @return Taille en caractères de la chaîne base64 (pour information technique)
     */
    public int getBase64Size() {
        return base64Size;
    }

    public void setBase64Size(int base64Size) {
        this.base64Size = base64Size;
    }

    @Override
    public String toString() {
        return "QrImageResponse{" +
                "accessUrl='" + accessUrl + '\'' +
                ", imageName='" + imageName + '\'' +
                ", reference='" + reference + '\'' +
                ", base64Size=" + base64Size +
                '}';
    }
}