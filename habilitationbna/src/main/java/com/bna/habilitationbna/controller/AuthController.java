package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.UserRepository;
import com.bna.habilitationbna.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final KeycloakAdminClientService keycloakService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(KeycloakAdminClientService keycloakService,
                          UserService userService,
                          UserRepository userRepository) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        // Vérification si le matricule existe déjà
        if (userRepository.findByMatricule(user.getMatricule()).isPresent()) {
            return ResponseEntity.badRequest().body("Matricule déjà utilisé");
        }

        // 1. Création dans Keycloak
        keycloakService.createUserInKeycloak(
                user.getMatricule(),
                user.getEmail(),
                user.getPassword()
        );

        // 2. Hashage du mot de passe et sauvegarde en base locale
        user.setPassword(userService.encodePassword(user.getPassword()));
        user.setRole("USER"); // Rôle par défaut

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }
    @GetMapping("/users-with-details")
    public ResponseEntity<List<User>> getAllUsersWithDetails() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update/{matricule}")
    public ResponseEntity<?> updateUser(@PathVariable String matricule, @RequestBody User userUpdate) {
        try {
            // 1. Mise à jour dans Keycloak
            keycloakService.updateUserInKeycloak(matricule, userUpdate.getEmail());

            // 2. Mise à jour dans la base locale
            userService.updateLocalUser(matricule, userUpdate);

            return ResponseEntity.ok("✅ Utilisateur mis à jour dans Keycloak et la base locale");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginInfo() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("🔒 L'authentification se fait via Keycloak.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("🚪 Déconnexion côté client");
    }

    @GetMapping("/users/matricule/{matricule}")
    public ResponseEntity<User> getUserByMatricule(@PathVariable String matricule) {
        Optional<User> user = userRepository.findByMatricule(matricule);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = keycloakService.getAllUsersFromKeycloak();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{matricule}")
    public ResponseEntity<String> deleteUser(@PathVariable String matricule) {
        try {
            // 1. Supprimer de Keycloak
            keycloakService.deleteUserFromKeycloak(matricule);

            // 2. Supprimer de la base locale
            int deletedCount = userService.deleteLocalUser(matricule);

            if (deletedCount == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("⚠️ Utilisateur supprimé de Keycloak mais non trouvé en base locale");
            }

            return ResponseEntity.ok("✅ Utilisateur supprimé de Keycloak et de la base locale");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("⛔ Non connecté");
        }
        String token = authHeader.substring(7);
        return ResponseEntity.ok("🔐 Token JWT reçu : " + token);
    }
}