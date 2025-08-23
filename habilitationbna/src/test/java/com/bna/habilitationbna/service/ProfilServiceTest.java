package com.bna.habilitationbna.service;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.repo.ProfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProfilServiceTest {

    private ProfilRepository profilRepository;
    private KeycloakAdminClientService keycloakService;
    private ProfilService profilService;

    @BeforeEach
    void setUp() {
        profilRepository = mock(ProfilRepository.class);
        keycloakService = mock(KeycloakAdminClientService.class);
        profilService = new ProfilService(profilRepository, keycloakService);
    }

    @Test
    void createProfil_shouldSaveProfilInRepository() {
        // Arrange
        String nom = "ADMIN";
        String description = "Administrateur";
        Profil savedProfil = new Profil();
        savedProfil.setNom(nom);
        savedProfil.setDescription(description);

        when(profilRepository.save(any(Profil.class))).thenReturn(savedProfil);

        // Act
        Profil result = profilService.createProfil(nom, description);

        // Assert
        ArgumentCaptor<Profil> captor = ArgumentCaptor.forClass(Profil.class);
        verify(profilRepository).save(captor.capture());

        Profil captured = captor.getValue();
        assertThat(captured.getNom()).isEqualTo(nom);
        assertThat(captured.getDescription()).isEqualTo(description);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo(nom); // ✅ ici c’est "ADMIN", pas 1L
    }


    @Test
    void findByNom_shouldReturnOptionalProfil() {
        // Arrange
        Profil profil = new Profil();
        profil.setNom("test");
        profil.setNom("USER");

        when(profilRepository.findByNom("USER")).thenReturn(Optional.of(profil));

        // Act
        Optional<Profil> result = profilService.findByNom("USER");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getNom()).isEqualTo("USER");
    }

    @Test
    void findByNoms_shouldReturnMatchingProfils() {
        // Arrange
        Profil profil1 = new Profil();
        profil1.setNom("ADMIN");
        Profil profil2 = new Profil();
        profil2.setNom("USER");

        Set<String> noms = Set.of("ADMIN", "USER");
        Set<Profil> profils = Set.of(profil1, profil2);

        when(profilRepository.findByNomIn(noms)).thenReturn(profils);

        // Act
        Set<Profil> result = profilService.findByNoms(noms);

        // Assert
        assertThat(result).hasSize(2).extracting(Profil::getNom)
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void findAll_shouldReturnAllProfils() {
        // Arrange
        Profil profil1 = new Profil();
        profil1.setNom("ADMIN");
        Profil profil2 = new Profil();
        profil2.setNom("USER");

        when(profilRepository.findAll()).thenReturn(List.of(profil1, profil2));

        // Act
        List<Profil> result = profilService.findAll();

        // Assert
        assertThat(result).hasSize(2);
    }
}
