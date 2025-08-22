package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.Ressource;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.RessourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RessourceServiceTest {

    @Mock
    private RessourceRepository ressourceRepository;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ProfilRepository profilRepository;

    @InjectMocks
    private RessourceService ressourceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddRessourceWithApplication() {
        Application app = new Application();
        app.setCode("APP001");

        Ressource ressource = new Ressource();
        ressource.setCode("RES001");
        ressource.setApplication(app);
        ressource.setLibelle("LibelleTest");
        ressource.setTypeRessource("TypeTest");

        when(applicationService.getApplicationByCode("APP001")).thenReturn(app);
        when(ressourceRepository.save(ressource)).thenReturn(ressource);

        Ressource result = ressourceService.addRessource(ressource);

        assertNotNull(result);
        assertEquals("APP001", result.getApplication().getCode());
        verify(ressourceRepository).save(ressource);
    }

    @Test
    void testUpdateRessourceSuccess() {
        Ressource existing = new Ressource();
        existing.setCode("RES001");
        existing.setLibelle("OldLibelle");
        existing.setTypeRessource("OldType");

        Ressource updated = new Ressource();
        updated.setLibelle("NewLibelle");
        updated.setTypeRessource("NewType");

        when(ressourceRepository.findById("RES001")).thenReturn(Optional.of(existing));
        when(ressourceRepository.save(existing)).thenReturn(existing);

        Ressource result = ressourceService.updateRessource("RES001", updated);

        assertEquals("NewLibelle", result.getLibelle());
        assertEquals("NewType", result.getTypeRessource());
        verify(ressourceRepository).save(existing);
    }

    @Test
    void testDeleteRessourceSuccess() {
        when(ressourceRepository.existsById("RES001")).thenReturn(true);
        doNothing().when(ressourceRepository).deleteById("RES001");

        ressourceService.deleteRessource("RES001");

        verify(ressourceRepository).deleteById("RES001");
    }

    @Test
    void testGetAllRessources() {
        Ressource r1 = new Ressource();
        Ressource r2 = new Ressource();

        when(ressourceRepository.findAll()).thenReturn(List.of(r1, r2));

        List<Ressource> list = ressourceService.getAllRessources();

        assertEquals(2, list.size());
        verify(ressourceRepository).findAll();
    }

    @Test
    void testAssignToProfilSuccess() {
        Ressource r = new Ressource();
        r.setCode("RES001");

        Profil p = new Profil();
        p.setNom("PROF001");

        when(ressourceRepository.findById("RES001")).thenReturn(Optional.of(r));
        when(profilRepository.findById("PROF001")).thenReturn(Optional.of(p));
        when(profilRepository.save(p)).thenReturn(p);

        ressourceService.assignToProfil("RES001", "PROF001");

        assertTrue(p.getRessources().contains(r));
        verify(profilRepository).save(p);
    }
}
