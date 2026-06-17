package com.example.demo.service;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Generates unique, human-readable registration numbers in YEAR-DEPT-### format.
 * Example output: 2026-ENG-014, 2026-STU-003
 */
@Service
@RequiredArgsConstructor
public class RegistrationNumberService {

    private final ProfileRepository profileRepository;

    /**
     * Generates the next sequential registration number.
     *
     * @param type       STUDENT or EMPLOYEE
     * @param department e.g. "Engineering", "Finance"  (first 3 letters used)
     * @return e.g. "2026-ENG-014"
     */
    public String generate(ProfileType type, String department) {
        int year = LocalDate.now().getYear();

        // Build department code: uppercase first 3 letters, or a type-based fallback
        String deptCode = buildDeptCode(type, department);

        // Count how many profiles already exist with the same prefix
        String prefix = year + "-" + deptCode + "-";
        long count = profileRepository.findAll()
                .stream()
                .filter(p -> p.getRegistrationNumber() != null
                        && p.getRegistrationNumber().startsWith(prefix))
                .count();

        // Sequential number, zero-padded to 3 digits
        String seq = String.format("%03d", count + 1);

        return prefix + seq;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private String buildDeptCode(ProfileType type, String department) {
        if (department != null && !department.isBlank()) {
            // Take first 3 letters of department name, uppercase
            String stripped = department.replaceAll("\\s+", "");
            return stripped.substring(0, Math.min(3, stripped.length())).toUpperCase();
        }
        // Fallback: type-based codes
        return switch (type) {
            case STUDENT  -> "STU";
            case EMPLOYEE -> "EMP";
            default       -> "USR";
        };
    }
}
