package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.UserRepository;
import com.bna.habilitationbna.service.ProfilService;
import com.bna.habilitationbna.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    /*private final KeycloakAdminClientService keycloakService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(KeycloakAdminClientService keycloakService,
                          UserService userService,
                          UserRepository userRepository) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.userRepository = userRepository;
    }*/

    /*@PostMapping("/register")
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
    }*/
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClientService.class);

    private final KeycloakAdminClientService keycloakService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProfilService ps;
    private final ProfilRepository profilRepository;

    public AuthController(KeycloakAdminClientService keycloakService,
                          UserService userService,
                          UserRepository userRepository,
                          ProfilService ps, ProfilRepository profilRepository) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.ps = ps;
        this.profilRepository = profilRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Map<String, Object> payload) {
        try {
            String matricule = (String) payload.get("matricule");
            String email = (String) payload.get("email");
            String password = (String) payload.get("password");
            String nom = (String) payload.get("nom");
            String prenom = (String) payload.get("prenom");
            String telephone = (String) payload.get("telephone");

            @SuppressWarnings("unchecked")
            Set<String> profilNoms = new HashSet<>((Collection<String>) payload.get("profils"));

            if (matricule == null || email == null || password == null || profilNoms.isEmpty()) {
                return ResponseEntity.badRequest().body("❗ Champs manquants ou invalides.");
            }

            if (userRepository.findByMatricule(matricule).isPresent()) {
                return ResponseEntity.badRequest().body("❌ Matricule déjà utilisé");
            }

            Set<Profil> profils = profilRepository.findByNomIn(profilNoms);
            if (profils.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Aucun profil valide trouvé");
            }

            // Création utilisateur dans Keycloak (sans rôles)
            keycloakService.createUserWithProfils(matricule, email, password, profils);

            // Sauvegarde dans la base locale
            User user = new User();
            user.setMatricule(matricule);
            user.setEmail(email);
            user.setPassword(userService.encodePassword(password));
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(telephone);
            user.setProfils(profils);

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @GetMapping("/users-with-details")
    public ResponseEntity<List<User>> getAllUsersWithDetails() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update/{matricule}")
    public ResponseEntity<?> updateUserEverywhere(
            @PathVariable String matricule,
            @RequestBody UserUpdateRequest request) {

        try {
            // 1. Vérifier que l'utilisateur existe
            User existingUser = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // 2. Préparer les profils
            Set<Profil> profils = request.getProfils() != null ?
                    ps.findByNoms(request.getProfils()) :
                    existingUser.getProfils();

            // 3. Mettre à jour Keycloak
            try {
                keycloakService.updateUserAndRoles(
                        matricule,
                        request.getEmail() != null ? request.getEmail() : existingUser.getEmail(),
                        profils
                );
            } catch (Exception e) {
                logger.error("Échec Keycloak, continuation avec la base locale seulement", e);
                // Vous pouvez choisir de continuer ou non
            }

            // 4. Mettre à jour la base locale
            if (request.getEmail() != null) existingUser.setEmail(request.getEmail());
            if (request.getNom() != null) existingUser.setNom(request.getNom());
            if (request.getPrenom() != null) existingUser.setPrenom(request.getPrenom());
            if (request.getTelephone() != null) existingUser.setTelephone(request.getTelephone());
            if (profils != null) existingUser.setProfils(profils);

            User savedUser = userRepository.save(existingUser);

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur lors de la mise à jour",
                            "message", e.getMessage(),
                            "timestamp", Instant.now()
                    ));
        }
    }
    public static class UserUpdateRequest {
        private String email;
        private String nom;
        private String prenom;
        private String telephone;
        private Set<String> profils;

        // Getters et Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getTelephone() { return telephone; }
        public void setTelephone(String telephone) { this.telephone = telephone; }
        public Set<String> getProfils() { return profils; }
        public void setProfils(Set<String> profils) { this.profils = profils; }
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