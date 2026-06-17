package com.example.demo.service;

import com.example.demo.model.Template;
import com.example.demo.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CRUD service for card Template entities.
 */
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<Template> getById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getByCode(String code) {
        return templateRepository.findByCode(code);
    }

    public List<Template> search(String query) {
        return templateRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(query, query);
    }

    public Template save(Template template) {
        return templateRepository.save(template);
    }

    public void delete(Long id) {
        templateRepository.deleteById(id);
    }
}
