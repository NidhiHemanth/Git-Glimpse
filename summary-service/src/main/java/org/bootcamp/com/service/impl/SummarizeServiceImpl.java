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

        // TODO: Prompt too long, needs to fetch specific details about user
        final String prompt = constructPrompt(username);
        log.info("AI Prompt: {}", prompt);

        // TODO: Has input length restrictions and the free model does not summarize well
        final String summary = getAiSummary(prompt);
        log.info("Summary: {}", summary);

        log.info("API Key prefix: {}", (openAiApiKey != null ? openAiApiKey.substring(0, 5) : "null"));
        return Optional.of(username);
    }

    // get their top five projects (in terms of stars, forks, collaborators)
    // what their recent interest is based on recently starred project
    // how active they were in the past year
    // top 3 projects they worked on in the past year
    private String getAiSummary(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                Constants.HUGGING_FACE_AI_API_URL,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> results = response.getBody();
        if (results == null || results.isEmpty() || !results.getFirst().containsKey("summary_text")) {
            throw new IllegalStateException("Invalid response from Hugging Face");
        }

        return results.getFirst().get("summary_text").toString();
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
