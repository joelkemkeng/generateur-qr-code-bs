package com.boazhousing.qrcodegen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.qr.base-url=https://test.boaz-housing.com",
        "app.qr.verification-path=/verif_doc",
        "app.colors.primary-blue=#0140ff",
        "app.colors.orange-dark=#f88206"
})
class BoazQrCodeServiceTest {

    private BoazQrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new BoazQrCodeService();

        // Inject test properties using reflection
        ReflectionTestUtils.setField(qrCodeService, "primaryBlue", "#0140ff");
        ReflectionTestUtils.setField(qrCodeService, "orangeDark", "#f88206");
        ReflectionTestUtils.setField(qrCodeService, "orangeMedium", "#fa9000");
        ReflectionTestUtils.setField(qrCodeService, "orangeLight", "#ffa94d");
        ReflectionTestUtils.setField(qrCodeService, "backgroundColor", "#ffffff");
        ReflectionTestUtils.setField(qrCodeService, "baseUrl", "https://test.boaz-housing.com");
        ReflectionTestUtils.setField(qrCodeService, "verificationPath", "/verif_doc");
    }

    @Test
    void testGenerateBoazStyleQrCode_ValidReference() throws Exception {
        // Test avec une référence valide
        String reference = "ATT-TEST123456789";

        String result = qrCodeService.generateBoazStyleQrCode(reference);

        assertNotNull(result, "Le QR code généré ne doit pas être null");
        assertFalse(result.isEmpty(), "Le QR code généré ne doit pas être vide");

        // Vérifier que c'est du base64 valide
        assertDoesNotThrow(() -> {
            Base64.getDecoder().decode(result);
        }, "Le résultat doit être du base64 valide");

        // Vérifier que la taille est raisonnable (image PNG base64)
        assertTrue(result.length() > 1000, "Le QR code doit avoir une taille suffisante");
        assertTrue(result.length() < 100000, "Le QR code ne doit pas être trop volumineux");
    }

    @Test
    void testGenerateBoazStyleQrCode_MultipleReferences() throws Exception {
        // Test avec plusieurs références pour vérifier la cohérence
        String[] references = {
                "ATT-ABC123",
                "ATT-XYZ789",
                "REF-TEST001"
        };

        for (String reference : references) {
            String result = qrCodeService.generateBoazStyleQrCode(reference);

            assertNotNull(result, "QR code pour " + reference + " ne doit pas être null");
            assertFalse(result.isEmpty(), "QR code pour " + reference + " ne doit pas être vide");

            // Chaque référence doit générer un QR code différent
            for (String otherRef : references) {
                if (!reference.equals(otherRef)) {
                    String otherResult = qrCodeService.generateBoazStyleQrCode(otherRef);
                    assertNotEquals(result, otherResult,
                            "Les QR codes pour " + reference + " et " + otherRef + " doivent être différents");
                }
            }
        }
    }

    @Test
    void testGenerateBoazStyleQrCode_EmptyReference() {
        // Test avec référence vide
        assertThrows(Exception.class, () -> {
            qrCodeService.generateBoazStyleQrCode("");
        }, "Une référence vide doit lever une exception");
    }

    @Test
    void testGenerateBoazStyleQrCode_NullReference() {
        // Test avec référence null
        assertThrows(Exception.class, () -> {
            qrCodeService.generateBoazStyleQrCode(null);
        }, "Une référence null doit lever une exception");
    }

    @Test
    void testGenerateBoazStyleQrCode_LongReference() throws Exception {
        // Test avec une référence très longue
        String longReference = "ATT-" + "A".repeat(100);

        String result = qrCodeService.generateBoazStyleQrCode(longReference);

        assertNotNull(result, "Le QR code pour une référence longue ne doit pas être null");
        assertFalse(result.isEmpty(), "Le QR code pour une référence longue ne doit pas être vide");
    }

    @Test
    void testGenerateBoazStyleQrCode_SpecialCharacters() throws Exception {
        // Test avec des caractères spéciaux dans la référence
        String specialReference = "ATT-TEST_2024-01";

        String result = qrCodeService.generateBoazStyleQrCode(specialReference);

        assertNotNull(result, "Le QR code avec caractères spéciaux ne doit pas être null");
        assertFalse(result.isEmpty(), "Le QR code avec caractères spéciaux ne doit pas être vide");
    }

    @Test
    void testQrCodeConsistency() throws Exception {
        // Test de cohérence - même référence doit donner même QR code
        String reference = "ATT-CONSISTENCY-TEST";

        String result1 = qrCodeService.generateBoazStyleQrCode(reference);
        String result2 = qrCodeService.generateBoazStyleQrCode(reference);

        assertEquals(result1, result2,
                "La même référence doit toujours générer le même QR code");
    }

    @Test
    void testQrCodeBase64Format() throws Exception {
        // Test du format base64
        String reference = "ATT-FORMAT-TEST";

        String result = qrCodeService.generateBoazStyleQrCode(reference);

        // Vérifier que c'est du base64 valide
        assertDoesNotThrow(() -> {
            byte[] decodedBytes = Base64.getDecoder().decode(result);
            assertTrue(decodedBytes.length > 0, "Les données décodées ne doivent pas être vides");
        }, "Le QR code doit être en format base64 valide");

        // Le base64 ne doit contenir que des caractères valides
        assertTrue(result.matches("^[A-Za-z0-9+/=]*$"),
                "Le base64 ne doit contenir que des caractères valides");
    }
}