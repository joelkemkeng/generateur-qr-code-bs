package com.boazhousing.qrcodegen.controller;

import com.boazhousing.qrcodegen.service.PdfGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QrCodeController.class)
class QrCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGenerateQrCode_ValidReference() throws Exception {
        // Test avec une référence valide
        mockMvc.perform(get("/generate-qr")
                        .param("reference", "ATT-TEST123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testGenerateQrCode_EmptyReference() throws Exception {
        // Test avec référence vide
        mockMvc.perform(get("/generate-qr")
                        .param("reference", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testGenerateQrCode_MissingReference() throws Exception {
        // Test sans paramètre référence
        mockMvc.perform(get("/generate-qr")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateQrCode_InvalidReference() throws Exception {
        // Test avec référence contenant des caractères invalides
        mockMvc.perform(get("/generate-qr")
                        .param("reference", "ATT@#$%^&*()")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testGenerateQrCode_TooLongReference() throws Exception {
        // Test avec référence trop longue
        String longReference = "ATT-" + "A".repeat(60);
        mockMvc.perform(get("/generate-qr")
                        .param("reference", longReference)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testListGeneratedPdfs() throws Exception {
        // Test de la liste des PDFs générés
        mockMvc.perform(get("/list-generated")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testDownloadPdf_InvalidFileName() throws Exception {
        // Test téléchargement avec nom de fichier invalide
        mockMvc.perform(get("/download/invalid-file.pdf")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadPdf_ValidButNonExistentFile() throws Exception {
        // Test téléchargement avec nom de fichier valide mais inexistant
        mockMvc.perform(get("/download/QR_ATT-TEST123_20240101_120000.pdf")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}