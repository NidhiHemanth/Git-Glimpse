package org.bootcamp.com.controller;

import lombok.extern.slf4j.Slf4j;
import org.bootcamp.com.model.UserSummary;
import org.bootcamp.com.service.UserReactiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/summarize")
@Slf4j
public class SummarizeUserController {
    @Autowired
    UserReactiveService userReactiveService;

    @GetMapping("/user/{username}")
    public Mono<ResponseEntity<String>> summarizeUser(@PathVariable String username) {
        return userReactiveService.findUser(username)
                .flatMap(userExists -> {
                    Mono<UserSummary> summaryMono = userReactiveService.saveUser(username);
                    log.info("USER {} {} in database", username, userExists ? "updated" : "created");
                    return summaryMono
                            .map(userSummary -> ResponseEntity.ok().body(userSummary.getSummary()))
                            .defaultIfEmpty(ResponseEntity.badRequest().body("Unable to summarize the user " + username));
                });
    }
}
