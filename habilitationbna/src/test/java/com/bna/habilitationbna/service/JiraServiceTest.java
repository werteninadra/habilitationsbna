package com.bna.habilitationbna.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class JiraServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JiraService jiraService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simuler les valeurs injectées @Value
        jiraService = new JiraService();
        jiraService.jiraDomain = "fake-domain.atlassian.net";
        jiraService.jiraEmail = "test@example.com";
        jiraService.jiraApiToken = "fake_token";

        // Replace le RestTemplate par le mock
        jiraService.restTemplate = restTemplate;
    }

    @Test
    void testGetTicketsForProject() {
        String fakeResponse = "{ \"issues\": [ { \"key\": \"TEST-1\" } ] }";

        ResponseEntity<String> responseEntity =
                new ResponseEntity<>(fakeResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class))
        ).thenReturn(responseEntity);

        String result = jiraService.getTicketsForProject("TEST");

        assertEquals(fakeResponse, result);
    }
}
