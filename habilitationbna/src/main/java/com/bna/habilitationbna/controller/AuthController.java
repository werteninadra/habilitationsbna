package com.bna.habilitationbna.controller;

import com.bna.habilitationbna.KeycloakAdminClientService;
import com.bna.habilitationbna.model.Profil;
import com.bna.habilitationbna.model.User;
import com.bna.habilitationbna.repo.ProfilRepository;
import com.bna.habilitationbna.repo.UserRepository;
import com.bna.habilitationbna.service.ProfilService;
import com.bna.habilitationbna.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
            user.setActive(true); // Add this line

            User savedUser = userRepository.save(user);

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    //@GetMapping("/users-with-details")
    //public ResponseEntity<List<User>> getAllUsersWithDetails() {
       // List<User> users = userRepository.findAll();
        //return ResponseEntity.ok(users);
    //}

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


    @GetMapping("/users-with-details")
    public ResponseEntity<List<Map<String, Object>>> getAllUsersWithDetails() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> response = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("matricule", user.getMatricule());
            userMap.put("email", user.getEmail());
            userMap.put("nom", user.getNom());
            userMap.put("prenom", user.getPrenom());
            userMap.put("telephone", user.getTelephone());
            userMap.put("active", user.getActive());
            userMap.put("blocked", user.getBlocked());
            userMap.put("profileImagePath", user.getProfileImagePath());

            // Sérialisation contrôlée des profils
            userMap.put("profils", user.getProfils().stream()
                    .map(profil -> {
                        Map<String, Object> profilMap = new HashMap<>();
                        profilMap.put("id", profil.getNom());
                        profilMap.put("nom", profil.getNom());
                        return profilMap;
                    }).collect(Collectors.toList()));

            return userMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
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
    @PostMapping("/users/{matricule}/upload-image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable String matricule,
            @RequestParam("file") MultipartFile file) {

        try {
            // Vérification du fichier
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fichier vide");
            }

            // Vérification de l'utilisateur
            User user = userRepository.findByMatricule(matricule)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Création du répertoire avec vérification
            String uploadDir = "uploads/profile-images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                try {
                    Files.createDirectories(uploadPath);
                } catch (IOException e) {
                    logger.error("Erreur création répertoire: " + uploadPath, e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erreur création répertoire");
                }
            }

            // Génération nom de fichier sécurisé
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = matricule + "_" + System.currentTimeMillis() + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Écriture sécurisée du fichier
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Erreur écriture fichier: " + filePath, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur sauvegarde fichier");
            }

            // Mise à jour de l'utilisateur
            user.setProfileImagePath("/api/auth/uploads/profile-images/" + fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Image téléchargée avec succès",
                    "imagePath", user.getProfileImagePath()
            ));

        } catch (Exception e) {
            logger.error("Erreur upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement: " + e.getMessage());
        }
    }
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