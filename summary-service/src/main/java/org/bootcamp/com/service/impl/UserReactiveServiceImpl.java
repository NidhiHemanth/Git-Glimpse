package org.bootcamp.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.model.UserSummary;
import org.bootcamp.com.repository.SummarizerRepository;
import org.bootcamp.com.service.SummarizeService;
import org.bootcamp.com.service.UserReactiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserReactiveServiceImpl implements UserReactiveService {
    @Autowired
    SummarizeService summarizeService;
    @Autowired
    SummarizerRepository repository;

    @Override
    public Mono<UserSummary> saveUser(String username) {
        log.info("Adding new user {}", username);
        return repository.save(new UserSummary(username, "Summary: " + username));
    }

    @Override
    public Mono<UserSummary> updateUser(String username) {
        log.info("Updating user {}", username);
        return repository.save(new UserSummary(username, "Updated Summary: " + username));
    }

    @Override
    public Mono<Boolean> findUser(String username) {
        return repository.findById(username).hasElement();
    }
}
