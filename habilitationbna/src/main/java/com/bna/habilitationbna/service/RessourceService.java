package com.bna.habilitationbna.service;

import com.bna.habilitationbna.exception.ProfilNotFoundException;
import com.bna.habilitationbna.exception.RessourceNotFoundException;
import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.model.Ressource;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.RessourceRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class RessourceService {

    private static final Logger logger = LoggerFactory.getLogger(RessourceService.class);

    private final RessourceRepository ressourceRepository;
    private final ApplicationService applicationService;
    private final ProfilRepository profilRepository;

    private static final String RESSOURCE_NOT_FOUND = "Ressource non trouvée";

    @Autowired
    public RessourceService(RessourceRepository ressourceRepository,
                            ApplicationService applicationService,
                            ProfilRepository profilRepository) {
        this.ressourceRepository = ressourceRepository;
        this.applicationService = applicationService;
        this.profilRepository = profilRepository;
    }

    private double predireTemps(String libelle, String typeRessource) {
        final String flaskUrl = "http://127.0.0.1:5001/prediction";

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("libelle", libelle);
            requestBody.put("typeRessource", typeRessource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    flaskUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            // Utilisation de Optional.of() car getBody() ne devrait jamais être null ici
            Map<String, Object> responseBody = Optional.of(response.getBody())
                    .orElseThrow(() -> new RuntimeException("Réponse Flask vide"));

            Object prediction = responseBody.get("temps_estime_jours");
            return prediction != null ? Double.parseDouble(prediction.toString()) : 0.0;

        } catch (Exception e) {
            logger.error("Erreur lors de la prédiction Flask pour {} / {} : {}", libelle, typeRessource, e.getMessage(), e);
            return 0.0;
        }
    }

    public Ressource addRessource(Ressource ressource) {
        if (ressource.getApplication() != null) {
            var app = applicationService.getApplicationByCode(ressource.getApplication().getCode());
            ressource.setApplication(app);
        }

        if (ressource.getLibelle() != null && ressource.getTypeRessource() != null) {
            var prediction = predireTemps(ressource.getLibelle(), ressource.getTypeRessource());
            ressource.setTempsEstimeJours(prediction);
        }

        return ressourceRepository.save(ressource);
    }

    public Ressource updateRessource(String code, Ressource updated) {
        var existing = ressourceRepository.findById(code)
                .orElseThrow(() -> new RessourceNotFoundException(RESSOURCE_NOT_FOUND));

        existing.setLibelle(updated.getLibelle());
        existing.setTypeRessource(updated.getTypeRessource());
        existing.setStatut(updated.isStatut());

        if (updated.getApplication() != null) {
            if (existing.getApplication() == null) existing.setApplication(new Application());
            existing.getApplication().setCode(updated.getApplication().getCode());
        } else {
            existing.setApplication(null);
        }

        if (updated.getLibelle() != null && updated.getTypeRessource() != null) {
            existing.setTempsEstimeJours(predireTemps(updated.getLibelle(), updated.getTypeRessource()));
        }

        return ressourceRepository.save(existing);
    }

    public void deleteRessource(String code) {
        if (!ressourceRepository.existsById(code)) {
            throw new RessourceNotFoundException(RESSOURCE_NOT_FOUND);
        }
        ressourceRepository.deleteById(code);
    }

    public Ressource uploadFileToRessource(String code, File file) throws IOException {
        var ressource = ressourceRepository.findById(code)
                .orElseThrow(() -> new RessourceNotFoundException(RESSOURCE_NOT_FOUND));

        String ipfsHash = IpfsUploader.uploadFile(file);
        if (ipfsHash == null)
            throw new RuntimeException("Erreur : hash IPFS non retourné par Pinata");

        ressource.setIpfsUrl("https://gateway.pinata.cloud/ipfs/" + ipfsHash);
        return ressourceRepository.save(ressource);
    }

    public void assignToProfil(String ressourceCode, String profilCode) {
        var ressource = ressourceRepository.findById(ressourceCode)
                .orElseThrow(() -> new RessourceNotFoundException(RESSOURCE_NOT_FOUND));

        var profil = profilRepository.findById(profilCode)
                .orElseThrow(() -> new ProfilNotFoundException("Profil non trouvé"));

        if (!profil.getRessources().contains(ressource)) {
            profil.getRessources().add(ressource);
            profilRepository.save(profil);
        }
    }

    public List<Ressource> getAllRessources() {
        return ressourceRepository.findAll();
    }

    public Optional<Ressource> getRessourceById(String code) {
        return ressourceRepository.findById(code)
                .map(r -> {
                    Hibernate.initialize(r.getApplication());
                    return r;
                });
    }

    public List<Ressource> getAllRessourcesWithProfils() {
        try {
            var ressources = ressourceRepository.findAll();
            for (var r : ressources) {
                Hibernate.initialize(r.getProfils());
                if (r.getApplication() != null) Hibernate.initialize(r.getApplication());
                if (r.getLibelle() != null && r.getTypeRessource() != null) {
                    r.setTempsEstimeJours(predireTemps(r.getLibelle(), r.getTypeRessource()));
                }
            }
            return ressources;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des ressources avec profils", e);
        }
    }

    public List<Ressource> getAllWithApplication() {
        var ressources = ressourceRepository.findAll();
        ressources.forEach(r -> Hibernate.initialize(r.getApplication()));
        return ressources;
    }
}
