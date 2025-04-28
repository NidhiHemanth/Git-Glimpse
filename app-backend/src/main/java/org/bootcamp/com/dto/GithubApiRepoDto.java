package org.bootcamp.com.dto;

public record GithubApiRepoDto (String name, String html_url, String description, Integer stargazers_count, String language, Integer forks_count) {}
