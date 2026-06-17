package com.example.demo.controller;

import com.example.demo.model.Template;
import com.example.demo.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for Template (card theme) management.
 *
 *  GET    /api/templates           → list all
 *  GET    /api/templates/{id}      → get by id
 *  GET    /api/templates/search    → search by name/code
 *  POST   /api/templates           → create
 *  PUT    /api/templates/{id}      → update
 *  DELETE /api/templates/{id}      → delete
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public List<Template> listAll() {
        return templateService.getAllTemplates();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getById(@PathVariable Long id) {
        return templateService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Template> search(@RequestParam String q) {
        return templateService.search(q);
    }

    @PostMapping
    public ResponseEntity<Template> create(@RequestBody Template template) {
        return ResponseEntity.ok(templateService.save(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Template> update(@PathVariable Long id, @RequestBody Template body) {
        return templateService.getById(id).map(existing -> {
            existing.setName(body.getName());
            existing.setCode(body.getCode());
            existing.setOrganizationName(body.getOrganizationName());
            existing.setLayout(body.getLayout());
            existing.setPrimaryColor(body.getPrimaryColor());
            existing.setSecondaryColor(body.getSecondaryColor());
            existing.setTextColor(body.getTextColor());
            existing.setTagline(body.getTagline());
            return ResponseEntity.ok(templateService.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
