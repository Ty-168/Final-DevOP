package com.example.demo.repository;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // Check existence by unique identifiers
    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByUuid(String uuid);

    // Search profile by UUID or Registration Number
    Optional<Profile> findByUuid(String uuid);

    Optional<Profile> findByRegistrationNumber(String registrationNumber);

    // Multi-field search for administration dashboard listing
    List<Profile> findByFullNameContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(String fullName,
            String regNum);

    // Filter by type (STUDENT / EMPLOYEE)
    List<Profile> findByType(ProfileType type);
}