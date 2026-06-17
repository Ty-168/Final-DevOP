package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.model.Template;
import com.example.demo.repository.ProfileRepository;
import com.example.demo.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Batch ID card generation service.
 *
 * Allows creating many profiles at once for a class, team, or department.
 * Each profile receives an auto-generated UUID and registration number.
 */
@Service
@RequiredArgsConstructor
public class BatchService {

    private final ProfileRepository         profileRepository;
    private final TemplateRepository        templateRepository;
    private final RegistrationNumberService regNumService;

    // ── DTO for a single row in a batch request ──────────────────────────────

    public record BatchProfileRequest(
            String    fullName,
            String    department,
            String    title,
            String    email,
            String    phone,
            String    bloodGroup,
            LocalDate dateOfBirth,
            LocalDate expiryDate,
            ProfileType type,
            Long      templateId,
            BarcodeType barcodeType
    ) {}

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Creates and persists a list of profiles in a single transaction.
     *
     * @param requests  list of cardholder data rows
     * @return list of saved Profile entities (with generated IDs)
     */
    @Transactional
    public List<Profile> createBatch(List<BatchProfileRequest> requests) {
        List<Profile> saved = new ArrayList<>();

        for (BatchProfileRequest req : requests) {
            Template template = resolveTemplate(req.templateId());
            ProfileType type  = req.type() != null ? req.type() : ProfileType.STUDENT;

            String regNum = regNumService.generate(type, req.department());

            Profile profile = Profile.builder()
                    .uuid(UUID.randomUUID().toString())
                    .registrationNumber(regNum)
                    .type(type)
                    .fullName(req.fullName())
                    .department(req.department())
                    .title(req.title())
                    .email(req.email())
                    .phone(req.phone())
                    .bloodGroup(req.bloodGroup())
                    .dateOfBirth(req.dateOfBirth())
                    .issueDate(LocalDate.now())
                    .expiryDate(req.expiryDate() != null
                            ? req.expiryDate()
                            : LocalDate.now().plusYears(4))
                    .template(template)
                    .barcodeType(req.barcodeType() != null
                            ? req.barcodeType()
                            : BarcodeType.CODE_128)
                    .build();

            saved.add(profileRepository.save(profile));
        }

        return saved;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Template resolveTemplate(Long templateId) {
        if (templateId == null) {
            // Use the first available template, or return null (card renders with defaults)
            return templateRepository.findAll().stream().findFirst().orElse(null);
        }
        return templateRepository.findById(templateId).orElse(null);
    }
}
