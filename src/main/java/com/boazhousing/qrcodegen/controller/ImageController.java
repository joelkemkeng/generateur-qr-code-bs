package com.boazhousing.qrcodegen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur dédié au service des images QR code générées.
 *
 * Ce contrôleur permet l'accès public aux images QR code générées par
 * l'ImageQrCodeService. Il est séparé du contrôleur principal pour
 * une meilleure organisation et réutilisabilité.
 *
 * @author Boaz Housing Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/images")
@Tag(name = "Image Server", description = "Service des images QR code générées")
public class ImageController {

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Value("${app.images.output-directory:qr-images}")
    private String outputDirectory;

    /**
     * Endpoint pour servir les images QR code générées.
     *
     * Cet endpoint permet l'accès direct aux images PNG générées par le service.
     * Il valide l'existence du fichier et ses permissions avant de le servir.
     *
     * GET /images/{filename}
     *
     * @param filename Nom du fichier image à servir (doit être un PNG généré)
     * @return ResponseEntity contenant l'image ou une erreur
     */
    @GetMapping("/{filename}")
    @Operation(
            summary = "Servir une image QR code générée",
            description = "Retourne l'image PNG générée par son nom de fichier. " +
                         "L'image est servie avec les en-têtes appropriés pour " +
                         "affichage direct dans un navigateur ou intégration."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image servie avec succès"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Nom de fichier invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Image non trouvée"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors du service de l'image"
            )
    })
    public ResponseEntity<Resource> serveImage(
            @Parameter(
                    description = "Nom du fichier image PNG à servir",
                    example = "QR_IMG_ATT-DOCUMENT-001_20240321_143022.png",
                    required = true
            )
            @PathVariable String filename) {

        try {
            logger.info("Demande de service d'image: {}", filename);

            // Validation du format de nom de fichier pour sécurité
            if (!isValidImageFilename(filename)) {
                logger.warn("Nom de fichier image invalide: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // Construction du chemin complet vers l'image
            Path imagePath = Paths.get(outputDirectory).resolve(filename);
            File imageFile = imagePath.toFile();

            // Vérifications de sécurité et d'existence
            if (!imageFile.exists()) {
                logger.warn("Image non trouvée: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            if (!imageFile.isFile() || !imageFile.canRead()) {
                logger.error("Image non accessible: {}", imagePath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // Validation du type MIME
            String mimeType = Files.probeContentType(imagePath);
            if (mimeType == null || !mimeType.equals("image/png")) {
                logger.warn("Type de fichier non supporté pour {}: {}", filename, mimeType);
                return ResponseEntity.badRequest().build();
            }

            // Création de la ressource et préparation de la réponse
            Resource imageResource = new FileSystemResource(imageFile);

            // Configuration des en-têtes pour affichage optimal
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageFile.length());
            headers.setCacheControl("public, max-age=3600"); // Cache 1 heure

            logger.info("Image {} servie avec succès ({} bytes)", filename, imageFile.length());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageResource);

        } catch (Exception e) {
            logger.error("Erreur lors du service de l'image {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Valide le format du nom de fichier pour des raisons de sécurité.
     *
     * Vérifie que le nom de fichier correspond au pattern attendu pour
     * les images QR générées et ne contient pas de caractères dangereux.
     *
     * @param filename Nom de fichier à valider
     * @return true si le nom est valide et sécurisé
     */
    private boolean isValidImageFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Pattern pour les images QR: QR_IMG_{reference}_{date}_{time}.png
        String pattern = "^QR_IMG_[A-Za-z0-9\\-_]+_\\d{8}_\\d{6}\\.png$";

        if (!filename.matches(pattern)) {
            return false;
        }

        // Vérifications de sécurité supplémentaires
        // Pas de navigation de répertoire
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        // Longueur raisonnable
        if (filename.length() > 100) {
            return false;
        }

        return true;
    }

    /**
     * Endpoint de santé pour vérifier la disponibilité du service d'images.
     *
     * GET /images/health
     */
    @GetMapping("/health")
    @Operation(
            summary = "Vérifier la santé du service d'images",
            description = "Retourne l'état du service d'images et la disponibilité du répertoire"
    )
    public ResponseEntity<String> checkHealth() {
        try {
            Path outputPath = Paths.get(outputDirectory);
            boolean directoryExists = Files.exists(outputPath);
            boolean directoryWritable = Files.isWritable(outputPath);

            String status = String.format(
                "Service d'images: OK\n" +
                "Répertoire: %s\n" +
                "Existe: %s\n" +
                "Accessible en écriture: %s",
                outputPath.toString(),
                directoryExists ? "Oui" : "Non",
                directoryWritable ? "Oui" : "Non"
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(status);

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de santé: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }
}