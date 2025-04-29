package org.bootcamp.com.service;

import java.util.Optional;

public interface SummarizeService {
    Optional<String> summarizeGithubUser(String username);
}
