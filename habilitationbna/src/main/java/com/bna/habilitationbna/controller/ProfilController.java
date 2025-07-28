package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.service.ProfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profils")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }
    @GetMapping
    public ResponseEntity<List<Profil>> getAllProfils() {
        return ResponseEntity.ok(profilService.findAll());
    }

    @PostMapping
    public ResponseEntity<Profil> createProfil(
            @RequestBody ProfilCreationRequest request) {

        Profil profil = profilService.createProfil(request.getNom(), request.getDescription());
        return ResponseEntity.ok(profil);
    }

    // Classe interne pour la requête de création
    public static class ProfilCreationRequest {
        private String nom;
        private String description;

        // Getters et Setters
        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}