package com.bna.habilitationbna.service;

import com.bna.habilitationbna.exception.ResourceNotFoundException;
import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.repo.Applicationrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional // Déplacez cette annotation au niveau de la classe
public class ApplicationService {

    private final Applicationrepo applicationRepository; // Renommez Applicationrepo en ApplicationRepository

    public Application createApplication(Application application) {
        if (application.getCode() == null || application.getCode().isEmpty()) {
            throw new IllegalArgumentException("Le code de l'application est requis");
        }

        if (applicationRepository.existsById(application.getCode())) {
            throw new IllegalArgumentException("Une application avec ce code existe déjà");
        }

        try {
            return applicationRepository.save(application);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'application", e);
        }
    }
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Application getApplicationByCode(String code) {
        return applicationRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Application non trouvée avec le code: " + code));
    }

    // Dans ApplicationService.java
    public Application updateApplication(Application application) {
        // Verify the application exists
        if (!applicationRepository.existsById(application.getCode())) {
            throw new ResourceNotFoundException("Application not found with code: " + application.getCode());
        }

        return applicationRepository.save(application);
    }

    public void deleteApplication(String code) {
        if (!applicationRepository.existsById(code)) {
            throw new ResourceNotFoundException("Application non trouvée avec le code: " + code);
        }
        applicationRepository.deleteById(code);
    }
}