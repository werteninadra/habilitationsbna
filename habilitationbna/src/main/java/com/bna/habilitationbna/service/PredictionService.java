package com.bna.habilitationbna.service;// PredictionService.java
import com.bna.habilitationbna.model.PredictionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class PredictionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PredictionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper(); // instanciation ici
    }


    public Map<String, Object> predict() {
        Map<String, Object> body = new HashMap<>();

        List<Map<String, Object>> historique = new ArrayList<>();
        historique.add(Map.of("date", "2025-07-01", "taux", 30, "nombreclients", 15));
        historique.add(Map.of("date", "2025-07-02", "taux", 35, "nombreclients", 20));

        body.put("historique", historique);
        body.put("jours", 7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:5000/predict", request, String.class);

        try {
            // Convertir la réponse JSON String en Map
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du parsing de la réponse JSON", e);
        }
    }

}


