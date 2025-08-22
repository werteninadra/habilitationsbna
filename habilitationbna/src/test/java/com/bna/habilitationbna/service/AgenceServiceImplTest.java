package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.repo.AgenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgenceServiceImplTest {

    @Mock
    private AgenceRepository agenceRepository;

    @InjectMocks
    private AgenceServiceImpl agenceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Agence agence1 = new Agence();
        Agence agence2 = new Agence();
        when(agenceRepository.findAll()).thenReturn(List.of(agence1, agence2));

        List<Agence> result = agenceService.findAll();
        assertEquals(2, result.size());
        verify(agenceRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdFound() {
        Agence agence = new Agence();
        when(agenceRepository.findById(1L)).thenReturn(Optional.of(agence));

        Agence result = agenceService.findById(1L);
        assertNotNull(result);
        verify(agenceRepository).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(agenceRepository.findById(1L)).thenReturn(Optional.empty());

        Agence result = agenceService.findById(1L);
        assertNull(result);
        verify(agenceRepository).findById(1L);
    }

    @Test
    void testSave() {
        Agence agence = new Agence();
        when(agenceRepository.save(agence)).thenReturn(agence);

        Agence result = agenceService.save(agence);
        assertNotNull(result);
        verify(agenceRepository).save(agence);
    }

    @Test
    void testDelete() {
        doNothing().when(agenceRepository).deleteById(1L);

        agenceService.delete(1L);
        verify(agenceRepository).deleteById(1L);
    }
}
