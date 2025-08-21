package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.*;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import com.bna.habilitationbna.repo.UserRepository;
import com.bna.habilitationbna.service.IAgenceService;
import com.bna.habilitationbna.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agences")
public class AgenceController {

    private final IAgenceService agenceService;
    private final PredictionService predictionService;
    private final OccupationRepository occupationRepository;
    private final AgenceRepository agenceRepository;
    private final UserRepository userRepository;

    @Autowired
    public AgenceController(
            IAgenceService agenceService,
            PredictionService predictionService,
            OccupationRepository occupationRepository,
            AgenceRepository agenceRepository,
            UserRepository userRepository

    ) {
        this.agenceService = agenceService;
        this.predictionService = predictionService;
        this.occupationRepository = occupationRepository;
        this.agenceRepository = agenceRepository;
        this.userRepository=userRepository;
    }
    @GetMapping
    public ResponseEntity<List<Agence>> getAgences(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Claim exact à utiliser
        String matricule = jwt.getClaim("preferred_username");

        User user = userRepository.findByMatricule(matricule).orElse(null);

        if (user == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Vérifie si c'est un CHEF_AGENCE
        boolean isChefAgence = user.getProfils().stream()
                .anyMatch(p -> p.getRole().equalsIgnoreCase("CHEFAGENCE"));

        // Vérifie si c'est un ADMIN
        boolean isAdmin = user.getProfils().stream()
                .anyMatch(p -> p.getRole().equalsIgnoreCase("Admin"));

        if (isChefAgence) {
            return ResponseEntity.ok(user.getAgence() != null ? List.of(user.getAgence()) : Collections.emptyList());
        }

        if (isAdmin) {
            return ResponseEntity.ok(agenceRepository.findAll());
        }

        // Pour les autres utilisateurs, tu peux renvoyer ce que tu veux (par ex. vide)
        return ResponseEntity.ok(Collections.emptyList());
    }


    @GetMapping("/test-jwt")
    public ResponseEntity<Map<String, Object>> testJwt(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).body(null);
        return ResponseEntity.ok(jwt.getClaims());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<DetailsAgence> getAgenceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(agenceService.getDetails(id));
    }

    @GetMapping("/prediction/today/{agenceId}")
    public ResponseEntity<String> getTodayPrediction(@PathVariable Long agenceId) {
        String result = predictionService.predictToday(agenceId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Agence> createAgence(@RequestBody Agence newAgence) {
        Agence savedAgence = agenceService.save(newAgence);
        return ResponseEntity.ok(savedAgence);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agence> updateAgence(@PathVariable Long id, @RequestBody Agence updatedAgence) {
        Agence agence = agenceService.findById(id);
        if (agence == null) {
            return ResponseEntity.notFound().build();
        }
        agence.setNom(updatedAgence.getNom());
        agence.setCapaciteMax(updatedAgence.getCapaciteMax());
        agenceService.save(agence);
        return ResponseEntity.ok(agence);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgence(@PathVariable Long id) {
        Agence agence = agenceService.findById(id);
        if (agence == null) {
            return ResponseEntity.notFound().build();
        }
        agenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prediction/{agenceId}")
    public ResponseEntity<String> getPrediction(@PathVariable Long agenceId) {
        Agence agence = agenceService.findById(agenceId);

        if (agence == null) {
            throw new RuntimeException("Agence non trouvée");
        }

        List<Occupation> occupations = occupationRepository.findByAgenceId(agenceId);
        String result = predictionService.preparePredictionRequest(
                agenceId,
                occupations,
                agence.getCapaciteMax()
        );

        return ResponseEntity.ok(result);
    }


}
