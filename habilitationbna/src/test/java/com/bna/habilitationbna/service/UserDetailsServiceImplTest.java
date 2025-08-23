package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails_whenUserExists() {
        // Arrange
        User user = new User();
        user.setMatricule("12345");
        user.setNom("Dupont");
        user.setPrenom("Jean");
        user.setPassword("secret");

        when(userRepository.findByMatricule("12345")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("12345");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("12345");
        assertThat(result.getPassword()).isEqualTo("secret");
        verify(userRepository, times(1)).findByMatricule("12345");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByMatricule("99999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("99999"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé : 99999");

        verify(userRepository, times(1)).findByMatricule("99999");
    }
}
