package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Ressource;
import com.bna.habilitationbna.service.RessourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ressources")
@CrossOrigin("*")
public class RessourceController {

    private final RessourceService ressourceService;

    // Injection via constructeur (recommandé)
    @Autowired
    public RessourceController(RessourceService ressourceService) {
        this.ressourceService = ressourceService;
    }

    @GetMapping
    public ResponseEntity<List<Ressource>> getAll() {
        List<Ressource> ressources = ressourceService.getAllRessources();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ressources);
    }

    @GetMapping("/with-profils")
    public ResponseEntity<List<Ressource>> getAllWithProfils() {
        List<Ressource> ressources = ressourceService.getAllRessourcesWithProfils();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(ressources);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Ressource> getById(@PathVariable String code) {
        return ressourceService.getRessourceById(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ressource> add(@RequestBody Ressource ressource) {
        try {
            Ressource saved = ressourceService.addRessource(ressource);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{code}")
    public ResponseEntity<Ressource> update(@PathVariable String code, @RequestBody Ressource updated) {
        try {
            Ressource updatedRessource = ressourceService.updateRessource(code, updated);
            return ResponseEntity.ok(updatedRessource);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
//jnjjnfvjjjfvvjnj
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        try {
            ressourceService.deleteRessource(code);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
//fvfvjjnfevenjjnjnvfejnfvejnfjnfvnjfv
    @PostMapping("/{codeRessource}/assign/{codeProfil}")
    public ResponseEntity<Void> assignToProfil(
            @PathVariable String codeRessource,
            @PathVariable String codeProfil) {
        try {
            ressourceService.assignToProfil(codeRessource, codeProfil);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}