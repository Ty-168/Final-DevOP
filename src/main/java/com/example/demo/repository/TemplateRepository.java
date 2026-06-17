package com.example.demo.repository;

import com.example.demo.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    /** Check if a template with the given code already exists (for uniqueness validation). */
    boolean existsByCode(String code);

    /** Find a template by its unique code. */
    Optional<Template> findByCode(String code);

    /** Simple search by template name or code (case-insensitive). */
    List<Template> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}