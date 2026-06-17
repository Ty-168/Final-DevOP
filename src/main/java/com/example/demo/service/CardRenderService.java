package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Base64;
import java.util.Locale;

/**
 * Uses Thymeleaf to render the ID card HTML template for:
 *  - Live preview (returned as an HTML string to the browser)
 *  - Piped into iText html2pdf for PDF export
 */
@Service
@RequiredArgsConstructor
public class CardRenderService {

    private final TemplateEngine templateEngine;
    private final QrCodeService  qrCodeService;
    private final BarcodeService barcodeService;
    private final PhotoStorageService photoStorageService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Renders the id-card.html Thymeleaf template to an HTML string.
     *
     * @param profile the cardholder data
     * @return rendered HTML string
     */
    public String renderCard(Profile profile) {
        Context ctx = buildContext(profile);
        return templateEngine.process("id-card", ctx);
    }

    // ── Context Builder ──────────────────────────────────────────────────────

    private Context buildContext(Profile profile) {
        Context ctx = new Context(Locale.ENGLISH);

        Template tpl = profile.getTemplate();

        ctx.setVariable("profile", profile);
        ctx.setVariable("template", tpl);

        // QR Code: encodes a verification URL containing the card's UUID
        String verifyUrl = qrCodeService.buildVerificationUrl(profile.getUuid(), baseUrl);
        ctx.setVariable("qrCodeBase64", qrCodeService.generateQrCodeBase64(verifyUrl, 160));

        // Barcode: encode registration number
        if (profile.getBarcodeType() != null && profile.getRegistrationNumber() != null) {
            ctx.setVariable("barcodeBase64",
                    barcodeService.generateBarcodeBase64(
                            profile.getRegistrationNumber(),
                            profile.getBarcodeType()));
        }

        // Photo: read from disk and embed as Base64 if present
        if (profile.hasPhoto()) {
            ctx.setVariable("photoBase64", loadPhotoBase64(profile));
        }

        return ctx;
    }

    private String loadPhotoBase64(Profile profile) {
        try {
            byte[] bytes = photoStorageService.loadPhotoBytes(profile.getPhotoFileName());
            String mimeType = profile.getPhotoContentType() != null
                    ? profile.getPhotoContentType() : "image/jpeg";
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return ""; // gracefully degrade if file missing
        }
    }
}
