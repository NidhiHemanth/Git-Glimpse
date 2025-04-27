package org.bootcamp.com.repository;

import org.bootcamp.com.model.GithubUser;

public interface GithubJdbcRepository {
    GithubUser findByUsername(String username);
}
