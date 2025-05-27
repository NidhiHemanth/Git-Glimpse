package org.bootcamp.com.repository;

import org.bootcamp.com.model.UserSummary;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SummarizerRepository extends ReactiveMongoRepository<UserSummary, String> {}
