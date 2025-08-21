package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Ressource;
import com.bna.habilitationbna.service.RessourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ressources")
@CrossOrigin("*")
public class RessourceController {

    private static final Logger log = LoggerFactory.getLogger(RessourceController.class);

    private final RessourceService ressourceService;

    @Autowired
    public RessourceController(RessourceService ressourceService) {
        this.ressourceService = ressourceService;
    }

    // Récupérer toutes les ressources
    @GetMapping
    public ResponseEntity<List<Ressource>> getAll() {
        List<Ressource> ressources = ressourceService.getAllRessources();
        return ResponseEntity.ok(ressources);
    }

    // Récupérer toutes les ressources avec profils
    @GetMapping("/with-profils")
    public ResponseEntity<List<Ressource>> getAllWithProfils() {
        return ResponseEntity.ok(ressourceService.getAllRessourcesWithProfils());
    }

    // Récupérer une ressource par code
    @GetMapping("/{code}")
    public ResponseEntity<Ressource> getById(@PathVariable String code) {
        return ressourceService.getRessourceById(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Ajouter une ressource
    @PostMapping
    public ResponseEntity<Ressource> add(@RequestBody Ressource ressource) {
        try {
            return ResponseEntity.ok(ressourceService.addRessource(ressource));
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de la ressource", e);
            return ResponseEntity.badRequest().build();
        }
    }
    @PreAuthorize("hasRole('Admin')")
    // Modifier une ressource
    @PutMapping("/{code}")
    public ResponseEntity<Ressource> update(@PathVariable String code, @RequestBody Ressource updated) {
        try {
            return ResponseEntity.ok(ressourceService.updateRessource(code, updated));
        } catch (RuntimeException e) {
            log.warn("Ressource {} non trouvée pour mise à jour", code);
            return ResponseEntity.notFound().build();
        }

    }

    // Upload d’un fichier vers IPFS
    @PostMapping(value = "/{code}/upload-ipfs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ressource> uploadFileToIpfs(
            @PathVariable String code,
            @RequestParam("file") MultipartFile multipartFile) {

        log.info("=== Début Upload IPFS pour la ressource: {} ===", code);

        if (multipartFile.isEmpty()) {
            log.error("Fichier vide reçu pour la ressource {}", code);
            return ResponseEntity.badRequest().build();
        }

        File tempFile = null;
        try {
            Optional<Ressource> ressourceOpt = ressourceService.getRessourceById(code);
            if (ressourceOpt.isEmpty()) {
                log.warn("Ressource {} introuvable pour upload IPFS", code);
                return ResponseEntity.notFound().build();
            }

            // Création du fichier temporaire
            String originalFilename = multipartFile.getOriginalFilename();
            tempFile = File.createTempFile("ipfs-", originalFilename != null ? originalFilename : "upload");
            multipartFile.transferTo(tempFile);

            log.info("Fichier temporaire créé: {} ({} octets)", tempFile.getAbsolutePath(), tempFile.length());

            // Upload vers IPFS
            Ressource updated = ressourceService.uploadFileToRessource(code, tempFile);

            log.info("Upload IPFS réussi pour la ressource {}", code);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("Échec upload IPFS pour la ressource {}", code, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("Impossible de supprimer le fichier temporaire {}", tempFile.getAbsolutePath());
            }
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        try {
            ressourceService.deleteRessource(code);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.warn("Ressource {} introuvable pour suppression", code);
            return ResponseEntity.notFound().build();
        }
    }

    // Associer ressource à un profil
    @PostMapping("/{codeRessource}/assign/{codeProfil}")
    public ResponseEntity<Void> assignToProfil(
            @PathVariable String codeRessource,
            @PathVariable String codeProfil) {
        try {
            ressourceService.assignToProfil(codeRessource, codeProfil);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Erreur lors de l'association ressource {} au profil {}", codeRessource, codeProfil, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
