package com.bna.habilitationbna.service;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakAdminClientService keycloakService;
    private final ProfilService profilService;




    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       KeycloakAdminClientService keycloakService,
                       ProfilService profilService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakService = keycloakService;
        this.profilService = profilService;
    }

    public User registerUser(User user, Set<String> profilNoms) {
        Set<Profil> profils = profilService.findByNoms(profilNoms);
        if (profils.isEmpty()) {
            throw new RuntimeException("Aucun profil valide fourni");
        }

        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setProfils(profils);

        // Création dans Keycloak (sans assignation de rôles)
        keycloakService.createUserWithProfils(
                user.getMatricule(),
                user.getEmail(),
                rawPassword,
                profils
        );

        return userRepository.save(user);
    }
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }



    public Optional<User> findByMatricule(String matricule) {
        return userRepository.findByMatricule(matricule);
    }

    @Transactional
    public User updateUser(String matricule, User updatedUser) {
        User existingUser = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mise à jour des champs de base
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getNom() != null) {
            existingUser.setNom(updatedUser.getNom());
        }
        if (updatedUser.getPrenom() != null) {
            existingUser.setPrenom(updatedUser.getPrenom());
        }
        if (updatedUser.getTelephone() != null) {
            existingUser.setTelephone(updatedUser.getTelephone());
        }

        // Mise à jour des profils si fournis
        if (updatedUser.getProfils() != null && !updatedUser.getProfils().isEmpty()) {
            existingUser.setProfils(updatedUser.getProfils());
        }

        return userRepository.save(existingUser);
    }
    public int deleteLocalUser(String matricule) {
        Optional<User> user = userRepository.findByMatricule(matricule);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return 1;
        }
        return 0;
    }
}
