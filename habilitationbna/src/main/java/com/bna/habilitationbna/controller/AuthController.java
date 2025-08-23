package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Agence;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.AgenceRepository;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.UserRepository;
import com.bna.habilitationbna.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // 🔹 Constantes pour éviter la duplication de littéraux
    private static final String MATRICULE = "matricule";
    private static final String EMAIL = "email";
    private static final String PRENOM = "prenom";
    private static final String TELEPHONE = "telephone";
    private static final String AGENCE_ID = "agenceId";
    private static final String PROFILS = "profils";
    private static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    private static final String AGENCE_NOT_FOUND = "Agence non trouvée";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";

    private final KeycloakAdminClientService keycloakService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ProfilRepository profilRepository;
    private final AgenceRepository agenceRepository;

    public AuthController(KeycloakAdminClientService keycloakService,
                          UserService userService,
                          UserRepository userRepository,
                          ProfilRepository profilRepository,
                          AgenceRepository agenceRepository) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.profilRepository = profilRepository;
        this.agenceRepository = agenceRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody Map<String, Object> payload) {
        try {
            var matricule = (String) payload.get(MATRICULE);
            var email = (String) payload.get(EMAIL);
            var password = (String) payload.get("password");
            var nom = (String) payload.get("nom");
            var prenom = (String) payload.get(PRENOM);
            var telephone = (String) payload.get(TELEPHONE);
            var agenceId = payload.get(AGENCE_ID) != null ? Long.valueOf(payload.get(AGENCE_ID).toString()) : null;

            @SuppressWarnings("unchecked")
            var profilNoms = new HashSet<>((Collection<String>) payload.get(PROFILS));

            if (matricule == null || email == null || password == null || profilNoms.isEmpty()) {
                return ResponseEntity.badRequest().body("❗ Champs manquants ou invalides.");
            }

            if (userRepository.findByMatricule(matricule).isPresent()) {
                return ResponseEntity.badRequest().body("❌ Matricule déjà utilisé");
            }

            var profils = profilRepository.findByNomIn(profilNoms);
            if (profils.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Aucun profil valide trouvé");
            }

            var agence = (agenceId != null) ? agenceRepository.findById(agenceId)
                    .orElseThrow(() -> new IllegalStateException(AGENCE_NOT_FOUND)) : null;

            keycloakService.createUserWithProfils(matricule, email, password, profils);

            var user = new User();
            user.setMatricule(matricule);
            user.setEmail(email);
            user.setPassword(userService.encodePassword(password));
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setTelephone(telephone);
            user.setProfils(profils);
            user.setActive(true);
            user.setAgence(agence);

            var savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @PutMapping("/update/{matricule}")
    public ResponseEntity<Object> updateUserEverywhere(@PathVariable String matricule,
                                                       @RequestBody Map<String, Object> payload) {
        try {
            var existingUser = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new IllegalStateException(USER_NOT_FOUND));

            if (payload.get(EMAIL) != null) existingUser.setEmail((String) payload.get(EMAIL));
            if (payload.get("nom") != null) existingUser.setNom((String) payload.get("nom"));
            if (payload.get(PRENOM) != null) existingUser.setPrenom((String) payload.get(PRENOM));
            if (payload.get(TELEPHONE) != null) existingUser.setTelephone((String) payload.get(TELEPHONE));

            if (payload.get(PROFILS) != null) {
                @SuppressWarnings("unchecked")
                var profilNoms = new HashSet<>((Collection<String>) payload.get(PROFILS));
                var profils = profilRepository.findByNomIn(profilNoms);
                existingUser.setProfils(profils);
            }

            if (payload.get(AGENCE_ID) != null) {
                var agenceId = Long.valueOf(payload.get(AGENCE_ID).toString());
                var agence = agenceRepository.findById(agenceId)
                        .orElseThrow(() -> new IllegalStateException(AGENCE_NOT_FOUND));
                existingUser.setAgence(agence);
            }

            var savedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR, "Erreur lors de la mise à jour", MESSAGE, e.getMessage()));
        }
    }

    // 🔹 Extraction de méthodes privées pour upload
    private void createDirectory(Path uploadPath) throws IOException {
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private void saveFile(MultipartFile file, Path filePath) throws IOException {
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @PostMapping("/users/{matricule}/upload-image")
    public ResponseEntity<Object> uploadProfileImage(@PathVariable String matricule,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            var user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new IllegalStateException(USER_NOT_FOUND));

            var uploadDir = "uploads/profile-images/";
            var uploadPath = Paths.get(uploadDir);

            createDirectory(uploadPath);

            var originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            var extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            var fileName = matricule + "_" + System.currentTimeMillis() + extension;
            var filePath = uploadPath.resolve(fileName);

            saveFile(file, filePath);

            user.setProfileImagePath("/api/auth/uploads/profile-images/" + fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(MESSAGE, "Image téléchargée avec succès",
                    "imagePath", user.getProfileImagePath()));

        } catch (Exception e) {
            logger.error("Erreur upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement: " + e.getMessage());
        }
    }
    // Blocage/Déblocage utilisateur
    @PutMapping("/users/{matricule}/block")
    public ResponseEntity<?> blockUser(@PathVariable String matricule) {
        try {
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            user.setBlocked(true);
            userRepository.save(user);

            // Bloquer aussi dans Keycloak si nécessaire
            keycloakService.disableUserInKeycloak(matricule);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur bloqué avec succès",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{matricule}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable String matricule) {
        try {
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            user.setBlocked(false);
            userRepository.save(user);

            // Débloquer dans Keycloak si nécessaire
            keycloakService.enableUserInKeycloak(matricule);

            return ResponseEntity.ok(Map.of(
                    "message", "Utilisateur débloqué avec succès",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Upload image profil

    @GetMapping("/uploads/profile-images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            // Chemin absolu vers le dossier d'upload
            Path uploadDir = Paths.get("uploads/profile-images/").toAbsolutePath().normalize();
            Path file = uploadDir.resolve(filename);

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file))
                    .body(resource);
        } catch (IOException e) {
            logger.error("Erreur lecture fichier image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

    public String getUsernameFromToken(String token) {
        try {
            // Découpage du JWT
            String[] chunks = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

            // Lecture du payload JSON
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(payload, Map.class);

            return (String) json.get("preferred_username"); // ou "sub" selon votre config Keycloak
        } catch (Exception e) {
            logger.error("Erreur de décodage du token", e);
            throw new RuntimeException("Token invalide");
        }
    }
    // Dans votre contrôleur Spring
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            // 1. Extraction du matricule depuis le token JWT
            String token = authHeader.replace("Bearer ", "");
            String matricule = getUsernameFromToken(token); // Méthode utilitaire

            // 2. Recherche dans la base locale
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // 3. Mise à jour du statut
            user.activate(); // Active l'utilisateur
            userRepository.save(user);

            // 4. Gestion de l'URL de l'image
            if (user.getProfileImagePath() != null && !user.getProfileImagePath().startsWith("http")) {
                user.setProfileImagePath("http://localhost:8081" + user.getProfileImagePath());
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).build(); // Non autorisé
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        try {
            String matricule = request.get("matricule");
            if (matricule == null) {
                return ResponseEntity.badRequest().body("Matricule requis");
            }

            // 1. Valider le token
            String token = authHeader.replace("Bearer ", "");
            String tokenMatricule = getUsernameFromToken(token);

            if (!tokenMatricule.equals(matricule)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 2. Mettre à jour l'utilisateur en base
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            user.logout();
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Déconnexion réussie",
                    "status", "success"
            ));

        } catch (Exception e) {
            logger.error("Erreur lors de la déconnexion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Dans votre AuthController.java
    @PutMapping("/users/{matricule}/status")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable String matricule,
            @RequestBody Map<String, String> request) {

        try {
            // 1. Valider l'action
            String action = request.get("action");
            if (action == null || (!action.equals("block") && !action.equals("unblock"))) {
                return ResponseEntity.badRequest().body("Action invalide - doit être 'block' ou 'unblock'");
            }

            // 2. Trouver l'utilisateur
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // 3. Mettre à jour le statut
            boolean shouldBlock = action.equals("block");
            user.setBlocked(shouldBlock);
            userRepository.save(user);

            // 4. Synchroniser avec Keycloak
            if (shouldBlock) {
                keycloakService.disableUserInKeycloak(matricule);
            } else {
                keycloakService.enableUserInKeycloak(matricule);
            }

            return ResponseEntity.ok(Map.of(
                    "message", shouldBlock ? "Utilisateur bloqué avec succès" : "Utilisateur débloqué avec succès",
                    "status", "success",
                    "blocked", shouldBlock
            ));

        } catch (Exception e) {
            logger.error("Erreur lors du changement de statut", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur lors de l'opération",
                            "message", e.getMessage()
                    ));
        }
    }}