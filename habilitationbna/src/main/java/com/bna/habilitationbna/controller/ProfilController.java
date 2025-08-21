package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.service.ProfilService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/profils")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllProfils() {
        List<Profil> profils = profilService.findAll();

        List<Map<String, String>> response = profils.stream()
                .map(p -> {
                    Map<String, String> profilMap = new HashMap<>();
                    profilMap.put("nom", p.getNom());
                    profilMap.put("description", p.getDescription());
                    return profilMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping
    public ResponseEntity<Profil> createProfil(
            @RequestBody ProfilCreationRequest request) {

        Profil profil = profilService.createProfil(request.getNom(), request.getDescription());
        return ResponseEntity.ok(profil);
    }
@Getter
@Setter
    // Classe interne pour la requête de création
    public static class ProfilCreationRequest {
        private String nom;
        private String description;

        // Getters et Setters
        public String getNom() {
            return nom;
        }



        public String getDescription() {
            return description;
        }


    }
}