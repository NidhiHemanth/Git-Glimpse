package org.bootcamp.com.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.exception.DataPersistenceException;
import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.repository.GithubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 @Component - used to mark a class as a Spring-managed bean
              an instance is managed by the Spring container as part of the dependency injection (DI) mechanism
              @Service should be used instead, according to stereotype
 @Autowired - used for dependency injection (design pattern)
 @Transactional - run within a transactional context
              used with CRUD operations for maintaining atomicity
 */

@Component
@Slf4j
public class GithubService {
    private final GithubRepository githubRepository;

    // Constructor injection is more preferred than Field injection
    @Autowired
    public GithubService(GithubRepository githubRepository) {
        this.githubRepository = githubRepository;
    }

    @Transactional
    public GithubUser saveUser(GithubUser githubUser) {
        final GithubUser persistedGithubUser;
        try {
            log.info("Persisted Github User entity with ID {}", githubUser);
            persistedGithubUser = githubRepository.save(githubUser);
        } catch (Exception e) {
            throw new DataPersistenceException(e);
        }
        return persistedGithubUser;
    }
}
