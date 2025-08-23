package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
public class PredictionService {
    private final AgenceRepository agenceRepository;
    private final OccupationRepository occupationRepository;
    private final RestTemplate restTemplate;

    // Constructeur principal pour Spring (prod)
    public PredictionService(AgenceRepository agenceRepository,
                             OccupationRepository occupationRepository,
                             RestTemplate restTemplate) {
        this.agenceRepository = agenceRepository;
        this.occupationRepository = occupationRepository;
        this.restTemplate = restTemplate;
    }

    // Constructeur secondaire pour compatibilité (création normale)

    public String preparePredictionRequest(Long agenceId, List<Occupation> occupations, int capaciteMax) {
        List<Map<String, Object>> historique = occupations.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", o.getDate().toString());
            map.put("estFerie", o.isEstFerie());
            // Ne pas envoyer tauxOccupation à Flask, il ne sert pas pour la prédiction
            // map.put("tauxOccupation", o.getTauxOccupation());
            String meteoVal = o.getMeteo();
            if (meteoVal == null || meteoVal.isEmpty() || meteoVal.equalsIgnoreCase("inconnu")) {
                meteoVal = "inconnu";
            }
            map.put("meteo", meteoVal);
            map.put("nombreClients", o.getNombreClients());
            map.put("jourSemaine", o.getJourSemaine());
            map.put("capacite_max", capaciteMax);
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> payload = new HashMap<>();
        payload.put("agenceId", agenceId);
        payload.put("historique", historique);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectMapper mapper = new ObjectMapper();
        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(payload);
            System.out.println("Envoi payload JSON: " + jsonPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la conversion JSON : " + e.getMessage());
        }

        HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

        String url = "http://localhost:5000/predict";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("Réponse du serveur Flask: " + response.getBody());

            Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);
            if (responseMap.containsKey("error")) {
                return "Erreur du serveur de prédiction: " + responseMap.get("error");
            }

            List<Integer> predictions = (List<Integer>) responseMap.get("prediction");
            if (predictions == null || predictions.isEmpty()) {
                return "Prédiction indisponible";
            }

            int predictionToday = predictions.get(0);
            return predictionToday == 1 ? "L'agence sera pleine" : "L'agence ne sera pas pleine";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la lecture de la prédiction : " + e.getMessage();
        }
    }

    public String predictToday(Long agenceId) {
        LocalDate today = LocalDate.now();

        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée"));

        List<Occupation> occupationsToday = occupationRepository.findByAgenceIdAndDate(agenceId, today);

        if (occupationsToday.isEmpty()) {
            Occupation occupation = new Occupation();
            occupation.setAgence(agence);
            occupation.setDate(today);
            occupation.setNombreClients(0);
            occupation.setMeteo("inconnu");
            occupation.setEstFerie(false);
            occupation.setJourSemaine(today.getDayOfWeek().getValue() - 1); // 0=lundi .. 6=dimanche

            occupationRepository.save(occupation);
            occupationsToday = List.of(occupation);
        }

        int capaciteMax = agence.getCapaciteMax();

        return preparePredictionRequest(agenceId, occupationsToday, capaciteMax);
    }
}
