package org.bootcamp.com.repository;

import org.bootcamp.com.model.GithubUser;
import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;

public interface GithubJdbcRepository {
    GithubUser findByUsername(String username);
    GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest);
    int deleteByUsername(String username);
    int addUser(GithubUser githubUser);
    // API to summarize user projects using openAI API
}
