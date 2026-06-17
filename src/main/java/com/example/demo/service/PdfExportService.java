package com.example.demo.service;

import com.example.demo.model.Profile;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a rendered HTML card (from Thymeleaf) into a PDF byte array
 * using iText's html2pdf module.
 *
 * Single-card and batch (multi-page) modes are both supported.
 */
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final CardRenderService cardRenderService;

    /**
     * Exports a single profile's ID card as a PDF byte array.
     *
     * @param profile the cardholder
     * @return PDF bytes ready to stream to the browser
     */
    public byte[] exportSingleCardPdf(Profile profile) {
        String html = cardRenderService.renderCard(profile);
        return htmlToPdf(html);
    }

    /**
     * Exports multiple profiles into a single PDF document (one card per page).
     * Each card is rendered independently so template colours are respected.
     *
     * @param profiles list of cardholders
     * @return merged PDF bytes
     */
    public byte[] exportBatchPdf(List<Profile> profiles) {
        // Build one big HTML document: each card wrapped in a page-break div
        StringBuilder combined = new StringBuilder();
        combined.append("<!DOCTYPE html><html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<style>")
                .append("  @page { margin: 0; size: 86mm 54mm; }")
                .append("  body { margin: 0; padding: 0; }")
                .append("  .page-break { page-break-after: always; }")
                .append("</style>")
                .append("</head><body>");

        List<String> renderedCards = new ArrayList<>();
        for (Profile profile : profiles) {
            renderedCards.add(cardRenderService.renderCard(profile));
        }

        for (int i = 0; i < renderedCards.size(); i++) {
            // Strip outer html/body tags from individual cards so they nest cleanly
            String cardBody = extractBody(renderedCards.get(i));
            combined.append("<div class=\"page-break\">")
                    .append(cardBody)
                    .append("</div>");
        }

        combined.append("</body></html>");
        return htmlToPdf(combined.toString());
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ConverterProperties props = new ConverterProperties();
            HtmlConverter.convertToPdf(html, baos, props);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert HTML to PDF", e);
        }
    }

    /**
     * Extracts the content between <body> tags for embedding in a combined document.
     * Falls back to returning the full HTML if no body tags are found.
     */
    private String extractBody(String html) {
        int start = html.indexOf("<body");
        int end   = html.lastIndexOf("</body>");
        if (start < 0 || end < 0) return html;
        int bodyStart = html.indexOf('>', start) + 1;
        return html.substring(bodyStart, end);
    }
}
