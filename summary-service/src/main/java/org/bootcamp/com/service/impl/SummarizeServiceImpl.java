package org.bootcamp.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.constants.Constants;
import org.bootcamp.com.service.SummarizeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class SummarizeServiceImpl implements SummarizeService {
    private final RestTemplate restTemplate;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${github.api.token:}")
    private String githubApiToken;

    public SummarizeServiceImpl() {
        this.restTemplate = new RestTemplateBuilder().build();
    }

    @Override
    public Optional<String> summarizeGithubUser(String username) {
        if(openAiApiKey.isEmpty()) {
            log.error("OPEN_AI_API_KEY not provided, cannot summarize this user {}", username);
            return Optional.empty();
        }

        final String prompt = constructPrompt(username);
        log.info("AI Prompt: {}", prompt);
        log.info("API Key prefix: {}", (openAiApiKey != null ? openAiApiKey.substring(0, 5) : "null"));
//        return Optional.ofNullable(getAiSummary(prompt));
        return Optional.of(prompt);
    }

    // get their top five projects (in terms of stars, forks, collaborators)
    // what their recent interest is based on recently starred project
    // how active they were in the past year
    // top 3 projects they worked on in the past year
    private String getAiSummary(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(Map.of(
            "role", "user",
            "content", prompt
        )));

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                Constants.OPEN_AI_API_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("choices")) {
            throw new IllegalStateException("Invalid response from OpenAI API: missing 'choices'");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices.isEmpty() || !choices.getFirst().containsKey("message")) {
            throw new IllegalStateException("Invalid response: no message in choices");
        }

        Map<String, Object> message = (Map<String, Object>) choices.getFirst().get("message");
        return (String) message.get("content");
    }

    private String constructPrompt(String username) {
        String userRepos = getUserProjects(String.format(Constants.GITHUB_REPOS_URL, username) + "?per_page=20");
        String starredRepos = getUserProjects(String.format(Constants.GITHUB_STARRED_REPOS_URL, username) + "?per_page=10");

        return String.format("""
              Analyze this GitHub user's personal repository JSON data + starred repositories data and summarize the following about the user:
            - Their top 5 repositories and their summary (based on stars, forks, collaborators)
            - The programming languages they work prefer to work on
            - Their recent technical interest (deduce from starred projects)
            - How active they were in the past year
            - 3 projects they worked on most recently
            Add any other interesting facts about the user available.
            
            GitHub username: %s
            Personal Repository JSON:
            %s
            Starred Repository JSON:
            %s
            """, username, userRepos, starredRepos);
    }

    private String getUserProjects(String url) {
        HttpHeaders headers = new HttpHeaders();

        if(!githubApiToken.isEmpty()) {
            log.info("\tAccessing {} Github API with Access Token {}.. (Authenticated)", url, githubApiToken.substring(0, 5));
            headers.set("Authorization", "Bearer " + githubApiToken);
        }

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return response.getBody();
    }
}
