package com.bna.habilitationbna;

import com.bna.habilitationbna.model.Profil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminClientService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClientService.class);

    // Constantes pour éviter la duplication
    private static final String ENABLED = "enabled";
    private static final String ADMIN_REALMS = "/admin/realms/";
    private static final String USERS = "/users/";

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    @Value("${keycloak.admin.realm}")
    private String adminRealm;
    @Value("${keycloak.realm}")
    private String targetRealm;
    @Value("${keycloak.admin.client-id}")
    private String clientId;
    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ------------------- UTILISATEURS -------------------

    public void createUserWithProfils(String username, String email, String password, Set<Profil> profils) {
        String adminToken = fetchAdminToken();
        createUserInKeycloak(adminToken, username, email, password);

        if (profils != null && !profils.isEmpty()) {
            String userId = getUserIdByUsername(adminToken, username);
            updateUserRoles(adminToken, userId, profils);
        }
    }

    public void updateUserAndRoles(String username, String newEmail, Set<Profil> profils) {
        String adminToken = fetchAdminToken();
        String userId = getUserIdByUsername(adminToken, username);

        if (newEmail != null) {
            updateUserInKeycloak(username, newEmail);
        }
        if (profils != null && !profils.isEmpty()) {
            updateUserRoles(adminToken, userId, profils);
        }
    }

    public void updateUserInKeycloak(String username, String newEmail) {
        String adminToken = fetchAdminToken();
        String userId = getUserIdByUsername(adminToken, username);

        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("email", newEmail);
        updatedUser.put("username", username);
        updatedUser.put(ENABLED, true);

        HttpHeaders headers = getAuthHeaders(adminToken);

        restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId,
                HttpMethod.PUT,
                new HttpEntity<>(updatedUser, headers),
                Void.class
        );
    }

    public List<Map<String, Object>> getAllUsersFromKeycloak() {
        String adminToken = fetchAdminToken();
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    public void deleteUserFromKeycloak(String username) {
        String adminToken = fetchAdminToken();
        String userId = getUserIdByUsername(adminToken, username);

        HttpHeaders headers = getAuthHeaders(adminToken);

        restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
    }

    // ------------------- ROLES -------------------

    private void updateUserRoles(String adminToken, String userId, Set<Profil> profils) {
        try {
            List<Map<String, Object>> currentRoles = getCurrentUserRoles(adminToken, userId);
            if (!currentRoles.isEmpty()) {
                removeAllRoles(adminToken, userId, currentRoles);
            }
            assignRolesToUser(adminToken, userId, profils);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des rôles Keycloak", e);
            throw new RuntimeException("Erreur de synchronisation des rôles Keycloak", e);
        }
    }

    private List<Map<String, Object>> getCurrentUserRoles(String adminToken, String userId) {
        HttpHeaders headers = getAuthHeaders(adminToken);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + "/role-mappings/realm",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody() != null ? response.getBody() : Collections.emptyList();
    }

    private void removeAllRoles(String adminToken, String userId, List<Map<String, Object>> rolesToRemove) {
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + "/role-mappings/realm",
                    HttpMethod.DELETE,
                    new HttpEntity<>(rolesToRemove, headers),
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            logger.warn("Erreur lors de la suppression des rôles: {}", e.getMessage());
        }
    }

    private void assignRolesToUser(String adminToken, String userId, Set<Profil> profils) {
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> rolesToAdd = profils.stream()
                .map(profil -> createRoleRepresentation(adminToken, profil.getNom()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!rolesToAdd.isEmpty()) {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + "/role-mappings/realm",
                    HttpMethod.POST,
                    new HttpEntity<>(rolesToAdd, headers),
                    Void.class
            );
        }
    }

    private Map<String, Object> createRoleRepresentation(String adminToken, String roleName) {
        try {
            HttpHeaders headers = getAuthHeaders(adminToken);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + "/roles/" + URLEncoder.encode(roleName, StandardCharsets.UTF_8),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> role = new HashMap<>();
            role.put("id", response.getBody().get("id"));
            role.put("name", roleName);
            return role;
        } catch (Exception e) {
            logger.warn("Impossible de récupérer les détails du rôle {}, utilisation du nom seulement", roleName);
            Map<String, Object> role = new HashMap<>();
            role.put("name", roleName);
            return role;
        }
    }

    // ------------------- ACTIVATION / DESACTIVATION -------------------

    public void disableUserInKeycloak(String username) {
        updateUserEnabledFlag(username, false);
    }

    public void enableUserInKeycloak(String username) {
        updateUserEnabledFlag(username, true);
    }

    private void updateUserEnabledFlag(String username, boolean enabled) {
        String adminToken = fetchAdminToken();
        String userId = getUserIdByUsername(adminToken, username);

        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put(ENABLED, enabled);

        try {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId,
                    HttpMethod.PUT,
                    new HttpEntity<>(userUpdate, headers),
                    Void.class
            );
            logger.info("Utilisateur {} {} dans Keycloak avec succès", username, enabled ? "activé" : "désactivé");
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'état de l'utilisateur dans Keycloak", e);
            throw new RuntimeException("Erreur lors de la mise à jour Keycloak", e);
        }
    }

    // ------------------- MDP -------------------

    public void sendPasswordResetEmail(String email) {
        String adminToken = fetchAdminToken();
        String userId = getUserIdByEmail(adminToken, email);

        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String resetPasswordUrl = serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + "/execute-actions-email";

        List<String> actions = Collections.singletonList("UPDATE_PASSWORD");

        restTemplate.exchange(
                resetPasswordUrl,
                HttpMethod.PUT,
                new HttpEntity<>(actions, headers),
                Void.class
        );
    }

    public void resetPasswordWithToken(String token, String newPassword) {
        String url = serverUrl + "/realms/" + targetRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("token", token);
        form.add("new_password", newPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        restTemplate.postForObject(url, request, Map.class);
    }

    // ------------------- HELPERS -------------------

    private String fetchAdminToken() {
        String url = serverUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new RuntimeException("Impossible de récupérer le token admin");
        }
        return (String) response.get("access_token");
    }

    private void createUserInKeycloak(String adminToken, String username, String email, String password) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put(ENABLED, true);
        userMap.put("credentials", List.of(
                Map.of("type", "password", "value", password, "temporary", false)
        ));

        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS,
                HttpMethod.POST,
                new HttpEntity<>(userMap, headers),
                Void.class
        );
    }

    private String getUserIdByUsername(String adminToken, String username) {
        HttpHeaders headers = getAuthHeaders(adminToken);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + "?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> body = response.getBody();
        if (body == null || body.isEmpty() || body.get(0).get("id") == null) {
            throw new RuntimeException("Utilisateur non trouvé dans Keycloak");
        }
        return body.get(0).get("id").toString();
    }

    private String getUserIdByEmail(String adminToken, String email) {
        HttpHeaders headers = getAuthHeaders(adminToken);

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String url = serverUrl + ADMIN_REALMS + targetRealm + USERS + "?email=" + encodedEmail;

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé avec cet email");
        }
        return response.getBody().get(0).get("id").toString();
    }

    private HttpHeaders getAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
