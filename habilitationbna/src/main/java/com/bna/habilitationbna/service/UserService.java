package com.bna.habilitationbna.service;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
private  final AgenceRepository agencerepo;



    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       KeycloakAdminClientService keycloakService,
                       ProfilService profilService, AgenceRepository agencerepo) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.keycloakService = keycloakService;
        this.profilService = profilService;
        this.agencerepo=agencerepo;
    }

    public User registerUser(User user, Set<String> profilNoms, Jwt jwt) {
        Set<Profil> profils = profilService.findByNoms(profilNoms);
        if (profils.isEmpty()) {
            throw new RuntimeException("Aucun profil valide fourni");
        }

        // Récupère le user connecté
        String matricule = jwt.getClaim("preferred_username");
        User currentUser = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));

        boolean isAdmin = currentUser.getProfils().stream()
                .anyMatch(p -> p.getRole().equalsIgnoreCase("ADMIN"));
        boolean isChefAgence = currentUser.getProfils().stream()
                .anyMatch(p -> p.getRole().equalsIgnoreCase("CHEFAGENCE"));

        // ADMIN → peut choisir l’agence envoyée
        if (isAdmin) {
            if (user.getAgence() == null) {
                throw new RuntimeException("L'agence est obligatoire pour créer un utilisateur");
            }
        }
        // CHEF_AGENCE → on force son agence
        else if (isChefAgence) {
            user.setAgence(currentUser.getAgence());
        }
        // Sinon → pas autorisé
        else {
            throw new RuntimeException("Vous n’avez pas le droit de créer des utilisateurs");
        }

        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setProfils(profils);

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
