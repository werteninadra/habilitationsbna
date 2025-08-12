package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.Ressource;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.RessourceRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class RessourceService {

    private final RessourceRepository ressourceRepository;
    private final ApplicationService applicationService;
private  final  ProfilRepository  profilRepository;
    private final String FLASK_URL = "http://127.0.0.1:5001/prediction";

    @Autowired
    public RessourceService(RessourceRepository ressourceRepository,
                            ApplicationService applicationService , ProfilRepository profilRepository)  {
        this.ressourceRepository = ressourceRepository;
        this.applicationService = applicationService;
        this.profilRepository=profilRepository;
    }
    private double predireTemps(String libelle, String typeRessource) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Corps JSON envoyé à Flask
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("libelle", libelle);
            requestBody.put("typeRessource", typeRessource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Appel Flask
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    FLASK_URL,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object prediction = response.getBody().get("temps_estime_jours");
                return prediction != null ? Double.parseDouble(prediction.toString()) : 0.0;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la prédiction Flask: " + e.getMessage());
        }
        return 0.0; // Valeur par défaut si erreur
    }

    public Ressource addRessource(Ressource ressource) {
        if (ressource.getApplication() != null) {
            Application app = applicationService.getApplicationByCode(
                    ressource.getApplication().getCode());
            ressource.setApplication(app);
        }

        // Prédire automatiquement si libelle et typeRessource sont fournis
        if (ressource.getLibelle() != null && ressource.getTypeRessource() != null) {
            double prediction = predireTemps(ressource.getLibelle(), ressource.getTypeRessource());
            // Il faut avoir un champ tempsEstimeJours dans l'entité Ressource
            ressource.setTempsEstimeJours(prediction);
        }

        return ressourceRepository.save(ressource);
    }

    public Ressource updateRessource(String code, Ressource updated) {
        Ressource existing = ressourceRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));

        existing.setLibelle(updated.getLibelle());
        existing.setTypeRessource(updated.getTypeRessource());
        existing.setStatut(updated.isStatut());

        if (updated.getApplication() != null) {
            if (existing.getApplication() == null) {
                existing.setApplication(new Application());
            }
            existing.getApplication().setCode(updated.getApplication().getCode());
        } else {
            existing.setApplication(null);
        }

        // Recalcul du temps estimé
        if (updated.getLibelle() != null && updated.getTypeRessource() != null) {
            double prediction = predireTemps(updated.getLibelle(), updated.getTypeRessource());
            existing.setTempsEstimeJours(prediction);
        }

        return ressourceRepository.save(existing);
    }


    public void deleteRessource(String code) {
        // Vérification d'existence avant suppression
        if (!ressourceRepository.existsById(code)) {
            throw new RuntimeException("Ressource non trouvée");
        }
        ressourceRepository.deleteById(code);
    }
    public Ressource uploadFileToRessource(String code, File file) throws IOException {
        Ressource ressource = ressourceRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("Ressource not found"));

        // Upload vers Pinata
        String ipfsHash = IpfsUploader.uploadFile(file);
        if (ipfsHash == null) {
            throw new RuntimeException("Erreur : hash IPFS non retourné par Pinata");
        }

        String ipfsUrl = "https://gateway.pinata.cloud/ipfs/" + ipfsHash;

        ressource.setIpfsUrl(ipfsUrl);
        return ressourceRepository.save(ressource);
    }

    public List<Ressource> getAllRessources() {
        return ressourceRepository.findAll();
    }

    public Optional<Ressource> getRessourceById(String code) {
        // Chargement explicite de l'application
        return ressourceRepository.findById(code)
                .map(resource -> {
                    Hibernate.initialize(resource.getApplication());
                    return resource;
                });
    }

    @Transactional
    public void assignToProfil(String ressourceCode, String profilCode) {
        Ressource ressource = ressourceRepository.findById(ressourceCode)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));
        Profil profil = profilRepository.findById(profilCode)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé"));

        // Vérification de non-doublon
        if (!profil.getRessources().contains(ressource)) {
            profil.getRessources().add(ressource);
            profilRepository.save(profil);
        }
    }

    public List<Ressource> getAllRessourcesWithProfils() {
        try {
            List<Ressource> ressources = ressourceRepository.findAll();

            for (Ressource r : ressources) {
                // Charger les relations
                Hibernate.initialize(r.getProfils());
                if (r.getApplication() != null) {
                    Hibernate.initialize(r.getApplication());
                }

                // Ajouter ou recalculer la prédiction
                if (r.getLibelle() != null && r.getTypeRessource() != null) {
                    double prediction = predireTemps(r.getLibelle(), r.getTypeRessource());
                    r.setTempsEstimeJours(prediction);
                }
            }
            return ressources;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chargement des ressources avec profils", e);
        }
    }


    // Méthode supplémentaire pour charger avec application
    public List<Ressource> getAllWithApplication() {
        List<Ressource> ressources = ressourceRepository.findAll();
        ressources.forEach(r -> Hibernate.initialize(r.getApplication()));
        return ressources;
    }
}