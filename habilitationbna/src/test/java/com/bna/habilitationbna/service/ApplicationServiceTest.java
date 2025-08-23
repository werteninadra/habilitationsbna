package com.bna.habilitationbna.service;

import com.bna.habilitationbna.exception.RessourceNotFoundException;
import com.bna.habilitationbna.model.Application;
import com.bna.habilitationbna.repo.Applicationrepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @Mock
    private Applicationrepo applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeleteApplicationNotFound() {
        String code = "ABC";

        // S'assurer que l'application n'existe pas
        when(applicationRepository.existsById(code)).thenReturn(false);

        // Vérifie que la bonne exception est levée
        assertThrows(RessourceNotFoundException.class, () -> {
            applicationService.deleteApplication(code);
        });
    }

    @Test
    void testCreateApplicationSuccess() {
        Application app = new Application();
        app.setCode("APP001");
        when(applicationRepository.existsById("APP001")).thenReturn(false);
        when(applicationRepository.save(app)).thenReturn(app);

        Application result = applicationService.createApplication(app);

        assertNotNull(result);
        assertEquals("APP001", result.getCode());
        verify(applicationRepository).save(app);
    }

    @Test
    void testCreateApplicationAlreadyExists() {
        Application app = new Application();
        app.setCode("APP001");
        when(applicationRepository.existsById("APP001")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                applicationService.createApplication(app));

        assertEquals("Une application avec ce code existe déjà", exception.getMessage());
        verify(applicationRepository, never()).save(app);
    }

    @Test
    void testGetAllApplications() {
        Application app1 = new Application();
        Application app2 = new Application();
        when(applicationRepository.findAll()).thenReturn(List.of(app1, app2));

        List<Application> apps = applicationService.getAllApplications();

        assertEquals(2, apps.size());
        verify(applicationRepository).findAll();
    }

    @Test
    void testGetApplicationByCodeFound() {
        Application app = new Application();
        app.setCode("APP001");
        when(applicationRepository.findById("APP001")).thenReturn(Optional.of(app));

        Application result = applicationService.getApplicationByCode("APP001");

        assertNotNull(result);
        assertEquals("APP001", result.getCode());
    }

    @Test
    void testGetApplicationByCodeNotFound() {
        when(applicationRepository.findById("APP001")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RessourceNotFoundException.class, () ->
                applicationService.getApplicationByCode("APP001"));

        assertEquals("Application non trouvée avec le code: APP001", exception.getMessage());
    }

    @Test
    void testUpdateApplicationSuccess() {
        Application app = new Application();
        app.setCode("APP001");
        when(applicationRepository.existsById("APP001")).thenReturn(true);
        when(applicationRepository.save(app)).thenReturn(app);

        Application updated = applicationService.updateApplication(app);

        assertNotNull(updated);
        assertEquals("APP001", updated.getCode());
        verify(applicationRepository).save(app);
    }

    @Test
    void testUpdateApplicationNotFound() {
        Application app = new Application();
        app.setCode("APP001");
        when(applicationRepository.existsById("APP001")).thenReturn(false);

        Exception exception = assertThrows(RessourceNotFoundException.class, () ->
                applicationService.updateApplication(app));

        assertEquals("Application non trouvée avec le code: APP001", exception.getMessage());
    }

    @Test
    void testDeleteApplicationSuccess() {
        when(applicationRepository.existsById("APP001")).thenReturn(true);
        doNothing().when(applicationRepository).deleteById("APP001");

        applicationService.deleteApplication("APP001");

        verify(applicationRepository).deleteById("APP001");
    }
}
