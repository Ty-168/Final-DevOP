package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Public verification endpoint — the URL embedded in QR codes points here.
 * GET /verify/{uuid}  →  returns card status info for the UUID
 */
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final ProfileService profileService;

    @GetMapping("/verify/{uuid}")
    public ResponseEntity<?> verify(@PathVariable String uuid) {
        return profileService.getProfileByUuid(uuid)
                .map(p -> ResponseEntity.ok(Map.of(
                        "status",             "VALID",
                        "name",               p.getFullName(),
                        "type",               p.getType().name(),
                        "registrationNumber", p.getRegistrationNumber(),
                        "department",         p.getDepartment() != null ? p.getDepartment() : "",
                        "issueDate",          p.getIssueDate() != null ? p.getIssueDate().toString() : "",
                        "expiryDate",         p.getExpiryDate() != null ? p.getExpiryDate().toString() : ""
                )))
                .orElse(ResponseEntity.ok(Map.of("status", "INVALID", "message", "No card found for this UUID.")));
    }
}
