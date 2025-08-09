package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.DetailsAgence;
import com.bna.habilitationbna.model.Occupation;
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
import java.util.Optional;
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
        // Mettre à jour les champs que tu souhaites
        agence.setNom(updatedAgence.getNom());
        agence.setCapaciteMax(updatedAgence.getCapaciteMax());
        // Sauvegarder
        // Suppose que agenceRepository est accessible via service (adapter si besoin)
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

        String result = predictionService.preparePredictionRequest(agenceId, occupations, agence.getCapaciteMax());

        return ResponseEntity.ok(result);
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