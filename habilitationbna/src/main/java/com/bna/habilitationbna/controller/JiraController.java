package com.bna.habilitationbna.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/jira")
@CrossOrigin("*")
public class JiraController {

    @Value("${jira.domain}")
    private String jiraDomain;

    @Value("${jira.email}")
    private String jiraEmail;

    @Value("${jira.apiToken}")
    private String jiraApiToken;

    @GetMapping("/issues")
    public ResponseEntity<String> getIssues(@RequestParam String project) {
        // Utilisation de la clé projet, exemple : BNAP
        String url = "https://" + jiraDomain + "/rest/api/3/search?jql=project=" + project;

        String auth = jiraEmail + ":" + jiraApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\":\"Erreur lors de la récupération des tickets Jira\"}");
        }
    }
}
