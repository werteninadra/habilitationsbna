package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/{code}")
    public ResponseEntity<Application> updateApplication(
            @PathVariable String code,
            @RequestBody Application application) {
        Application updatedApplication = applicationService.updateApplication(code, application);
        return ResponseEntity.ok(updatedApplication);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteApplication(@PathVariable String code) {
        applicationService.deleteApplication(code);
        return ResponseEntity.noContent().build();
    }
}