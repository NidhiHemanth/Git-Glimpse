package org.bootcamp.com.service;

import org.bootcamp.com.model.UserSummary;
import reactor.core.publisher.Mono;

public interface UserReactiveService {
    Mono<UserSummary> saveUser(String username);
    Mono<Boolean> findUser(String username);
}
