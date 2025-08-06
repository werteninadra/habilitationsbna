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

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class RessourceService {

    private final RessourceRepository ressourceRepository;
    private final ApplicationService applicationService;
private  final  ProfilRepository  profilRepository;
    @Autowired
    public RessourceService(RessourceRepository ressourceRepository,
                            ApplicationService applicationService , ProfilRepository profilRepository)  {
        this.ressourceRepository = ressourceRepository;
        this.applicationService = applicationService;
        this.profilRepository=profilRepository;
    }

    public Ressource addRessource(Ressource ressource) {
        if (ressource.getApplication() != null) {
            Application app = applicationService.getApplicationByCode(
                    ressource.getApplication().getCode());
            ressource.setApplication(app);
        }
        return ressourceRepository.save(ressource);
    }
    public Ressource updateRessource(String code, Ressource updated) {
        Ressource existing = ressourceRepository.findById(code)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée"));

        // Mise à jour des champs de base
        existing.setLibelle(updated.getLibelle());
        existing.setTypeRessource(updated.getTypeRessource());
        existing.setStatut(updated.isStatut());

        // Gestion de l'application
        if (updated.getApplication() != null) {
            // Si une application est fournie dans la mise à jour
            if (existing.getApplication() == null) {
                existing.setApplication(new Application());
            }
            existing.getApplication().setCode(updated.getApplication().getCode());
        } else {
            // Si aucune application n'est fournie (pour dissocier)
            existing.setApplication(null);
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
            // Chargez explicitement les relations
            for (Ressource r : ressources) {
                Hibernate.initialize(r.getProfils());
                if (r.getApplication() != null) {
                    Hibernate.initialize(r.getApplication());
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