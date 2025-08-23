package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PredictionServiceTest {

    private AgenceRepository agenceRepository;
    private OccupationRepository occupationRepository;
    private RestTemplate restTemplate;
    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        agenceRepository = mock(AgenceRepository.class);
        occupationRepository = mock(OccupationRepository.class);
        restTemplate = mock(RestTemplate.class);

        // Injection du RestTemplate mocké
        predictionService = new PredictionService(agenceRepository, occupationRepository, restTemplate);
    }

    @Test
    void testPredictToday_withExistingOccupationAndPredictionOK() {
        Long agenceId = 1L;
        Agence agence = new Agence();
        agence.setId(agenceId);
        agence.setCapaciteMax(100);

        Occupation occupation = new Occupation();
        occupation.setId(10L);
        occupation.setAgence(agence);
        occupation.setDate(LocalDate.now());
        occupation.setNombreClients(20);
        occupation.setMeteo("ensoleille");
        occupation.setEstFerie(false);
        occupation.setJourSemaine(1);

        when(agenceRepository.findById(agenceId)).thenReturn(Optional.of(agence));
        when(occupationRepository.findByAgenceIdAndDate(eq(agenceId), any(LocalDate.class)))
                .thenReturn(List.of(occupation));

        // Simule la réponse Flask
        String fakeResponse = "{\"prediction\":[1]}";
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(fakeResponse, HttpStatus.OK));

        // Act
        String result = predictionService.predictToday(agenceId);

        // Assert
        assertEquals("L'agence sera pleine", result);
    }

    @Test
    void testPredictToday_whenAgenceNotFound() {
        when(agenceRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> predictionService.predictToday(999L));

        assertEquals("Agence non trouvée", ex.getMessage());
    }

    @Test
    void testPredictToday_whenFlaskError() {
        Long agenceId = 1L;
        Agence agence = new Agence();
        agence.setId(agenceId);
        agence.setCapaciteMax(50);

        when(agenceRepository.findById(agenceId)).thenReturn(Optional.of(agence));
        when(occupationRepository.findByAgenceIdAndDate(eq(agenceId), any(LocalDate.class)))
                .thenReturn(List.of());

        when(occupationRepository.save(any(Occupation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Flask down"));

        String result = predictionService.predictToday(agenceId);

        assertTrue(result.contains("Erreur lors de la lecture de la prédiction"));
    }
}
