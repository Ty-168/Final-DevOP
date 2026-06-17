package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST endpoints for Profile (cardholder) management.
 *
 *  GET    /api/profiles            → list all
 *  GET    /api/profiles/{id}       → get by DB id
 *  GET    /api/profiles/uuid/{uuid} → get by UUID
 *  GET    /api/profiles/search     → full-name / reg-number search
 *  GET    /api/profiles/type/{type} → filter by STUDENT / EMPLOYEE / USER
 *  POST   /api/profiles            → create (UUID & reg-number auto-generated)
 *  PUT    /api/profiles/{id}       → update
 *  DELETE /api/profiles/{id}       → delete
 *  POST   /api/profiles/{id}/upload-photo → upload photo
 */
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public List<Profile> listAll() {
        return profileService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(profileService.getProfileById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Profile> getByUuid(@PathVariable String uuid) {
        return profileService.getProfileByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Profile> search(@RequestParam String q) {
        return profileService.searchProfiles(q);
    }

    @GetMapping("/type/{type}")
    public List<Profile> byType(@PathVariable ProfileType type) {
        return profileService.getProfilesByType(type);
    }

    @PostMapping
    public ResponseEntity<Profile> create(@RequestBody Profile profile) {
        return ResponseEntity.ok(profileService.saveProfile(profile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> update(@PathVariable Long id, @RequestBody Profile body) {
        try {
            Profile existing = profileService.getProfileById(id);
            // Selectively update mutable fields
            existing.setFullName(body.getFullName());
            existing.setDepartment(body.getDepartment());
            existing.setTitle(body.getTitle());
            existing.setEmail(body.getEmail());
            existing.setPhone(body.getPhone());
            existing.setBloodGroup(body.getBloodGroup());
            existing.setDateOfBirth(body.getDateOfBirth());
            existing.setExpiryDate(body.getExpiryDate());
            existing.setType(body.getType());
            existing.setBarcodeType(body.getBarcodeType());
            existing.setTemplate(body.getTemplate());
            return ResponseEntity.ok(profileService.saveProfile(existing));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /** Upload or replace a profile photo. */
    @PostMapping(value = "/{id}/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Profile> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            Profile updated = profileService.uploadProfilePhoto(id, file);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}