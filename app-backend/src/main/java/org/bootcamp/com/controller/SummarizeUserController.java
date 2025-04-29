package org.bootcamp.com.controller;

import org.bootcamp.com.service.SummarizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/summarize")
public class SummarizeUserController {
    @Autowired
    SummarizeService summarizeService;

    @GetMapping("/user/{username}")
    public ResponseEntity<String> summarizeUser(@PathVariable String username) {
        return summarizeService.summarizeGithubUser(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}
