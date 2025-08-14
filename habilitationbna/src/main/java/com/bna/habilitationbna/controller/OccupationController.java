package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import com.bna.habilitationbna.service.IAgenceService;
import com.bna.habilitationbna.service.OccupationService;
import com.bna.habilitationbna.service.PredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/occupations")
public class OccupationController {

    private final OccupationService occupationService;
    private final PredictionService predictionService;
    private  final  OccupationRepository occupationRepository;
    private final AgenceRepository agencerepo;
    @Autowired
    private RestTemplate restTemplate;

    private  final OccupationRepository occupationrepository;
    public OccupationController(OccupationService occupationService , OccupationRepository  OccupationRepository, PredictionService predictionService, OccupationRepository occupationRepository,AgenceRepository agencerepo) {
        this.occupationService = occupationService;
        this.occupationrepository=OccupationRepository;
        this.predictionService=predictionService;
        this.occupationRepository=occupationRepository;
        this.agencerepo=agencerepo;
    }

    @PostMapping("/create/{agenceId}")
    public ResponseEntity<Occupation> createOccupation(
            @PathVariable Long agenceId,
            @RequestParam int nombreClients,
            @RequestParam boolean estFerie,
            @RequestParam String meteo,
            @RequestParam int jourSemaine)


    {

        Occupation occupation = occupationService.createOccupation(agenceId, nombreClients, estFerie, meteo, jourSemaine);
        return ResponseEntity.ok(occupation);
    }



    @GetMapping("/details/{agenceId}")
    public ResponseEntity<List<Occupation>> getOccupationsByAgence(@PathVariable Long agenceId) {
        List<Occupation> occupations = occupationrepository.findByAgenceId(agenceId);
        return ResponseEntity.ok(occupations);
    }
    @GetMapping("/prediction/today/{agenceId}")
    public ResponseEntity<Map<String, String>> getTodayPrediction(@PathVariable Long agenceId) {
        String result = predictionService.predictToday(agenceId);
        return ResponseEntity.ok(Map.of("message", result));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Occupation> updateOccupation(@PathVariable Long id, @RequestBody Occupation updatedOccupation) {
        return occupationRepository.findById(id)
                .map(occupation -> {
                    occupation.setNombreClients(updatedOccupation.getNombreClients());
                    occupation.setEstFerie(updatedOccupation.isEstFerie());
                    occupation.setMeteo(updatedOccupation.getMeteo());
                    occupation.setJourSemaine(updatedOccupation.getJourSemaine());
                    occupationRepository.save(occupation);
                    return ResponseEntity.ok(occupation);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOccupation(@PathVariable Long id) {
        if (!occupationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        occupationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }



}
