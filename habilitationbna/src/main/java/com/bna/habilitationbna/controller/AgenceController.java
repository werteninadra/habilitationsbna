package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.DetailsAgence;
import com.bna.habilitationbna.model.PredictionDTO;
import com.bna.habilitationbna.repo.OccupationRepository;
import com.bna.habilitationbna.service.IAgenceService;
import com.bna.habilitationbna.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/agences")
public class AgenceController {
    private final IAgenceService agenceService;
    private final PredictionService predictionService;
private  final OccupationRepository occupationRepository;
    @Autowired // Injection explicite
    public AgenceController(IAgenceService agenceService,
                            PredictionService predictionService , OccupationRepository occupationRepository) {
        this.agenceService = agenceService;
        this.predictionService = predictionService;
        this.occupationRepository=occupationRepository;
    }
    // Endpoints CRUD standards
    @GetMapping
    public ResponseEntity<List<Agence>> getAllAgences() { // Notez les chevrons corrects >>
        return ResponseEntity.ok(agenceService.findAll());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<DetailsAgence> getAgenceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(agenceService.getDetails(id));
    }

    // Endpoint IA

    @GetMapping("/predict/{id}")
    public ResponseEntity<?> predict(@PathVariable("id") int agenceId) {
        Map<String, Object> predictionResult = predictionService.predict();
        return ResponseEntity.ok(predictionResult);
    }


    private List<Map<String, Object>> fetchHistoriqueFromDatabase(Long id) {
        // TODO: Implémenter la récupération réelle des données, format attendu :
        // Liste de Map avec clés : "date" (String), "taux" (double), "nombreClients" (int)
        // Exemple dummy :
        return List.of(
                Map.of("date", "2025-07-01", "taux", 70.5, "nombreClients", 100),
                Map.of("date", "2025-07-02", "taux", 72.0, "nombreClients", 110),
                Map.of("date", "2025-07-03", "taux", 68.0, "nombreClients", 90)
        );
    }
}