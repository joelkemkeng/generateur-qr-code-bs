package com.boazhousing.qrcodegen.controller;

import com.boazhousing.qrcodegen.model.ApiResponse;
import com.boazhousing.qrcodegen.model.QrGenerationResult;
import com.boazhousing.qrcodegen.model.QrImageResponse;
import com.boazhousing.qrcodegen.service.PdfGenerationService;
import com.boazhousing.qrcodegen.service.ImageQrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/")
@Tag(name = "QR Code Generator", description = "API pour générer des QR codes avec le style unique Boaz-Housing")
public class QrCodeController {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeController.class);

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private ImageQrCodeService imageQrCodeService;

    /**
     * Endpoint principal pour générer un QR code PDF
     * GET /generate-qr?reference=ATT-XXXXXXXX
     */
    @GetMapping("/generate-qr")
    @Operation(
            summary = "Générer un QR code PDF avec le style Boaz-Housing",
            description = "Génère un PDF contenant un QR code stylisé avec des points bleus circulaires, " +
                         "des coins orange dégradé et le logo central Boaz-Housing. Le QR code pointe vers " +
                         "l'URL de vérification avec la référence fournie."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "QR code PDF généré avec succès",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Référence invalide ou manquante",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne du serveur",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<QrGenerationResult>> generateQrCode(
            @Parameter(
                    description = "Référence unique pour le document (ex: ATT-XXXXXXXX)",
                    example = "ATT-DOCUMENT-001",
                    required = true
            )
            @RequestParam(name = "reference")
            @NotBlank(message = "La référence ne peut pas être vide")
            @Pattern(regexp = "^[A-Za-z0-9\\-_]+$", message = "La référence ne doit contenir que des lettres, chiffres, tirets et underscores")
            String reference) {

        try {
            logger.info("Demande de génération QR pour référence: {}", reference);

            // Validation supplémentaire
            if (StringUtils.isBlank(reference)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La référence est obligatoire"));
            }

            if (reference.length() > 50) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La référence ne peut pas dépasser 50 caractères"));
            }

            // Nettoyer la référence
            String cleanReference = reference.trim().toUpperCase();

            // Générer le PDF avec QR code
            QrGenerationResult result = pdfGenerationService.generateQrCodePdf(cleanReference);

            return ResponseEntity.ok(
                    ApiResponse.success(result, "QR code PDF généré avec succès")
            );

        } catch (Exception e) {
            logger.error("Erreur lors de la génération QR pour référence {}: {}", reference, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la génération du QR code", e.getMessage()));
        }
    }

    /**
     * Endpoint pour générer une image QR code avec toutes les informations d'accès.
     *
     * Ce nouvel endpoint génère une image QR code avec le style Boaz-Housing et retourne:
     * - L'URL d'accès direct à l'image
     * - Le nom unique du fichier généré
     * - L'image complète encodée en base64 (prête à utiliser)
     *
     * GET /generate-qr-image?reference=ATT-XXXXXXXX
     */
    @GetMapping("/generate-qr-image")
    @Operation(
            summary = "Générer une image QR code avec accès complet",
            description = "Génère une image QR code avec le style Boaz-Housing et retourne toutes les " +
                         "informations d'accès : URL publique, nom de fichier et image base64. " +
                         "L'image base64 peut être directement intégrée dans du HTML ou reconvertie " +
                         "en fichier PNG ailleurs. Le service est isolé et réutilisable."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Image QR code générée avec succès",
                    content = @Content(schema = @Schema(implementation = QrImageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Référence invalide ou manquante",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la génération de l'image",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<QrImageResponse>> generateQrImageWithFullAccess(
            @Parameter(
                    description = "Référence unique pour le document (ex: ATT-DOCUMENT-001). " +
                                 "Utilisée pour construire l'URL de vérification du QR code.",
                    example = "ATT-DOCUMENT-001",
                    required = true
            )
            @RequestParam(name = "reference")
            @NotBlank(message = "La référence ne peut pas être vide")
            @Pattern(regexp = "^[A-Za-z0-9\\-_]+$",
                    message = "La référence ne doit contenir que des lettres, chiffres, tirets et underscores")
            String reference) {

        try {
            logger.info("Demande de génération image QR pour référence: {}", reference);

            // Validation de base identique à l'endpoint PDF
            if (StringUtils.isBlank(reference)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La référence est obligatoire"));
            }

            if (reference.length() > 50) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("La référence ne peut pas dépasser 50 caractères"));
            }

            // Nettoyage de la référence
            String cleanReference = reference.trim().toUpperCase();

            // Génération de l'image QR avec le service isolé
            ImageQrCodeService.QrImageResult serviceResult =
                imageQrCodeService.generateAndSaveQrCodeImage(cleanReference);

            // Construction de la réponse utilisateur avec toutes les informations
            QrImageResponse response = new QrImageResponse(
                serviceResult.getAccessUrl(),
                serviceResult.getFilename(),
                serviceResult.getBase64Image(),
                serviceResult.getReference()
            );

            logger.info("Image QR générée avec succès pour référence {}: {} (base64: {} chars)",
                       cleanReference, serviceResult.getFilename(), serviceResult.getBase64Image().length());

            return ResponseEntity.ok(
                    ApiResponse.success(response, "Image QR code générée avec succès")
            );

        } catch (Exception e) {
            logger.error("Erreur lors de la génération image QR pour référence {}: {}",
                        reference, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la génération de l'image QR code", e.getMessage()));
        }
    }

    /**
     * Endpoint pour télécharger les PDFs générés
     * GET /download/{filename}
     */
    @GetMapping("/download/{filename}")
    @Operation(
            summary = "Télécharger un PDF généré",
            description = "Télécharge un fichier PDF généré précédemment par nom de fichier"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Fichier téléchargé avec succès",
                    content = @Content(mediaType = "application/pdf")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Nom de fichier invalide"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Fichier non trouvé"
            )
    })
    public ResponseEntity<Resource> downloadPdf(
            @Parameter(
                    description = "Nom du fichier PDF à télécharger",
                    example = "QR_ATT-DOCUMENT-001_20240101_120000.pdf",
                    required = true
            )
            @PathVariable String filename) {
        try {
            logger.info("Demande de téléchargement du fichier: {}", filename);

            // Validation du nom de fichier
            if (!filename.matches("^QR_[A-Za-z0-9\\-_]+_\\d{8}_\\d{6}\\.pdf$")) {
                logger.warn("Nom de fichier invalide: {}", filename);
                return ResponseEntity.badRequest().build();
            }

            // Vérifier l'existence du fichier
            if (!pdfGenerationService.pdfExists(filename)) {
                logger.warn("Fichier non trouvé: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Créer la ressource fichier
            String filePath = pdfGenerationService.getPdfPath(filename);
            Resource fileResource = new FileSystemResource(filePath);

            if (!fileResource.exists() || !fileResource.isReadable()) {
                logger.error("Fichier non accessible: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // Préparer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

            logger.info("Fichier {} téléchargé avec succès", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileResource.contentLength())
                    .body(fileResource);

        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement du fichier {}: {}", filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint pour lister les PDFs générés
     * GET /list-generated
     */
    @GetMapping("/list-generated")
    @Operation(
            summary = "Lister les PDFs générés",
            description = "Retourne la liste de tous les fichiers PDF générés dans le répertoire de sortie"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Liste récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la récupération de la liste"
            )
    })
    public ResponseEntity<ApiResponse<?>> listGeneratedPdfs() {
        try {
            String outputDirectory = "document-qr-code-generer";
            File directory = new File(outputDirectory);

            if (!directory.exists()) {
                return ResponseEntity.ok(
                        ApiResponse.success(new String[0], "Aucun fichier généré")
                );
            }

            File[] pdfFiles = directory.listFiles((dir, name) -> name.endsWith(".pdf"));
            String[] fileNames = new String[pdfFiles != null ? pdfFiles.length : 0];

            if (pdfFiles != null) {
                for (int i = 0; i < pdfFiles.length; i++) {
                    fileNames[i] = pdfFiles[i].getName();
                }
            }

            return ResponseEntity.ok(
                    ApiResponse.success(fileNames, String.format("%d fichier(s) trouvé(s)", fileNames.length))
            );

        } catch (Exception e) {
            logger.error("Erreur lors de la liste des fichiers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur lors de la récupération de la liste des fichiers"));
        }
    }

    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(Exception e) {
        logger.error("Erreur de validation: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Erreur de validation", e.getMessage()));
    }
}