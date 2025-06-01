package org.bootcamp.com.repository;

import org.bootcamp.com.request.GithubUserFetchRequest;
import org.bootcamp.com.response.GithubUserFetchResponse;

public interface GithubCustomJpaRepository {
    GithubUserFetchResponse fetchUsers(GithubUserFetchRequest fetchRequest);
}
