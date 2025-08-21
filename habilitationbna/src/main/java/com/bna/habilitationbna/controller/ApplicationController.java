package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin("*")
@RequiredArgsConstructor // Ajoutez cette annotation
public class ApplicationController {

    private final ApplicationService applicationService; // Utilisez final

    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody Application application) {
        try {
            Application created = applicationService.createApplication(application);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur serveur");
        }
    }


    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Application> getApplicationByCode(@PathVariable String code) {
        Application application = applicationService.getApplicationByCode(code);
        return ResponseEntity.ok(application);
    }


    // Dans ApplicationController.java
    @PutMapping("/{code}")
    public ResponseEntity<?> updateApplication(
            @PathVariable String code,
            @RequestBody Map<String, Object> updateData) {

        try {
            // Get existing application
            Application existing = applicationService.getApplicationByCode(code);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Application not found with code: " + code));
            }

            // Apply updates
            if (updateData.containsKey("libelle")) {
                existing.setLibelle((String) updateData.get("libelle"));
            }
            if (updateData.containsKey("description")) {
                existing.setDescription((String) updateData.get("description"));
            }

            // Save updates
            Application updated = applicationService.updateApplication(existing);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error updating application: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String code) {
        applicationService.deleteApplication(code);
        return ResponseEntity.noContent().build();
    }
}