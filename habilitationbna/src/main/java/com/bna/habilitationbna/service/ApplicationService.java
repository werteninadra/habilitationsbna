package com.bna.habilitationbna.service;

import com.bna.habilitationbna.exception.RessourceNotFoundException;
import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.repo.Applicationrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final Applicationrepo applicationRepository;

    // Création d'une application
    public Application createApplication(Application application) {
        if (application.getCode() == null || application.getCode().isEmpty()) {
            throw new IllegalArgumentException("Le code de l'application est requis");
        }

        if (applicationRepository.existsById(application.getCode())) {
            throw new IllegalArgumentException("Une application avec ce code existe déjà");
        }

        return applicationRepository.save(application);
    }

    // Récupération de toutes les applications
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    // Récupération d'une application par son code
    public Application getApplicationByCode(String code) {
        return applicationRepository.findById(code)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Application non trouvée avec le code: " + code));
    }

    // Mise à jour d'une application existante
    public Application updateApplication(Application application) {
        if (!applicationRepository.existsById(application.getCode())) {
            throw new RessourceNotFoundException(
                    "Application non trouvée avec le code: " + application.getCode());
        }
        return applicationRepository.save(application);
    }

    // Suppression d'une application
    public void deleteApplication(String code) {
        if (!applicationRepository.existsById(code)) {
            throw new RessourceNotFoundException(
                    "Application non trouvée avec le code: " + code);
        }
        applicationRepository.deleteById(code);
    }
}
