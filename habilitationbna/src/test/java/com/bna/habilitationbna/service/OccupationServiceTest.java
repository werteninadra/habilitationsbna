package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Occupation;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.OccupationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OccupationServiceTest {

    private OccupationRepository occupationRepository;
    private AgenceRepository agenceRepository;
    private OccupationService occupationService;

    @BeforeEach
    void setUp() {
        occupationRepository = mock(OccupationRepository.class);
        agenceRepository = mock(AgenceRepository.class);
        occupationService = new OccupationService(occupationRepository, agenceRepository);
    }

    @Test
    void testCreateOccupationSuccess() {
        // Arrange
        Long agenceId = 1L;
        Agence agence = new Agence();
        agence.setId(agenceId);
        agence.setNom("Agence Test");

        when(agenceRepository.findById(agenceId)).thenReturn(Optional.of(agence));

        Occupation savedOccupation = new Occupation();
        savedOccupation.setId(10L);
        when(occupationRepository.save(any(Occupation.class))).thenReturn(savedOccupation);

        // Act
        Occupation result = occupationService.createOccupation(agenceId, 50, false, "Ensoleillé", 2);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());

        // Vérifie que l’occupation passée à save contient les bons champs
        ArgumentCaptor<Occupation> captor = ArgumentCaptor.forClass(Occupation.class);
        verify(occupationRepository).save(captor.capture());
        Occupation occupationToSave = captor.getValue();

        assertEquals(agence, occupationToSave.getAgence());
        assertEquals(50, occupationToSave.getNombreClients());
        assertFalse(occupationToSave.isEstFerie());
        assertEquals("Ensoleillé", occupationToSave.getMeteo());
        assertEquals(2, occupationToSave.getJourSemaine());
        assertNotNull(occupationToSave.getDate()); // doit avoir une date
    }

    @Test
    void testCreateOccupationAgenceNotFound() {
        // Arrange
        Long agenceId = 999L;
        when(agenceRepository.findById(agenceId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> occupationService.createOccupation(agenceId, 30, true, "Pluie", 3));

        assertEquals("Agence non trouvée", exception.getMessage());

        // Vérifie que rien n’a été sauvegardé
        verify(occupationRepository, never()).save(any());
    }
}
