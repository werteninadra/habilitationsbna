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

        return response.getBody(); // directement retourner, pas besoin de null check
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

    // Définir une exception dédiée statique
    public static class KeycloakRoleUpdateException extends RuntimeException {
        public KeycloakRoleUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }



    private void updateUserRoles(String adminToken, String userId, Set<Profil> profils) {
        try {
            List<Map<String, Object>> currentRoles = Optional.ofNullable(
                    getCurrentUserRoles(adminToken, userId)
            ).orElse(Collections.emptyList());

            if (!currentRoles.isEmpty()) {
                removeAllRoles(adminToken, userId, currentRoles);
            }

            assignRolesToUser(adminToken, userId, profils);

        } catch (Exception e) {
            String profilNames = profils.stream()
                    .map(Profil::getNom)
                    .collect(Collectors.joining(","));

            // Pas de logger.error ici → on remonte l’info dans l’exception custom
            throw new KeycloakRoleUpdateException(
                    String.format(
                            "Erreur lors de la mise à jour des rôles Keycloak pour l'utilisateur %s avec profils [%s]",
                            userId, profilNames
                    ),
                    e
            );
        }
    }


    private List<Map<String, Object>> getCurrentUserRoles(String adminToken, String userId) {
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + userId +roleMappingsRealmPath,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody() ;
    }


    private void assignRolesToUser(String adminToken, String userId, Set<Profil> profils) {
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> rolesToAdd = profils.stream()
                .map(profil -> createRoleRepresentation(adminToken, profil.getNom()))
                .collect(Collectors.toUnmodifiableList()); // Immuable et compatible Java 8+

        if (!rolesToAdd.isEmpty()) {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + roleMappingsRealmPath,
                    HttpMethod.POST,
                    new HttpEntity<>(rolesToAdd, headers),
                    Void.class
            );
        }
    }


    // Exception dédiée statique

    private void removeAllRoles(String adminToken, String userId, List<Map<String, Object>> rolesToRemove) {
        HttpHeaders headers = getAuthHeaders(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + USERS + userId + roleMappingsRealmPath ,
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
            HttpHeaders headers = getAuthHeaders(adminToken);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    serverUrl + ADMIN_REALMS + targetRealm + "/roles/" + URLEncoder.encode(roleName, StandardCharsets.UTF_8),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            var responseBody = Optional.ofNullable(response.getBody())
                    .orElse(Collections.emptyMap());
            Map<String, Object> role = new HashMap<>();

            if (responseBody.get("id") != null) {
                role.put("id", responseBody.get("id"));
            }
            role.put("name", roleName);
            return role;


        } catch (Exception e) {
            logger.warn("Impossible de récupérer les détails du rôle {}, utilisation du nom seulement", roleName, e);
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
    public static class KeycloakUserUpdateException extends RuntimeException {
        public KeycloakUserUpdateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    // Méthode refactorisée
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
            logger.info("Utilisateur {} {} dans Keycloak avec succès",
                    username, enabled ? "activé" : "désactivé");

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Pas besoin de logger.error ici, tu relances déjà avec contexte
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
            throw new KeycloakTokenException("Impossible de récupérer le token admin");
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
    public class KeycloakUserNotFoundException extends RuntimeException {

        public KeycloakUserNotFoundException(String message) {
            super(message);
        }

        public KeycloakUserNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    private String getUserIdByUsername(String adminToken, String username) {
        HttpHeaders headers = getAuthHeaders(adminToken);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + ADMIN_REALMS + targetRealm + USERS + "?username=" + username,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        var body = response.getBody();
        if (body == null || body.isEmpty() || body.get(0).get("id") == null) {
            throw new KeycloakUserNotFoundException("Utilisateur non trouvé dans Keycloak : " + username);
        }

        return body.get(0).get("id").toString();
    }



    private HttpHeaders getAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
