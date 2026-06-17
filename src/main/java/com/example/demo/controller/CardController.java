package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.service.BatchService;
import com.example.demo.service.CardRenderService;
import com.example.demo.service.PdfExportService;
import com.example.demo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for card operations:
 *  - GET  /api/cards/{id}/preview   → HTML live preview
 *  - GET  /api/cards/{id}/pdf       → PDF download (single card)
 *  - POST /api/cards/batch          → create batch of profiles
 *  - POST /api/cards/batch/pdf      → batch PDF download (list of IDs)
 */
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardRenderService cardRenderService;
    private final PdfExportService  pdfExportService;
    private final ProfileService    profileService;
    private final BatchService      batchService;

    // ── Live Preview ─────────────────────────────────────────────────────────

    /**
     * Returns the rendered Thymeleaf HTML for a single ID card.
     * The browser displays this directly as a live preview.
     */
    @GetMapping(value = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        String html = cardRenderService.renderCard(profile);
        return ResponseEntity.ok(html);
    }

    // ── Single PDF Export ────────────────────────────────────────────────────

    /**
     * Generates a PDF for a single profile and streams it to the browser.
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long id) {
        Profile profile = profileService.getProfileById(id);
        byte[] pdf = pdfExportService.exportSingleCardPdf(profile);

        String filename = "id-card-" + profile.getRegistrationNumber() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── Batch Create ─────────────────────────────────────────────────────────

    /**
     * Creates many profiles in one request.
     * Body: JSON array of BatchProfileRequest objects.
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Profile>> createBatch(
            @RequestBody List<BatchService.BatchProfileRequest> requests) {
        List<Profile> created = batchService.createBatch(requests);
        return ResponseEntity.ok(created);
    }

    // ── Batch PDF Export ─────────────────────────────────────────────────────

    /**
     * Downloads a single PDF containing all listed profile IDs (one card per page).
     * Body: JSON array of profile IDs, e.g. [1, 2, 3]
     */
    @PostMapping(value = "/batch/pdf", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> exportBatchPdf(@RequestBody List<Long> profileIds) {
        List<Profile> profiles = profileIds.stream()
                .map(profileService::getProfileById)
                .collect(Collectors.toList());

        byte[] pdf = pdfExportService.exportBatchPdf(profiles);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"id-cards-batch.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
