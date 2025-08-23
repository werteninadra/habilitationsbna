package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setMatricule("USR001");
        user.setPassword("secret");

        // Créons un profil fictif
        Profil profil1 = new Profil();
        profil1.setNom("ROLE_ADMIN");   // ✅ utiliser setNom()

        Profil profil2 = new Profil();
        profil2.setNom("ROLE_USER");    // ✅ utiliser setNom()

        Set<Profil> profils = new HashSet<>();
        profils.add(profil1);
        profils.add(profil2);

        user.setProfils(profils);

        // Ajoutons une agence factice
        Agence agence = new Agence();
        agence.setId(1L);
        agence.setNom("Agence Test");
        user.setAgence(agence);

        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testHasRole() {
        assertTrue(customUserDetails.hasRole("ADMIN"));
        assertTrue(customUserDetails.hasRole("USER"));
        assertFalse(customUserDetails.hasRole("MANAGER"));
    }

    @Test
    void testGetRoles() {
        Set<String> roles = customUserDetails.getRoles();
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_USER"));
    }

    @Test
    void testGetPasswordAndUsername() {
        assertEquals("secret", customUserDetails.getPassword());
        assertEquals("USR001", customUserDetails.getUsername());
    }

    @Test
    void testGetAgence() {
        assertNotNull(customUserDetails.getAgence());
        assertEquals("Agence Test", customUserDetails.getAgence().getNom());
    }

    @Test
    void testAccountStatus() {
        assertTrue(customUserDetails.isAccountNonExpired());
        assertTrue(customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.isCredentialsNonExpired());
        assertTrue(customUserDetails.isEnabled());
    }
}
