package com.bna.habilitationbna;

import com.bna.habilitationbna.model.Profil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakAdminClientService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClientService.class);

    @Value("${keycloak.role-mappings-realm-path:/role-mappings/realm}")
    private String roleMappingsRealmPath;

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
        var adminToken = fetchAdminToken();
        createUserInKeycloak(adminToken, username, email, password);

        if (profils != null && !profils.isEmpty()) {
            var userId = getUserIdByUsername(adminToken, username);
            updateUserRoles(adminToken, userId, profils);
        }
    }

    public List<Map<String, Object>> getAllUsersFromKeycloak() {
        var adminToken = fetchAdminToken();
        var headers = getAuthHeaders(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        var response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }

    public void deleteUserFromKeycloak(String username) {
        var adminToken = fetchAdminToken();
        var userId = getUserIdByUsername(adminToken, username);

        var headers = getAuthHeaders(adminToken);

        restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
    }

    // ------------------- ROLES -------------------

    public static class KeycloakRoleUpdateException extends RuntimeException {
        public KeycloakRoleUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void updateUserRoles(String adminToken, String userId, Set<Profil> profils) {
        try {
            var currentRoles = Optional.ofNullable(getCurrentUserRoles(adminToken, userId))
                    .orElse(Collections.emptyList());

            if (!currentRoles.isEmpty()) {
                removeAllRoles(adminToken, userId, currentRoles);
            }

            assignRolesToUser(adminToken, userId, profils);

        } catch (Exception e) {
            var profilNames = profils.stream()
                    .map(Profil::getNom)
                    .collect(Collectors.joining(","));

            throw new KeycloakRoleUpdateException(
                    String.format("Erreur lors de la mise à jour des rôles Keycloak pour l'utilisateur %s avec profils [%s]",
                            userId, profilNames),
                    e
            );
        }
    }

    private List<Map<String, Object>> getCurrentUserRoles(String adminToken, String userId) {
        var headers = getAuthHeaders(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        var response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + roleMappingsRealmPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        return response.getBody();
    }

    private void assignRolesToUser(String adminToken, String userId, Set<Profil> profils) {
        var headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var rolesToAdd = profils.stream()
                .map(profil -> createRoleRepresentation(adminToken, profil.getNom()))
                .collect(Collectors.toUnmodifiableList());

        if (!rolesToAdd.isEmpty()) {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + roleMappingsRealmPath,
                    HttpMethod.POST,
                    new HttpEntity<>(rolesToAdd, headers),
                    Void.class
            );
        }
    }

    private void removeAllRoles(String adminToken, String userId, List<Map<String, Object>> rolesToRemove) {
        var headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + roleMappingsRealmPath,
                    HttpMethod.DELETE,
                    new HttpEntity<>(rolesToRemove, headers),
                    Void.class
            );
        } catch (HttpClientErrorException e) {
            logger.warn("Erreur lors de la suppression des rôles: {}", e.getMessage());
        }
    }

    private Map<String, Object> createRoleRepresentation(String adminToken, String roleName) {
        try {
            var headers = getAuthHeaders(adminToken);

            var response = restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + "/roles/" + URLEncoder.encode(roleName, StandardCharsets.UTF_8),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            var responseBody = Optional.ofNullable(response.getBody()).orElse(Collections.emptyMap());
            var role = new HashMap<String, Object>();

            if (responseBody.get("id") != null) {
                role.put("id", responseBody.get("id"));
            }
            role.put("name", roleName);
            return role;

        } catch (Exception e) {
            logger.warn("Impossible de récupérer les détails du rôle {}, utilisation du nom seulement", roleName, e);
            var role = new HashMap<String, Object>();
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

    public static class KeycloakUserUpdateException extends RuntimeException {
        public KeycloakUserUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void updateUserEnabledFlag(String username, boolean enabled) {
        var adminToken = fetchAdminToken();
        var userId = getUserIdByUsername(adminToken, username);

        var headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var userUpdate = new HashMap<String, Object>();
        userUpdate.put(ENABLED, enabled);

        try {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId,
                    HttpMethod.PUT,
                    new HttpEntity<>(userUpdate, headers),
                    Void.class
            );
            logger.info("Utilisateur {} {} dans Keycloak avec succès",
                    username, enabled ? "activé" : "désactivé");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new KeycloakUserUpdateException(
                    String.format("Erreur HTTP (%s) lors de la mise à jour de l'utilisateur '%s' dans Keycloak",
                            e.getStatusCode(), username),
                    e
            );

        } catch (ResourceAccessException e) {
            throw new KeycloakUserUpdateException(
                    String.format("Accès Keycloak impossible pour l'utilisateur '%s'", username),
                    e
            );
        }
    }

    public static class KeycloakTokenException extends RuntimeException {
        public KeycloakTokenException(String message) {
            super(message);
        }
    }

    private String fetchAdminToken() {
        var url = serverUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        var request = new HttpEntity<>(form, headers);
        var response = restTemplate.postForObject(url, request, Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new KeycloakTokenException("Impossible de récupérer le token admin");
        }

        return (String) response.get("access_token");
    }

    private void createUserInKeycloak(String adminToken, String username, String email, String password) {
        var userMap = new HashMap<String, Object>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put(ENABLED, true);
        userMap.put("credentials", List.of(
                Map.of("type", "password", "value", password, "temporary", false)
        ));

        var headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS,
                HttpMethod.POST,
                new HttpEntity<>(userMap, headers),
                Void.class
        );
    }

    public static class KeycloakUserNotFoundException extends RuntimeException {
        public KeycloakUserNotFoundException(String message) { super(message); }
        public KeycloakUserNotFoundException(String message, Throwable cause) { super(message, cause); }
    }

    private String getUserIdByUsername(String adminToken, String username) {
        var headers = getAuthHeaders(adminToken);

        var response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + "?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        var body = response.getBody();
        if (body == null || body.isEmpty() || body.get(0).get("id") == null) {
            throw new KeycloakUserNotFoundException("Utilisateur non trouvé dans Keycloak : " + username);
        }

        return body.get(0).get("id").toString();
    }

    private HttpHeaders getAuthHeaders(String token) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
