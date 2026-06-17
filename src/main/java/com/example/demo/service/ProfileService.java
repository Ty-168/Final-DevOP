package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository         profileRepository;
    private final PhotoStorageService        photoStorageService;
    private final RegistrationNumberService  registrationNumberService;

    // ── CREATE / UPDATE ──────────────────────────────────────────────────────

    /**
     * Persists a profile. If uuid or registrationNumber are blank, they are
     * auto-generated before saving.
     */
    @Transactional
    public Profile saveProfile(Profile profile) {
        if (profile.getUuid() == null || profile.getUuid().isBlank()) {
            profile.setUuid(UUID.randomUUID().toString());
        }
        if (profile.getRegistrationNumber() == null || profile.getRegistrationNumber().isBlank()) {
            ProfileType type = profile.getType() != null ? profile.getType() : ProfileType.USER;
            profile.setRegistrationNumber(
                    registrationNumberService.generate(type, profile.getDepartment()));
        }
        return profileRepository.save(profile);
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
    }

    public Optional<Profile> getProfileByRegNum(String regNum) {
        return profileRepository.findByRegistrationNumber(regNum);
    }

    public Optional<Profile> getProfileByUuid(String uuid) {
        return profileRepository.findByUuid(uuid);
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public List<Profile> searchProfiles(String query) {
        return profileRepository
                .findByFullNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(query, query);
    }

    public List<Profile> getProfilesByType(ProfileType type) {
        return profileRepository.findByType(type);
    }

    // ── PHOTO UPLOAD ─────────────────────────────────────────────────────────

    @Transactional
    public Profile uploadProfilePhoto(Long profileId, MultipartFile file) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile ID " + profileId + " not found."));

        String generatedFileName = photoStorageService.storePhoto(file);

        profile.setPhotoFileName(generatedFileName);
        profile.setPhotoContentType(file.getContentType());

        return profileRepository.save(profile);
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public void deleteProfile(Long id) {
        profileRepository.deleteById(id);
    }
}
