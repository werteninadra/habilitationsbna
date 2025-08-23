package com.bna.habilitationbna.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class JiraService {

    @Value("${jira.domain}")
    public String jiraDomain; // ex: ton_domaine.atlassian.net

    @Value("${jira.email}")
    public String jiraEmail;

    @Value("${jira.apiToken}")
    public String jiraApiToken;

    public  RestTemplate restTemplate = new RestTemplate();

    public String getTicketsForProject(String projectKey) {
        String url = UriComponentsBuilder.fromHttpUrl("https://" + jiraDomain + "/rest/api/3/search")
                .queryParam("jql", "project=" + projectKey)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodeCredentials());
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    private String encodeCredentials() {
        String auth = jiraEmail + ":" + jiraApiToken;
        return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }
}
