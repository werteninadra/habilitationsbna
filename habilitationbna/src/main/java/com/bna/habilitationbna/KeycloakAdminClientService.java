package com.bna.habilitationbna;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
@Service
public class KeycloakAdminClientService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;    // exemple: http://localhost:8080

    @Value("${keycloak.admin.realm}")
    private String adminRealm;   // "master"

    @Value("${keycloak.realm}")
    private String targetRealm;  // "bna-realm"

    @Value("${keycloak.admin.client-id}")
    private String clientId;     // "admin-cli"

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    private String fetchAdminToken() {
        String url = serverUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String,String>> req = new HttpEntity<>(form, headers);
        Map<String,Object> resp = restTemplate.postForObject(url, req, Map.class);
        return (String) resp.get("access_token");
    }

    public void createUserInKeycloak(String username, String email, String password) {
        // 1) Récupère un token admin tout neuf
        String adminToken = fetchAdminToken();

        // 2) Prépare la requête de création
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String,Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("enabled", true);

        Map<String,Object> cred = new HashMap<>();
        cred.put("type", "password");
        cred.put("value", password);
        cred.put("temporary", false);

        user.put("credentials", List.of(cred));

        HttpEntity<Map<String,Object>> request = new HttpEntity<>(user, headers);

        // 3) Appelle Keycloak
        restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users",
                HttpMethod.POST,
                request,
                String.class
        );
    }
    public void updateUserInKeycloak(String username, String newEmail) {
        String token = fetchAdminToken();

        // Trouver l'utilisateur
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users?username=" + username,
                HttpMethod.GET,
                entity,
                List.class
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null || users.isEmpty()) throw new RuntimeException("Utilisateur introuvable");

        String userId = (String) users.get(0).get("id");

        // Mettre à jour
        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("email", newEmail);
        updatedUser.put("username", username);
        updatedUser.put("enabled", true);

        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(token);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(updatedUser, updateHeaders);

        restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users/" + userId,
                HttpMethod.PUT,
                updateRequest,
                Void.class
        );
    }

    public List<Map<String, Object>> getAllUsersFromKeycloak() {
        String adminToken = fetchAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users",
                HttpMethod.GET,
                entity,
                List.class
        );

        return response.getBody();
    }
    public void deleteUserFromKeycloak(String username) {
        String token = fetchAdminToken();

        // 1. Trouver l'ID de l'utilisateur dans Keycloak
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Spécifiez Map<String, Object> comme type de réponse
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users?username=" + username,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new RuntimeException("Utilisateur Keycloak non trouvé");
        }

        // Cast explicite vers Map et récupération de l'ID
        Map<String, Object> userData = response.getBody().get(0);
        String userId = (String) userData.get("id");

        // 2. Supprimer l'utilisateur
        restTemplate.exchange(
                serverUrl + "/admin/realms/" + targetRealm + "/users/" + userId,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

}
////