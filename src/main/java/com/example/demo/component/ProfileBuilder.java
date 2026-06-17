package com.example.demo.component;

import com.example.demo.model.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class ProfileBuilder {

    public Template createDefaultTemplate() {
        return Template.builder()
                .code("DEFAULT-V")
                .name("Default Blue Theme")
                .organizationName("My Academy / Company")
                .layout("VERTICAL")
                .primaryColor("#1d4ed8")
                .secondaryColor("#e0e7ff")
                .textColor("#111827")
                .tagline("Excellence in Education")
                .build();
    }

    public Profile createDefaultStudentProfile(Template template) {
        return Profile.builder()
                .uuid(UUID.randomUUID().toString())
                .registrationNumber("2026-GEN-000")
                .type(ProfileType.STUDENT)
                .fullName("John Doe")
                .department("General Sciences")
                .title("Year 1 Student")
                .email("johndoe@example.com")
                .phone("+85512345678")
                .bloodGroup("O+")
                .dateOfBirth(LocalDate.of(2005, 1, 1))
                .issueDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(4))
                .template(template)
                .barcodeType(BarcodeType.CODE_128)
                .build();
    }
}