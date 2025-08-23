package com.bna.habilitationbna.service;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private KeycloakAdminClientService keycloakService;
    private ProfilService profilService;
    private AgenceRepository agenceRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        keycloakService = mock(KeycloakAdminClientService.class);
        profilService = mock(ProfilService.class);
        agenceRepository = mock(AgenceRepository.class);

        userService = new UserService(userRepository, passwordEncoder, keycloakService, profilService, agenceRepository);
    }

    @Test
    void registerUser_shouldCreateUser_whenAdminProvidesAgence() {
        // Arrange
        User newUser = new User();
        newUser.setMatricule("USR002");
        newUser.setEmail("user@test.com");
        newUser.setPassword("rawPass");
        Agence agence = new Agence();
        agence.setId(1L);
        newUser.setAgence(agence);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("preferred_username")).thenReturn("ADMIN001");

        User admin = new User();
        Profil adminProfil = new Profil();
        adminProfil.setNom("ADMIN");
        admin.setProfils(Set.of(adminProfil));

        when(userRepository.findByMatricule("ADMIN001")).thenReturn(Optional.of(admin));

        Profil userProfil = new Profil();
        userProfil.setNom("USER");
        when(profilService.findByNoms(Set.of("USER"))).thenReturn(Set.of(userProfil));

        when(passwordEncoder.encode("rawPass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User result = userService.registerUser(newUser, Set.of("USER"), jwt);

        // Assert
        assertThat(result.getMatricule()).isEqualTo("USR002");
        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.getProfils()).contains(userProfil);

        verify(keycloakService).createUserWithProfils("USR002", "user@test.com", "rawPass", Set.of(userProfil));
        verify(userRepository).save(newUser);
    }

    @Test
    void registerUser_shouldThrow_whenProfilInvalid() {
        User newUser = new User();
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("preferred_username")).thenReturn("ADMIN001");
        when(profilService.findByNoms(Set.of("INVALID"))).thenReturn(Collections.emptySet());

        assertThatThrownBy(() -> userService.registerUser(newUser, Set.of("INVALID"), jwt))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aucun profil valide fourni");
    }

    @Test
    void updateUser_shouldUpdateFieldsAndProfiles() {
        User existing = new User();
        existing.setMatricule("USR001");
        existing.setNom("OldName");
        existing.setEmail("old@mail.com");
        when(userRepository.findByMatricule("USR001")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User updated = new User();
        updated.setNom("NewName");
        updated.setProfils(Set.of(new Profil()));

        User result = userService.updateUser("USR001", updated);

        assertThat(result.getNom()).isEqualTo("NewName");
        assertThat(result.getProfils()).hasSize(1);
    }

    @Test
    void deleteLocalUser_shouldReturn1_whenUserExists() {
        User existing = new User();
        when(userRepository.findByMatricule("USR001")).thenReturn(Optional.of(existing));

        int result = userService.deleteLocalUser("USR001");

        assertThat(result).isEqualTo(1);
        verify(userRepository).delete(existing);
    }

    @Test
    void deleteLocalUser_shouldReturn0_whenUserNotFound() {
        when(userRepository.findByMatricule("USR999")).thenReturn(Optional.empty());

        int result = userService.deleteLocalUser("USR999");

        assertThat(result).isEqualTo(0);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void createUser_shouldSaveUser() {
        // Arrange
        User newUser = new User();
        newUser.setMatricule("USR003");
        newUser.setNom("nadra");
        newUser.setEmail("admin@test.com");
        newUser.setPassword("password123");

        // Ajouter une agence obligatoire
        Agence agence = new Agence();
        agence.setId(1L);
        newUser.setAgence(agence);

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("preferred_username")).thenReturn("ADMIN001");

        User admin = new User();
        Profil adminProfil = new Profil();
        adminProfil.setNom("ADMIN");
        admin.setProfils(Set.of(adminProfil));
        when(userRepository.findByMatricule("ADMIN001")).thenReturn(Optional.of(admin));

        Profil userProfil = new Profil();
        userProfil.setNom("USER");
        when(profilService.findByNoms(Set.of("USER"))).thenReturn(Set.of(userProfil));

        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User result = userService.registerUser(newUser, Set.of("USER"), jwt);

        // Assert
        assertNotNull(result);
        assertEquals("nadra", result.getNom());
        assertEquals("admin@test.com", result.getEmail());
        assertEquals("encodedPass", result.getPassword());
        assertThat(result.getProfils()).contains(userProfil);

        verify(keycloakService).createUserWithProfils("USR003", "admin@test.com", "password123", Set.of(userProfil));
        verify(userRepository).save(newUser);
    }


}
