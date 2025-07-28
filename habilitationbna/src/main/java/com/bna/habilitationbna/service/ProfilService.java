package com.bna.habilitationbna.service;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.repo.ProfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ProfilService {

    private final ProfilRepository profilRepository;
    private final KeycloakAdminClientService keycloakService;

    public ProfilService(ProfilRepository profilRepository,
                         KeycloakAdminClientService keycloakService) {
        this.profilRepository = profilRepository;
        this.keycloakService = keycloakService;
    }

    @Transactional
    public Profil createProfil(String nom, String description) {
        // Crée d'abord le rôle dans Keycloak
        //keycloakService.createRoleIfNotExists(nom);

        // Puis dans la base locale
        Profil profil = new Profil();
        profil.setNom(nom);
        profil.setDescription(description);
        return profilRepository.save(profil);
    }

    public Optional<Profil> findByNom(String nom) {
        return profilRepository.findByNom(nom);
    }

    public Set<Profil> findByNoms(Set<String> noms) {
        return profilRepository.findByNomIn(noms);
    }

    public List<Profil> findAll() {
        return profilRepository.findAll();
    }
}