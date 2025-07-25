package com.bna.habilitationbna.service;

import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        if (userRepository.findByMatricule(user.getMatricule()).isPresent()) {
            throw new RuntimeException("Matricule déjà utilisé");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    public Optional<User> findByMatricule(String matricule) {
        return userRepository.findByMatricule(matricule);
    }

  /*  public void updateLocalUser(String matricule, User updatedUser) {
        User existingUser = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + matricule));

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        userRepository.save(existingUser);
    }*/
  public String encodePassword(String rawPassword) {
      return passwordEncoder.encode(rawPassword);
  }

    public void updateLocalUser(String matricule, User updatedUser) {
        User existingUser = userRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

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
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }

        userRepository.save(existingUser);
    }
    public int deleteLocalUser(String matricule) {
        Optional<User> user = userRepository.findByMatricule(matricule);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return 1; // 1 entité supprimée
        }
        return 0; // Aucune entité supprimée
    }
}
